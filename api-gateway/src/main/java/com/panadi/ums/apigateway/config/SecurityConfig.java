package com.panadi.ums.apigateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Arrays;

@Configuration
@EnableWebSecurity
class SecurityConfig {
    private static final String[] STUDENT_PATHS = {
            "/api/v1/students/**",
            "/student-service/api/v1/students/**"
    };
    private static final String[] ACADEMIC_PATHS = {
            "/api/v1/academic/**",
            "/academic-service/api/v1/academic/**"
    };
    private static final String[] ENROLLMENT_PATHS = {
            "/api/v1/enrollments/**",
            "/enrollment-service/api/v1/enrollments/**"
    };
    private static final String[] ATTENDANCE_PATHS = {
            "/api/v1/attendance/**",
            "/attendance-service/api/v1/attendance/**"
    };
    private static final String[] ASSIGNMENT_PATHS = {
            "/api/v1/assignments/**",
            "/assignment-service/api/v1/assignments/**"
    };
    private static final String[] ASSIGNMENT_SUBMISSION_PATHS = {
            "/api/v1/assignments/*/submissions",
            "/assignment-service/api/v1/assignments/*/submissions",
            "/api/v1/assignments/*/submissions/me",
            "/assignment-service/api/v1/assignments/*/submissions/me"
    };

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, CorsConfigurationSource corsConfigurationSource) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .requestMatchers("/internal/**", "/*-service/internal/**").denyAll()
                        .requestMatchers("/identity-service/api/v1/provisioning/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/students", "/student-service/api/v1/students").denyAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/academic/teachers", "/academic-service/api/v1/academic/teachers").denyAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/students/me", "/student-service/api/v1/students/me").hasRole("STUDENT")
                        .requestMatchers(HttpMethod.GET, "/api/v1/academic/teachers/me", "/academic-service/api/v1/academic/teachers/me").hasRole("TEACHER")
                        .requestMatchers(HttpMethod.GET, ACADEMIC_PATHS).hasAnyRole("ADMIN", "TEACHER", "STUDENT")
                        .requestMatchers(ACADEMIC_PATHS).hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, STUDENT_PATHS).hasAnyRole("ADMIN", "TEACHER")
                        .requestMatchers(STUDENT_PATHS).hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, ENROLLMENT_PATHS).hasAnyRole("ADMIN", "TEACHER", "STUDENT")
                        .requestMatchers(HttpMethod.POST, "/api/v1/enrollments", "/enrollment-service/api/v1/enrollments").hasAnyRole("ADMIN", "STUDENT")
                        .requestMatchers(HttpMethod.POST, "/api/v1/enrollments/*/sections", "/enrollment-service/api/v1/enrollments/*/sections").hasAnyRole("ADMIN", "STUDENT")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/enrollments/*/sections/*/drop", "/enrollment-service/api/v1/enrollments/*/sections/*/drop").hasAnyRole("ADMIN", "STUDENT")
                        .requestMatchers(ENROLLMENT_PATHS).hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, ATTENDANCE_PATHS).hasAnyRole("ADMIN", "TEACHER", "STUDENT")
                        .requestMatchers(ATTENDANCE_PATHS).hasAnyRole("ADMIN", "TEACHER")
                        .requestMatchers(HttpMethod.GET, ASSIGNMENT_PATHS).hasAnyRole("ADMIN", "TEACHER", "STUDENT")
                        .requestMatchers(HttpMethod.POST, ASSIGNMENT_SUBMISSION_PATHS).hasAnyRole("ADMIN", "STUDENT")
                        .requestMatchers(ASSIGNMENT_PATHS).hasAnyRole("ADMIN", "TEACHER")
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())))
                .httpBasic(httpBasic -> httpBasic.disable());

        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(@Value("${ums.frontend.allowed-origins:http://localhost:3000,http://localhost:5173}") String origins) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.stream(origins.split(",")).map(String::trim).toList());
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Idempotency-Key"));
        configuration.setExposedHeaders(List.of("Location"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new KeycloakRealmRoleConverter());
        return converter;
    }

    private static class KeycloakRealmRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {
        @Override
        public Collection<GrantedAuthority> convert(Jwt jwt) {
            Map<String, Object> realmAccess = jwt.getClaim("realm_access");
            if (realmAccess == null) {
                return Collections.emptyList();
            }

            Object rolesClaim = realmAccess.get("roles");
            if (!(rolesClaim instanceof Collection<?> roles)) {
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
