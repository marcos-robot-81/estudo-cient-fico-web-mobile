package com.Redirecionamento; 

import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
public class SpaWebFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // Lógica: Se não for API e não for arquivo (tem ponto), manda pro index.html
        if (!path.startsWith("/api") && !path.contains(".") && !path.equals("/")) {
            return chain.filter(exchange.mutate()
                .request(exchange.getRequest().mutate().path("/index.html").build())
                .build());
        }

        return chain.filter(exchange);
    }
}