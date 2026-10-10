package com.example.demo.bean.dto;

import java.io.Serializable;
import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idProducto;

    private String codigoProducto;

    private String nombreProducto;

    private String descripcion;

    private String unidadMedida;

    private BigDecimal precio;

    private Integer stock;

    private Boolean activo;
}