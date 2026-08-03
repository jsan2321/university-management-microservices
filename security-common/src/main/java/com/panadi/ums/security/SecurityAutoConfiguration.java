package com.panadi.ums.security;

import feign.RequestInterceptor;
import feign.Request;
import feign.Retryer;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@AutoConfiguration
@EnableMethodSecurity
public class SecurityAutoConfiguration {
    private static final String OPENAPI_BEARER_SCHEME = "bearerAuth";

    @Bean
    @ConditionalOnMissingBean(Request.Options.class)
    Request.Options feignOptions() {
        return new Request.Options(1, TimeUnit.SECONDS, 3, TimeUnit.SECONDS, true);
    }

    @Bean
    @ConditionalOnMissingBean(Retryer.class)
    Retryer feignRetryer() { return Retryer.NEVER_RETRY; }

    @Bean
    @ConditionalOnMissingBean(OpenAPI.class)
    OpenAPI umsOpenApi() {
        return new OpenAPI()
                .components(new Components().addSecuritySchemes(OPENAPI_BEARER_SCHEME,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(OPENAPI_BEARER_SCHEME));
    }

    @Bean
    @ConditionalOnMissingBean
    SecurityFilterChain serviceSecurityFilterChain(HttpSecurity http, Converter<Jwt, AbstractAuthenticationToken> converter) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health", "/actuator/info", "/actuator/prometheus").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/internal/**").hasAnyRole("INTERNAL", "PROVISIONER")
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(converter)))
                .httpBasic(httpBasic -> httpBasic.disable());
        return http.build();
    }

    @Bean
    @ConditionalOnMissingBean
    JwtDecoder jwtDecoder(
            @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}") String issuer,
            @Value("${ums.security.audience:ums-api}") String audience
    ) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(issuer + "/protocol/openid-connect/certs").build();
        OAuth2TokenValidator<Jwt> audienceValidator = token -> token.getAudience().contains(audience)
                ? OAuth2TokenValidatorResult.success()
                : OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "Required audience is missing", null));
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(JwtValidators.createDefaultWithIssuer(issuer), audienceValidator));
        return decoder;
    }

    @Bean
    @ConditionalOnMissingBean
    Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new KeycloakRealmRoleConverter());
        return converter;
    }

    @Bean
    @ConditionalOnProperty(name = "ums.security.internal.client-secret")
    InternalAccessTokenProvider internalAccessTokenProvider(@Value("${ums.security.internal.token-uri}") String tokenUri,
                                                            @Value("${ums.security.internal.client-id:ums-internal}") String clientId,
                                                            @Value("${ums.security.internal.client-secret}") String clientSecret) {
        return new InternalAccessTokenProvider(RestClient.builder(), tokenUri, clientId, clientSecret);
    }

    @Bean
    @ConditionalOnProperty(name = "ums.security.internal.client-secret")
    RequestInterceptor internalBearerTokenInterceptor(InternalAccessTokenProvider tokens) {
        return template -> template.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.getToken());
    }

    private static final class KeycloakRealmRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {
        @Override
        public Collection<GrantedAuthority> convert(Jwt jwt) {
            Map<String, Object> realmAccess = jwt.getClaim("realm_access");
            if (realmAccess == null || !(realmAccess.get("roles") instanceof Collection<?> roles)) {
                return Collections.emptyList();
            }
            List<GrantedAuthority> authorities = new ArrayList<>();
            for (Object role : roles) {
                if (role instanceof String roleName) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + roleName));
                }
            }
            return authorities;
        }
    }
}
