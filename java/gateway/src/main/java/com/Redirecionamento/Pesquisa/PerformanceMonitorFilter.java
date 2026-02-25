package com.Redirecionamento.Pesquisa;

import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;
import org.springframework.web.reactive.function.client.WebClient;

import com.Redirecionamento.Pesquisa.tapyofdeta.Dados;

import reactor.core.publisher.Mono;

import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;
import java.time.Duration;


@Component
public class PerformanceMonitorFilter implements WebFilter {

    private final WebClient webClient;
    private final OperatingSystemMXBean osBean;

    public PerformanceMonitorFilter() {
        java.lang.management.OperatingSystemMXBean bean = ManagementFactory.getOperatingSystemMXBean();
        this.osBean = (bean instanceof OperatingSystemMXBean) ? (OperatingSystemMXBean) bean : null;

        // Configura um pool de conexões robusto para alta concorrência
        ConnectionProvider provider = ConnectionProvider.builder("metrics-pool")
                .maxConnections(1000)               // Permite mais conexões simultâneas
                .pendingAcquireMaxCount(200000)     // Aumenta drasticamente a fila de espera (buffer)
                .pendingAcquireTimeout(Duration.ofSeconds(180)) // Aumenta o tempo de espera para 3 minutos
                .build();

        this.webClient = WebClient.builder()
                .baseUrl("http://localhost:8002/sql")
                .clientConnector(new ReactorClientHttpConnector(HttpClient.create(provider)))
                .build();
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        // --- ANTES DA REQUISIÇÃO ---
        long startNano = System.nanoTime();
        long startCpuTime = (osBean != null) ? osBean.getProcessCpuTime() : 0;
        long[] memInfoBefore = getSystemMemory();
        long memBefore = memInfoBefore[0];

        return chain.filter(exchange)
                .then(Mono.defer(() -> recordMetrics(exchange, startNano, startCpuTime, memBefore)))
                .onErrorResume(e -> recordMetrics(exchange, startNano, startCpuTime, memBefore)
                        .then(Mono.error(e)));
    }

    private Mono<Void> recordMetrics(ServerWebExchange exchange, long startNano, long startCpuTime, long memBefore) {
        // --- DEPOIS DA REQUISIÇÃO ---
        long endNano = System.nanoTime();
        long endCpuTime = (osBean != null) ? osBean.getProcessCpuTime() : 0;
        long[] memInfoAfter = getSystemMemory();
        long memAfter = memInfoAfter[0];
        long memTotal = memInfoAfter[1];

        // CÁLCULO DOS DELTAS (DIFERENÇAS)
        long durationMs = (endNano - startNano) / 1_000_000;

        long durationNano = endNano - startNano;
        double cpuUsage = 0.0;
        if (durationNano > 0) {
            long cpuTimeUsed = endCpuTime - startCpuTime;
            cpuUsage = (double) cpuTimeUsed / durationNano * 100.0 / Runtime.getRuntime().availableProcessors();
        }

        long deltaMem = memAfter - memBefore; // Em Bytes

        double processCpuLoad = (osBean != null) ? osBean.getProcessCpuLoad() : 0.0;
        if (processCpuLoad < 0) processCpuLoad = 0.0;

        Dados dados = new Dados(
            String.valueOf(exchange.getRequest().getURI()),
            durationMs + " ms",
            String.format("%.6f%%", processCpuLoad * 100),
            String.format("%.6f%%", cpuUsage),  /// CPU
            (memAfter / (1024 * 1024)) + "MB / " + (memTotal / (1024 * 1024)) + "MB",
            deltaMem + " B"
        );

        return webClient
            .post()
            .bodyValue(dados)
            .retrieve()
            .bodyToMono(Void.class)
            .doOnError(e -> System.err.println("Falha ao enviar métrica: " + e.getMessage()))
            .onErrorResume(e -> Mono.empty());
    }

    private long[] getSystemMemory() {
        if (osBean != null) {
            long total = osBean.getTotalPhysicalMemorySize();
            long free = osBean.getFreePhysicalMemorySize();
            return new long[]{total - free, total};
        }
        return new long[]{Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory(), Runtime.getRuntime().maxMemory()};
    }
}