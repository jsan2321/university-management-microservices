package com.panadi.ums.apigateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .requestMatchers(HttpMethod.GET, ACADEMIC_PATHS).hasAnyRole("ADMIN", "TEACHER", "STUDENT")
                        .requestMatchers(ACADEMIC_PATHS).hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, STUDENT_PATHS).hasAnyRole("ADMIN", "TEACHER")
                        .requestMatchers(STUDENT_PATHS).hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, ENROLLMENT_PATHS).hasAnyRole("ADMIN", "TEACHER", "STUDENT")
                        .requestMatchers(ENROLLMENT_PATHS).hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, ATTENDANCE_PATHS).hasAnyRole("ADMIN", "TEACHER", "STUDENT")
                        .requestMatchers(ATTENDANCE_PATHS).hasAnyRole("ADMIN", "TEACHER")
                        .requestMatchers(HttpMethod.GET, ASSIGNMENT_PATHS).hasAnyRole("ADMIN", "TEACHER", "STUDENT")
                        .requestMatchers(ASSIGNMENT_PATHS).hasAnyRole("ADMIN", "TEACHER")
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())))
                .httpBasic(httpBasic -> httpBasic.disable());

        return http.build();
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
