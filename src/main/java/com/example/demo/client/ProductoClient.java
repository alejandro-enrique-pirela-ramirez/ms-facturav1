package com.example.demo.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.bean.dto.ProductoDto;

@FeignClient(name = "ms-producto-facturador-electronico")
public interface ProductoClient {

    @GetMapping("/api/productos/{id}")
    ProductoDto buscarPorId(@PathVariable("id") Long id);

    @PutMapping("/api/productos/{id}/descontar-stock")
    ProductoDto descontarStock(@PathVariable("id") Long id, @RequestParam("cantidad") Integer cantidad,
            @RequestParam(value = "referencia", required = false) String referencia);
}