package com.example.demo.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.example.demo.bean.dto.ClienteDto;

// AJUSTA el nombre (spring.application.name de tu ms-cliente) y la ruta
@FeignClient(name = "ms-cliente-facturador-electronico")
public interface ClienteClient {

    @GetMapping("/api/clientes/{id}")
    ClienteDto buscarPorId(@PathVariable("id") Long id);
}