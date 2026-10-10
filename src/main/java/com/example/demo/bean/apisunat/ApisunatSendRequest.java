package com.example.demo.bean.apisunat;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;


@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApisunatSendRequest(
        String personaId,
        String personaToken,
        String fileName,                    
        Map<String, Object> documentBody, 
        String customerEmail) {
}