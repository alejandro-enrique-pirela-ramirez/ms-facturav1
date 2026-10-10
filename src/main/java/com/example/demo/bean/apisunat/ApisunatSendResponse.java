package com.example.demo.bean.apisunat;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ApisunatSendResponse(String status, String documentId, Object error) {
}