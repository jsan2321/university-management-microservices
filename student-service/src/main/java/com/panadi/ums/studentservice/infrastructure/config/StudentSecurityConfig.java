package com.panadi.ums.studentservice.infrastructure.config;

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
class StudentSecurityConfig {
    @Bean
    SecurityFilterChain studentSecurityFilterChain(HttpSecurity http, Converter<Jwt, AbstractAuthenticationToken> converter) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .requestMatchers("/internal/**").hasAnyRole("INTERNAL", "PROVISIONER")
                        .requestMatchers("/api/v1/students/me").hasRole("STUDENT")
                        .requestMatchers(HttpMethod.POST, "/api/v1/students").denyAll()
                        .requestMatchers("/api/v1/students/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(converter)))
                .httpBasic(httpBasic -> httpBasic.disable());
        return http.build();
    }
}
