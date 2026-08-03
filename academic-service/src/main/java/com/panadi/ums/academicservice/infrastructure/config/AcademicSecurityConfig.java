package com.panadi.ums.academicservice.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
class AcademicSecurityConfig {
    @Bean
    SecurityFilterChain academicSecurityFilterChain(HttpSecurity http, Converter<Jwt, AbstractAuthenticationToken> converter) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/internal/**").hasAnyRole("INTERNAL", "PROVISIONER")
                        .requestMatchers("/api/v1/academic/teachers/me").hasRole("TEACHER")
                        .requestMatchers("/api/v1/academic/teachers/me/sections").hasRole("TEACHER")
                        .requestMatchers(HttpMethod.POST, "/api/v1/academic/teachers").denyAll()
                        .requestMatchers("/api/v1/academic/teachers/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/academic/**").hasAnyRole("ADMIN", "TEACHER", "STUDENT")
                        .requestMatchers("/api/v1/academic/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(converter)))
                .httpBasic(httpBasic -> httpBasic.disable());
        return http.build();
    }
}
