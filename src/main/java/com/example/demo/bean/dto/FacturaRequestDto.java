package com.example.demo.bean.dto;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FacturaRequestDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idCliente;         // consulta a ms-cliente
    private String serie;           // "F001"
    private Integer numero;         // 123
    private String fechaEmision;    // "2025-10-08"
    private String moneda;          // "PEN"
    private String tipoOperacion;   // "0101"
    private List<ItemFacturaDto> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemFacturaDto implements Serializable {
        private static final long serialVersionUID = 1L;
        private Long idProducto;    // consulta a ms-producto
        private Integer cantidad;
    }
}