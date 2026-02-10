package com.hvitops.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class HvitopsGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(HvitopsGatewayApplication.class, args);
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("appointments", r -> r
                        .path("/appointments/**")
                        .uri("http://hvitops-appointments:8081"))
                .route("laboratory-tests", r -> r
                        .path("/laboratory-tests/**")
                        .uri("http://hvitops-laboratory-tests:8082"))
                .route("records", r -> r
                        .path("/records/**")
                        .uri("http://hvitops-records:8083"))
                .build();
    }
}
