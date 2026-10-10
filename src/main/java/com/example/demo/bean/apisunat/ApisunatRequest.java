package com.example.demo.bean.apisunat;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class ApisunatRequest {

    private String documento = "factura";
    private String serie;
    private Integer numero;

    @JsonProperty("fecha_de_emision")
    private String fechaDeEmision;

    private String moneda;

    @JsonProperty("tipo_operacion")
    private String tipoOperacion;

    @JsonProperty("tipo_de_documento")
    private String tipoDeDocumento = "01"; // 01 factura, 03 boleta

    @JsonProperty("cliente_tipo_de_documento")
    private String clienteTipoDeDocumento;

    @JsonProperty("cliente_numero_de_documento")
    private String clienteNumeroDeDocumento;

    @JsonProperty("cliente_denominacion")
    private String clienteDenominacion;

    @JsonProperty("cliente_direccion")
    private String clienteDireccion;

    @JsonProperty("cliente_email")
    private String clienteEmail;

    @JsonProperty("emisor_ruc")
    private String emisorRuc;

    @JsonProperty("emisor_razon_social")
    private String emisorRazonSocial;

    @JsonProperty("emisor_nombre_comercial")
    private String emisorNombreComercial;

    @JsonProperty("emisor_direccion")
    private String emisorDireccion;

    @JsonProperty("emisor_ubigeo")
    private String emisorUbigeo;

    private List<Item> items;
    private String total;

    @Data
    public static class Item {
        @JsonProperty("unidad_de_medida")
        private String unidadDeMedida;

        private String codigo;
        private String descripcion;
        private String cantidad;

        @JsonProperty("valor_unitario")
        private String valorUnitario;

        @JsonProperty("porcentaje_igv")
        private String porcentajeIgv;

        @JsonProperty("codigo_tipo_afectacion_igv")
        private String codigoTipoAfectacionIgv;

        private String total;
    }
}