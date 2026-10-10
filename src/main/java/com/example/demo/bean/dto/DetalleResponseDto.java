package com.example.demo.bean.dto;

import java.io.Serializable;
import java.math.BigDecimal;

import lombok.Data;
import lombok.RequiredArgsConstructor;
@Data
@RequiredArgsConstructor
public class DetalleResponseDto implements Serializable {

    private static final long serialVersionUID = 1L;

        private String codigoProducto;
        private String descripcion;
        private String unidadMedida;
        private BigDecimal cantidad;
        private BigDecimal valorUnitario;
        private BigDecimal igv;
        private BigDecimal total;
    }
