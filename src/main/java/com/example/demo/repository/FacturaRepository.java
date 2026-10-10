package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.bean.entity.Factura;
import com.example.demo.bean.enums.EstadoFactura;

public interface FacturaRepository extends JpaRepository<Factura, Long> {

    List<Factura> findByEstadoOrderByFechaCreacionDesc(EstadoFactura estado);

    List<Factura> findByIdClienteOrderByFechaCreacionDesc(Long idCliente);

    boolean existsBySerieAndNumero(String serie, Integer numero);

    Optional<Factura> findTopBySerieOrderByNumeroDesc(String serie);
}