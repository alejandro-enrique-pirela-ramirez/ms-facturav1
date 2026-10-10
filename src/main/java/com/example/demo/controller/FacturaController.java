package com.example.demo.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.bean.dto.FacturaRequestDto;
import com.example.demo.bean.dto.FacturaResponseDto;
import com.example.demo.bean.enums.EstadoFactura;
import com.example.demo.useCase.FacturaUseCase;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/facturas")
@RequiredArgsConstructor
public class FacturaController {

    private final FacturaUseCase facturaUseCase;

    @PostMapping("/emitir")
    public ResponseEntity<FacturaResponseDto> emitir(@RequestBody FacturaRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(facturaUseCase.emitir(request));
    }

    @PostMapping("/{id}/actualizar")
    public ResponseEntity<FacturaResponseDto> actualizar(@PathVariable Long id) {
        return ResponseEntity.ok(facturaUseCase.actualizar(id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FacturaResponseDto> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(facturaUseCase.buscarPorId(id));
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<FacturaResponseDto>> porEstado(@PathVariable EstadoFactura estado) {
        return ResponseEntity.ok(facturaUseCase.listarPorEstado(estado));
    }

    @GetMapping("/cliente/{idCliente}")
    public ResponseEntity<List<FacturaResponseDto>> porCliente(@PathVariable Long idCliente) {
        return ResponseEntity.ok(facturaUseCase.listarPorCliente(idCliente));
    }
}