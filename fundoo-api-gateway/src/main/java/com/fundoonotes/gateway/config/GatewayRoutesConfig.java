package com.fundoonotes.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRoutesConfig {

    @Bean
    public RouteLocator fundooRoutes(
            RouteLocatorBuilder builder,
            @Value("${fundoo.backend.url}")
            String backendUrl
    ) {

        return builder.routes()

                // UC17: Forward all Fundoo REST API
                // requests to the existing backend.
                .route("fundoo-api-route", route ->
                        route.path("/api/**")
                                .uri(backendUrl)
                )

                .build();
    }
}
