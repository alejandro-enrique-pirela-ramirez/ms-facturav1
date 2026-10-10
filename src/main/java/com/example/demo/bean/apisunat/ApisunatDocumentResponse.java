package com.example.demo.bean.apisunat;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ApisunatDocumentResponse(
        String status,          // ACEPTADO, RECHAZADO, EXCEPCION (o PENDIENTE)
        String fileName,
        Long issueTime,
        Long responseTime,      
        String xml,
        String cdr,
        List<Object> faults,
        List<Object> notes) {
}