package com.fundoonotes.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;

import org.springframework.stereotype.Component;

import reactor.core.publisher.Mono;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class GatewayRequestLoggingFilter
        implements GlobalFilter, Ordered {

    private static final Logger log =
            LoggerFactory.getLogger(
                    GatewayRequestLoggingFilter.class
            );

    @Override
    public Mono<Void> filter(
            org.springframework.web.server.ServerWebExchange exchange,
            org.springframework.cloud.gateway.filter.GatewayFilterChain chain
    ) {

        // Generate a new correlation ID on the gateway.
        String requestId = UUID.randomUUID().toString();

        long startedAt = System.nanoTime();

        // Forward the generated request ID to the backend.
        // Do not read request bodies or Authorization headers.
        ServerHttpRequest request = exchange.getRequest()
                .mutate()
                .headers(headers ->
                        headers.set("X-Request-Id", requestId)
                )
                .build();

        var updatedExchange = exchange.mutate()
                .request(request)
                .build();

        return chain.filter(updatedExchange)
                .doFinally(signalType -> {

                    long durationMs =
                            TimeUnit.NANOSECONDS.toMillis(
                                    System.nanoTime() - startedAt
                            );

                    var status =
                            updatedExchange.getResponse()
                                    .getStatusCode();

                    log.info(
                            "UC17 Gateway | requestId={} | " +
                                    "method={} | path={} | status={} | {}ms",
                            requestId,
                            request.getMethod(),
                            request.getPath().value(),
                            status,
                            durationMs
                    );
                });
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
