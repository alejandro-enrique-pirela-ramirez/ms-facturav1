package com.example.demo.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.example.demo.bean.apisunat.ApisunatDocumentResponse;
import com.example.demo.bean.apisunat.ApisunatSendRequest;
import com.example.demo.bean.apisunat.ApisunatSendResponse;
import com.example.demo.bean.apisunat.ApisunatVoidRequest;

@FeignClient(name = "apisunat-client", url = "${apisunat.url}")
public interface ApisunatClient {

    @PostMapping("/personas/v1/sendBill")
    ApisunatSendResponse sendBill(@RequestBody ApisunatSendRequest request);

    @GetMapping("/documents/{documentId}/getById")
    ApisunatDocumentResponse getById(@PathVariable("documentId") String documentId);
    
    @PostMapping("/personas/v1/voidBill")
    ApisunatSendResponse voidBill(@RequestBody ApisunatVoidRequest request);
}