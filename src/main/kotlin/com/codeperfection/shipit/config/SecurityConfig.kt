package com.codeperfection.shipit.config

import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfig {

    @Bean
    fun defaultSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            // Accept JWT tokens
            .oauth2ResourceServer {
                it.jwt {}
            }
            .csrf { it.disable() }
            .authorizeHttpRequests {
                it.requestMatchers(EndpointRequest.toAnyEndpoint())
                    .permitAll()
                    .anyRequest().authenticated()
            }
        return http.build()
    }
}
