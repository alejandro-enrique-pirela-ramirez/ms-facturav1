package com.example.demo.bean.apisunat;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

/** Body de POST /personas/v1/sendBill (personaId y personaToken van en el body, no en headers). */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApisunatSendRequest(
        String personaId,
        String personaToken,
        String fileName,                    // RUC-01-F001-00000001
        Map<String, Object> documentBody,   // UBL 2.1 en JSON
        String customerEmail) {
}