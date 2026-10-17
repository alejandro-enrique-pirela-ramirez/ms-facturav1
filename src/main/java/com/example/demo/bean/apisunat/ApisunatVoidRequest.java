package com.example.demo.bean.apisunat;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApisunatVoidRequest(
        String personaId,
        String personaToken,
        String documentId,   // documentId de la factura a anular
        String reason) {     // motivo, 3 a 100 caracteres
}