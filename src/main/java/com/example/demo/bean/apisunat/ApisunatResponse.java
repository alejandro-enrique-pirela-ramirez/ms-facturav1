package com.example.demo.bean.apisunat;

import lombok.Data;

@Data
public class ApisunatResponse {
    private Boolean success;
    private String message;
    private Payload payload;

    @Data
    public static class Payload {
        private String estado;   // ACEPTADO, RECHAZADO, PENDIENTE
        private String hash;
        private String xml;
        private String cdr;
        private String pdf;
    }
}