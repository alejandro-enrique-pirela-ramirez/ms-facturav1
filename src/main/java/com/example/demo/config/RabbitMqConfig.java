package com.example.demo.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    public static final String EXCHANGE = "facturador.factura.exchange";

    public static final String QUEUE_PENDIENTE = "facturador.factura.pendiente.queue";
    public static final String RK_PENDIENTE = "factura.pendiente";

    public static final String QUEUE_EMITIR = "facturador.factura.emitir.queue";
    public static final String RK_EMITIR = "factura.emitir";

    public static final String RK_ESTADO = "factura.estado";

    @Bean
    public DirectExchange facturaExchange() {
        return new DirectExchange(EXCHANGE);
    }

    @Bean
    public Queue facturaPendienteQueue() {
        return new Queue(QUEUE_PENDIENTE, true);
    }

    @Bean
    public Binding facturaPendienteBinding(Queue facturaPendienteQueue, DirectExchange facturaExchange) {
        return BindingBuilder.bind(facturaPendienteQueue).to(facturaExchange).with(RK_PENDIENTE);
    }

    @Bean
    public Queue facturaEmitirQueue() {
        return new Queue(QUEUE_EMITIR, true);
    }

    @Bean
    public Binding facturaEmitirBinding(Queue facturaEmitirQueue, DirectExchange facturaExchange) {
        return BindingBuilder.bind(facturaEmitirQueue).to(facturaExchange).with(RK_EMITIR);
    }
}