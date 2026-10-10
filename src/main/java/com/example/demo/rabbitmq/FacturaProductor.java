package com.example.demo.rabbitmq;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import com.example.demo.bean.dto.FacturaRequestDto;
import com.example.demo.bean.entity.Factura;
import com.example.demo.bean.event.FacturaEstadoEvent;
import com.example.demo.bean.event.FacturaPendienteEvent;
import com.example.demo.config.RabbitMqConfig;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class FacturaProductor {

    private final RabbitTemplate rabbitTemplate;

    public void publicarEmision(FacturaRequestDto request) {
        rabbitTemplate.convertAndSend(RabbitMqConfig.EXCHANGE, RabbitMqConfig.RK_EMITIR, request);
        log.info("factura encolada para emitir: cliente {}", request.getIdCliente());
    }

    public void publicarPendiente(Long idFactura) {
        FacturaPendienteEvent event = new FacturaPendienteEvent(idFactura);
        rabbitTemplate.convertAndSend(RabbitMqConfig.EXCHANGE, RabbitMqConfig.RK_PENDIENTE, event);
        log.info("factura pendiente publicada: {}", event);
    }

    public void publicarEstado(Factura f) {
        FacturaEstadoEvent event = new FacturaEstadoEvent(
                f.getIdFactura(), f.getIdCliente(), f.getSerie(), f.getNumero(),
                f.getTotal(), f.getEstado(), f.getUrlPdf(), f.getFechaRespuesta());
        rabbitTemplate.convertAndSend(RabbitMqConfig.EXCHANGE, RabbitMqConfig.RK_ESTADO, event);
        log.info("estado de factura publicado: {}", event);
    }
}