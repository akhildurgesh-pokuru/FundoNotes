package com.fundoonotes.config;

import com.fundoonotes.repository.UserRepository;
import com.fundoonotes.security.JwtAuthenticationFilter;
import com.fundoonotes.security.TokenUtil;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            TokenUtil tokenUtil,
            UserRepository userRepository
    ) throws Exception {

        JwtAuthenticationFilter jwtFilter =
                new JwtAuthenticationFilter(
                        tokenUtil,
                        userRepository
                );

        http
                // Our REST API uses Bearer tokens rather than cookie sessions.
                .csrf(AbstractHttpConfigurer::disable)

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)

                .authorizeHttpRequests(auth -> auth

                        // Registration and login are public.
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/users/register",
                                "/api/users/login"
                        ).permitAll()

                        // All other endpoints require a valid JWT.
                        .anyRequest().authenticated()
                )

                .exceptionHandling(exceptions ->
                        exceptions.authenticationEntryPoint(
                                (request, response, exception) ->
                                        response.sendError(
                                                401,
                                                "Authentication required"
                                        )
                        )
                )

                // Validate JWTs before Spring's username/password filter.
                .addFilterBefore(
                        jwtFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}
