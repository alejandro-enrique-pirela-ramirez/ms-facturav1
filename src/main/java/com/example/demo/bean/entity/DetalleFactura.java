package com.example.demo.bean.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "detalle_factura")
@Getter
@Setter
public class DetalleFactura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle")
    private Long idDetalle;

    @ManyToOne
    @JoinColumn(name = "id_factura", nullable = false)
    private Factura factura;

    @Column(name = "id_producto")
    private Long idProducto;

    @Column(name = "codigo_producto")
    private String codigoProducto;

    private String descripcion;

    @Column(name = "unidad_medida")
    private String unidadMedida;

    private BigDecimal cantidad;

    @Column(name = "valor_unitario", precision = 14, scale = 4)
    private BigDecimal valorUnitario;

    @Column(name = "porcentaje_igv")
    private BigDecimal porcentajeIgv;

    private BigDecimal igv;
    private BigDecimal total;

    
}