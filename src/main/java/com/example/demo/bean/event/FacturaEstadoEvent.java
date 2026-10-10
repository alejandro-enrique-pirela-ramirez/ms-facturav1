package com.example.demo.bean.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.demo.bean.enums.EstadoFactura;

public record FacturaEstadoEvent(
        Long idFactura,
        Long idCliente,
        String serie,
        Integer numero,
        BigDecimal total,
        EstadoFactura estado,
        String urlPdf,
        LocalDateTime fechaRespuesta) {
}