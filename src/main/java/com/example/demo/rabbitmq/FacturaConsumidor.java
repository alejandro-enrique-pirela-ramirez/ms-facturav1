package com.example.demo.rabbitmq;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.bean.dto.FacturaRequestDto;
import com.example.demo.bean.dto.FacturaResponseDto;
import com.example.demo.bean.enums.EstadoFactura;
import com.example.demo.bean.event.FacturaPendienteEvent;
import com.example.demo.config.RabbitMqConfig;
import com.example.demo.useCase.FacturaUseCase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class FacturaConsumidor {

    private final FacturaUseCase facturaUseCase;

    @RabbitListener(queues = RabbitMqConfig.QUEUE_EMITIR)
    public void emitirFactura(FacturaRequestDto request) {
        try {
            FacturaResponseDto dto = facturaUseCase.emitir(request);
            log.info("factura emitida {}-{} estado {}", dto.getSerie(), dto.getNumero(), dto.getEstado());
        } catch (ResponseStatusException e) {
        
            log.warn("factura del lote descartada (cliente {}): {} {}",
                    request.getIdCliente(), e.getStatusCode(), e.getReason());
        }
        
    }

    @RabbitListener(queues = RabbitMqConfig.QUEUE_PENDIENTE)
    public void consultarEstadoSunat(FacturaPendienteEvent event) {
        log.info("consultando estado SUNAT de factura {}", event.idFactura());
        FacturaResponseDto dto = facturaUseCase.actualizar(event.idFactura());

        if (dto.getEstado() == EstadoFactura.PENDIENTE) {
            throw new IllegalStateException("Factura " + event.idFactura() + " sigue PENDIENTE en SUNAT");
        }
        log.info("factura {} quedó en estado {}", event.idFactura(), dto.getEstado());
    }
}