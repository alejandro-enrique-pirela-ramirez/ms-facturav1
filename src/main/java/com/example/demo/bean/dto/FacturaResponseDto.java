package com.example.demo.bean.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.example.demo.bean.enums.EstadoFactura;

import lombok.Data;

@Data
public class FacturaResponseDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idFactura;
    private Long idCliente;
    private String serie;
    private Integer numero;
    private String fechaEmision;
    private String clienteDenominacion;
    private String clienteNumeroDocumento;
    private String moneda;
    private BigDecimal subtotal;
    private BigDecimal igv;
    private BigDecimal total;
    private EstadoFactura estado;
    private String documentId;
    private String hashCpe;
    private String urlXml;
    private String urlCdr;
    private String urlPdf;
    private String mensajeSunat;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaRespuesta;
    private List<DetalleResponseDto> detalles;
}