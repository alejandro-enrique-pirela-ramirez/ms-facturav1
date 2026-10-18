package com.example.demo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.example.demo.model.FacturaService;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

class MsFacturaFacturadorElectronicoApplicationTests {

    @Test
    void startsWithRemoteConfiguration() throws IOException {
        List<String> requests = new CopyOnWriteArrayList<>();
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        try (var input = getClass().getResourceAsStream("/config-server-response.json")) {
            assertThat(input).isNotNull();
            byte[] response = input.readAllBytes();
            server.createContext("/", exchange -> {
                requests.add(exchange.getRequestURI().getPath());
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, response.length);
                try (var output = exchange.getResponseBody()) {
                    output.write(response);
                }
            });
        }
        server.start();

        try (ConfigurableApplicationContext context = SpringApplication.run(
                MsFacturaFacturadorElectronicoApplication.class,
                "--spring.main.web-application-type=none",
                "--CONFIG_SERVER_URL=http://localhost:" + server.getAddress().getPort())) {
            assertThat(requests).contains("/ms-factura/default/main");
            assertThat(context.getEnvironment().getProperty("config.integration.message")).isEqualTo("desde-config-server");
            assertThat(context.getEnvironment().getProperty("spring.datasource.url")).startsWith("jdbc:h2:mem:");
            assertThat(context.getBean(FacturaService.class)).isNotNull();
        } finally {
            server.stop(0);
        }
    }

    @Test
    void failsWhenRequiredRemoteConfigurationIsMissing() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/", exchange -> {
            byte[] response = "{}".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(404, response.length);
            try (var output = exchange.getResponseBody()) {
                output.write(response);
            }
        });
        server.start();

        try {
            assertThatThrownBy(() -> {
                try (ConfigurableApplicationContext ignored = SpringApplication.run(
                        MsFacturaFacturadorElectronicoApplication.class,
                        "--spring.main.web-application-type=none",
                        "--CONFIG_SERVER_URL=http://localhost:" + server.getAddress().getPort())) {
                    // El arranque debe fallar antes de crear el contexto.
                }
            }).hasStackTraceContaining("Could not locate PropertySource");
        } finally {
            server.stop(0);
        }
    }
}
