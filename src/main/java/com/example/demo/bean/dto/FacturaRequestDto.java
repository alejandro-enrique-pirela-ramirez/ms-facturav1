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

    private Long idCliente;         
    private String serie;          
    private Integer numero;         
    private String fechaEmision;    
    private String moneda;          
    private String tipoOperacion;   
    private List<ItemFacturaDto> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemFacturaDto implements Serializable {
        private static final long serialVersionUID = 1L;
        private Long idProducto;    
        private Integer cantidad;
    }
}