package com.example.demo.bean.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.example.demo.bean.enums.EstadoFactura;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "factura", uniqueConstraints = @UniqueConstraint(columnNames = { "serie", "numero" }))
@Getter
@Setter
public class Factura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_factura")
    private Long idFactura;

    private String serie;
    private Integer numero;

    @Column(name = "fecha_emision")
    private String fechaEmision;

    @Column(name = "id_cliente")
    private Long idCliente;

    @Column(name = "cliente_tipo_documento")
    private String clienteTipoDocumento;      // catálogo 06 SUNAT: 6 = RUC, 1 = DNI

    @Column(name = "cliente_numero_documento")
    private String clienteNumeroDocumento;

    @Column(name = "cliente_denominacion")
    private String clienteDenominacion;

    private String moneda;

    @Column(name = "tipo_operacion")
    private String tipoOperacion;

    private BigDecimal subtotal;
    private BigDecimal igv;
    private BigDecimal total;

    @Enumerated(EnumType.STRING)
    private EstadoFactura estado = EstadoFactura.PENDIENTE;

    @Column(name = "document_id")
    private String documentId;              

    @Column(name = "hash_cpe")
    private String hashCpe;

    @Column(name = "url_xml", length = 500)
    private String urlXml;

    @Column(name = "url_cdr", length = 500)
    private String urlCdr;

    @Column(name = "url_pdf", length = 500)
    private String urlPdf;

    @Column(name = "mensaje_sunat", length = 1000)
    private String mensajeSunat;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Column(name = "fecha_respuesta")
    private LocalDateTime fechaRespuesta;

    @OneToMany(mappedBy = "factura", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<DetalleFactura> detalles = new ArrayList<>();

    public void addDetalle(DetalleFactura detalle) {
        detalle.setFactura(this);
        this.detalles.add(detalle);
    }
}