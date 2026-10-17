package com.example.demo.controller;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.bean.dto.FacturaRequestDto;
import com.example.demo.bean.dto.FacturaResponseDto;
import com.example.demo.bean.enums.EstadoFactura;
import com.example.demo.rabbitmq.FacturaProductor;
import com.example.demo.useCase.FacturaUseCase;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/facturas")
@RequiredArgsConstructor
public class FacturaController {

    private final FacturaUseCase facturaUseCase;
    private final FacturaProductor productor;

    @PostMapping("/emitir")
    public ResponseEntity<Map<String, Object>> emitir(@RequestBody FacturaRequestDto request) {
        validar(request, 1);
        productor.publicarEmision(request);
        return ResponseEntity.accepted().body(Map.of(
                "encolados", 1,
                "mensaje", "Se procesará en segundo plano"));
    }

    @PostMapping("/lote")
    public ResponseEntity<Map<String, Object>> emitirLote(@RequestBody List<FacturaRequestDto> requests) {
        if (requests == null || requests.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El lote no puede estar vacío");
        }
        // se valida todo el lote antes de encolar: o entra completo o no entra nada
        for (int i = 0; i < requests.size(); i++) {
            validar(requests.get(i), i + 1);
        }
        requests.forEach(productor::publicarEmision);
        return ResponseEntity.accepted().body(Map.of(
                "encolados", requests.size(),
                "mensaje", "Se procesarán en segundo plano, una por una y en orden"));
    }

    @PostMapping("/{id}/actualizar")
    public ResponseEntity<FacturaResponseDto> actualizar(@PathVariable Long id) {
        return ResponseEntity.ok(facturaUseCase.actualizar(id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FacturaResponseDto> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(facturaUseCase.buscarPorId(id));
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> descargarPdf(@PathVariable Long id) {
        FacturaResponseDto f = facturaUseCase.buscarPorId(id);
        if (f.getUrlPdf() == null || f.getUrlPdf().isBlank()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "La factura no tiene PDF disponible");
        }

        byte[] pdf;
        try (InputStream in = URI.create(f.getUrlPdf()).toURL().openStream()) {
            pdf = in.readAllBytes();
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "No se pudo descargar el PDF desde APISUNAT");
        }

        String nombre = f.getSerie() + "-" + f.getNumero() + ".pdf";
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nombre + "\"")
                .body(pdf);
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<FacturaResponseDto>> porEstado(@PathVariable EstadoFactura estado) {
        return ResponseEntity.ok(facturaUseCase.listarPorEstado(estado));
    }

    @PostMapping("/{id}/anular")
    public ResponseEntity<FacturaResponseDto> anular(@PathVariable Long id,
                                                     @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(facturaUseCase.anular(id, body.get("motivo")));
    }

    @GetMapping("/cliente/{idCliente}")
    public ResponseEntity<List<FacturaResponseDto>> porCliente(@PathVariable Long idCliente) {
        return ResponseEntity.ok(facturaUseCase.listarPorCliente(idCliente));
    }

    private static void validar(FacturaRequestDto r, int posicion) {
        if (r == null || r.getIdCliente() == null) {
            throw error(posicion, "falta idCliente");
        }
        if (r.getSerie() == null || r.getSerie().isBlank()) {
            throw error(posicion, "falta serie");
        }
        if (r.getItems() == null || r.getItems().isEmpty()) {
            throw error(posicion, "debe tener al menos un item");
        }
        for (FacturaRequestDto.ItemFacturaDto item : r.getItems()) {
            if (item.getIdProducto() == null || item.getCantidad() == null || item.getCantidad() <= 0) {
                throw error(posicion, "item inválido (idProducto y cantidad > 0)");
            }
        }
    }

    private static ResponseStatusException error(int posicion, String msg) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Factura #" + posicion + ": " + msg);
    }
}