package com.suraj.rag.embedding.config;

import com.suraj.rag.embedding.common.ApiPaths;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, SecurityProperties securityProperties)
            throws Exception {
        boolean jwtEnabled = securityProperties.enabled();
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(ApiPaths.ACTUATOR_HEALTH, ApiPaths.HEALTH, ApiPaths.OPENAPI, ApiPaths.SWAGGER_UI)
                        .permitAll()
                        .anyRequest()
                        .access((authentication, context) -> jwtEnabled
                                ? new AuthorizationDecision(
                                authentication.get() != null && authentication.get().isAuthenticated())
                                : new AuthorizationDecision(true)));
        if (jwtEnabled) {
            http.oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
        }
        return http.build();
    }
}
