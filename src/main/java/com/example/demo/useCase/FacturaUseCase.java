package com.example.demo.useCase;

import java.util.List;

import com.example.demo.bean.dto.FacturaRequestDto;
import com.example.demo.bean.dto.FacturaResponseDto;
import com.example.demo.bean.enums.EstadoFactura;

public interface FacturaUseCase {

    FacturaResponseDto emitir(FacturaRequestDto request);

  
    FacturaResponseDto actualizar(Long id);

    FacturaResponseDto buscarPorId(Long id);

    List<FacturaResponseDto> listarPorEstado(EstadoFactura estado);

    List<FacturaResponseDto> listarPorCliente(Long idCliente);
}