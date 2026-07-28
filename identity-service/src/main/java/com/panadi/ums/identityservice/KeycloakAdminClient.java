package com.panadi.ums.identityservice;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.net.URI;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
class KeycloakAdminClient {
    private final RestClient restClient;
    private final String baseUrl;
    private final String realm;
    private final String clientId;
    private final String clientSecret;
    private volatile Token token;

    KeycloakAdminClient(RestClient.Builder builder,
                        @Value("${ums.keycloak.base-url:http://localhost:8180}") String baseUrl,
                        @Value("${ums.keycloak.realm:ums}") String realm,
                        @Value("${ums.keycloak.provisioner-client-id:ums-provisioner}") String clientId,
                        @Value("${ums.keycloak.provisioner-client-secret}") String clientSecret) {
        this.restClient = builder.build();
        this.baseUrl = baseUrl;
        this.realm = realm;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    UUID createProvisionedUser(String username, String universityEmail, String firstName, String lastName, String role, String temporaryPassword) {
        try {
            Map<String, Object> representation = Map.of(
                    "username", username, "email", universityEmail, "firstName", firstName, "lastName", lastName,
                    "enabled", true, "emailVerified", true,
                    "credentials", List.of(Map.of("type", "password", "value", temporaryPassword, "temporary", true))
            );
            URI location = restClient.post()
                    .uri(baseUrl + "/admin/realms/{realm}/users", realm)
                    .header(HttpHeaders.AUTHORIZATION, bearer())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(representation)
                    .retrieve()
                    .toBodilessEntity()
                    .getHeaders().getLocation();
            if (location == null) throw new ProvisioningException("Keycloak did not return the created user id");
            UUID userId = UUID.fromString(location.getPath().substring(location.getPath().lastIndexOf('/') + 1));
            try {
                assignRole(userId, role);
            } catch (RuntimeException roleFailure) {
                try { delete(userId); } catch (RuntimeException ignored) { }
                throw roleFailure;
            }
            return userId;
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == 409) {
                throw new ProvisioningConflictException("A Keycloak user with that username or email already exists");
            }
            throw new ProvisioningException("Keycloak user creation failed", exception);
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(KeycloakAdminClient.class);

    void requireRole(UUID userId, String role) {
        Map<?, ?>[] roles = restClient.get()
                .uri(baseUrl + "/admin/realms/{realm}/users/{userId}/role-mappings/realm/composite", realm, userId)
                .header(HttpHeaders.AUTHORIZATION, bearer())
                .retrieve().body(Map[].class);
        boolean found = roles != null && Arrays.stream(roles).anyMatch(value -> role.equals(value.get("name")));
        if (!found) throw new ProvisioningConflictException("Keycloak user does not have the required " + role + " role");
    }

    void enable(UUID userId) {
        restClient.put().uri(baseUrl + "/admin/realms/{realm}/users/{userId}", realm, userId)
                .header(HttpHeaders.AUTHORIZATION, bearer()).contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("enabled", true)).retrieve().toBodilessEntity();
    }

    void delete(UUID userId) {
        restClient.delete().uri(baseUrl + "/admin/realms/{realm}/users/{userId}", realm, userId)
                .header(HttpHeaders.AUTHORIZATION, bearer()).retrieve().toBodilessEntity();
    }

    private void assignRole(UUID userId, String role) {
        Map<?, ?> roleRepresentation = restClient.get()
                .uri(baseUrl + "/admin/realms/{realm}/roles/{role}", realm, role)
                .header(HttpHeaders.AUTHORIZATION, bearer()).retrieve().body(Map.class);
        restClient.post().uri(baseUrl + "/admin/realms/{realm}/users/{userId}/role-mappings/realm", realm, userId)
                .header(HttpHeaders.AUTHORIZATION, bearer()).contentType(MediaType.APPLICATION_JSON)
                .body(List.of(roleRepresentation)).retrieve().toBodilessEntity();
    }

    private String bearer() {
        Token current = token;
        if (current == null || current.expiresAt().isBefore(Instant.now().plusSeconds(15))) {
            synchronized (this) {
                current = token;
                if (current == null || current.expiresAt().isBefore(Instant.now().plusSeconds(15))) {
                    LinkedMultiValueMap<String, String> form = new LinkedMultiValueMap<>();
                    form.add("grant_type", "client_credentials");
                    form.add("client_id", clientId);
                    form.add("client_secret", clientSecret);
                    TokenResponse response = restClient.post()
                            .uri(baseUrl + "/realms/{realm}/protocol/openid-connect/token", realm)
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED).body(form)
                            .retrieve().body(TokenResponse.class);
                    if (response == null) throw new ProvisioningException("Keycloak returned no provisioner token");
                    token = new Token(response.accessToken(), Instant.now().plusSeconds(response.expiresIn()));
                }
            }
        }
        return "Bearer " + token.value();
    }

    private record Token(String value, Instant expiresAt) {}
    private record TokenResponse(@JsonProperty("access_token") String accessToken,
                                 @JsonProperty("expires_in") long expiresIn) {}
}
