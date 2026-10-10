package com.example.demo.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.bean.apisunat.ApisunatDocumentResponse;
import com.example.demo.bean.apisunat.ApisunatSendRequest;
import com.example.demo.bean.apisunat.ApisunatSendResponse;
import com.example.demo.bean.dto.ClienteDto;
import com.example.demo.bean.dto.DetalleResponseDto;
import com.example.demo.bean.dto.FacturaRequestDto;
import com.example.demo.bean.dto.FacturaResponseDto;
import com.example.demo.bean.dto.ProductoDto;
import com.example.demo.bean.entity.DetalleFactura;
import com.example.demo.bean.entity.Factura;
import com.example.demo.bean.enums.EstadoFactura;
import com.example.demo.client.ApisunatClient;
import com.example.demo.client.ClienteClient;
import com.example.demo.client.ProductoClient;
import com.example.demo.repository.FacturaRepository;
import com.example.demo.useCase.FacturaUseCase;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class FacturaService implements FacturaUseCase {

    private static final BigDecimal PORCENTAJE_IGV = new BigDecimal("18");
    private static final BigDecimal TASA_IGV = new BigDecimal("0.18");
    private static final ZoneId LIMA = ZoneId.of("America/Lima");

    private final FacturaRepository facturaRepository;
    private final ClienteClient clienteClient;
    private final ProductoClient productoClient;
    private final ApisunatClient apisunatClient;
    private final UblInvoiceBuilder ublBuilder;

    @Value("${apisunat.url}") private String apisunatUrl;
    @Value("${apisunat.persona-id}") private String personaId;
    @Value("${apisunat.persona-token}") private String personaToken;
    @Value("${emisor.ruc}") private String emisorRuc;


    @Override
    public FacturaResponseDto emitir(FacturaRequestDto req) {
        if (req.getItems() == null || req.getItems().isEmpty()) {
            throw error(HttpStatus.BAD_REQUEST, "La factura debe tener al menos un item");
        }

        ClienteDto cliente = obtenerCliente(req.getIdCliente());
        String tipoDoc = codigoTipoDocumento(cliente.getTipoDocumento());
        if (!"6".equals(tipoDoc)) {
            throw error(HttpStatus.UNPROCESSABLE_ENTITY, "Una factura requiere un cliente con RUC");
        }

        String serie = req.getSerie();
        Integer numero = req.getNumero() != null ? req.getNumero() : siguienteNumero(serie);
        if (facturaRepository.existsBySerieAndNumero(serie, numero)) {
            throw error(HttpStatus.CONFLICT, "Ya existe la factura " + serie + "-" + numero);
        }

        Factura f = new Factura();
        f.setSerie(serie);
        f.setNumero(numero);
        f.setFechaEmision(req.getFechaEmision() != null ? req.getFechaEmision() : LocalDate.now(LIMA).toString());
        f.setIdCliente(req.getIdCliente());
        f.setClienteTipoDocumento(tipoDoc);
        f.setClienteNumeroDocumento(cliente.getNumeroDocumento());
        f.setClienteDenominacion(cliente.getNombre());
        f.setMoneda(req.getMoneda() != null ? req.getMoneda() : "PEN");
        f.setTipoOperacion(req.getTipoOperacion() != null ? req.getTipoOperacion() : "0101");

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal igv = BigDecimal.ZERO;

        for (FacturaRequestDto.ItemFacturaDto item : req.getItems()) {
            ProductoDto p = obtenerProducto(item.getIdProducto());
            if (!Boolean.TRUE.equals(p.getActivo())) {
                throw error(HttpStatus.UNPROCESSABLE_ENTITY, "Producto inactivo: " + p.getNombreProducto());
            }
            if (p.getStock() != null && p.getStock() < item.getCantidad()) {
                throw error(HttpStatus.UNPROCESSABLE_ENTITY, "Stock insuficiente: " + p.getNombreProducto());
            }

            BigDecimal cantidad = BigDecimal.valueOf(item.getCantidad());
            BigDecimal valorVenta = money(cantidad.multiply(p.getPrecio()));   // precio SIN IGV
            BigDecimal igvLinea = money(valorVenta.multiply(TASA_IGV));

            DetalleFactura d = new DetalleFactura();
            d.setIdProducto(p.getIdProducto());
            d.setCodigoProducto(p.getCodigoProducto());
            d.setDescripcion(p.getNombreProducto());
            d.setUnidadMedida(p.getUnidadMedida());
            d.setCantidad(cantidad);
            d.setValorUnitario(p.getPrecio());
            d.setPorcentajeIgv(PORCENTAJE_IGV);
            d.setIgv(igvLinea);
            d.setTotal(valorVenta.add(igvLinea));
            f.addDetalle(d);

            subtotal = subtotal.add(valorVenta);
            igv = igv.add(igvLinea);
        }
        f.setSubtotal(subtotal);
        f.setIgv(igv);
        f.setTotal(subtotal.add(igv));

        // 1) Se guarda PENDIENTE antes de llamar a APISUNAT: nunca se pierde el registro
        f = facturaRepository.save(f);

        // 2) Se envía a APISUNAT
        enviar(f, cliente);

        // 3) SUNAT responde en segundos: se consulta unas veces; si no, queda PENDIENTE
        //    y el front puede llamar a POST /api/facturas/{id}/actualizar
        for (int i = 0; i < 3 && f.getEstado() == EstadoFactura.PENDIENTE && f.getDocumentId() != null; i++) {
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
            f = refrescar(f);
        }
        return toDto(f);
    }

    // ------------------------------------------------------------- actualizar
    @Override
    public FacturaResponseDto actualizar(Long id) {
        Factura f = obtener(id);
        if (f.getEstado() == EstadoFactura.PENDIENTE) {
            if (f.getDocumentId() == null) {                 // el envío anterior falló: se reintenta
                enviar(f, obtenerCliente(f.getIdCliente()));
            }
            f = refrescar(f);
        }
        return toDto(f);
    }

    // ----------------------------------------------------------- consultas
    @Override
    public FacturaResponseDto buscarPorId(Long id) {
        return toDto(obtener(id));
    }

    @Override
    public List<FacturaResponseDto> listarPorEstado(EstadoFactura estado) {
        return facturaRepository.findByEstadoOrderByFechaCreacionDesc(estado).stream().map(this::toDto).toList();
    }

    @Override
    public List<FacturaResponseDto> listarPorCliente(Long idCliente) {
        return facturaRepository.findByIdClienteOrderByFechaCreacionDesc(idCliente).stream().map(this::toDto).toList();
    }

    // ------------------------------------------------------------ APISUNAT
    private void enviar(Factura f, ClienteDto cliente) {
        String fileName = fileName(f);
        try {
            ApisunatSendResponse resp = apisunatClient.sendBill(new ApisunatSendRequest(
                    personaId, personaToken, fileName,
                    ublBuilder.build(f, cliente.getDireccion()),
                    cliente.getCorreo()));

            if ("PENDIENTE".equals(resp.status())) {
                f.setDocumentId(resp.documentId());
                f.setUrlPdf(apisunatUrl + "/documents/" + resp.documentId() + "/getPDF/A4/" + fileName + ".pdf");
                f.setMensajeSunat("Enviado a APISUNAT, esperando respuesta de SUNAT");
            } else {
                f.setEstado(EstadoFactura.RECHAZADO);
                f.setMensajeSunat(truncar("APISUNAT: " + resp.error()));
            }
        } catch (FeignException e) {
            if (e.status() >= 400 && e.status() < 500) {
                f.setEstado(EstadoFactura.RECHAZADO);
            }                                                 // 5xx/timeout: queda PENDIENTE y se puede reintentar
            f.setMensajeSunat(truncar("APISUNAT " + e.status() + ": " + e.contentUTF8()));
            log.warn("sendBill falló para {}: {}", fileName, e.getMessage());
        }
        facturaRepository.save(f);
    }

    private Factura refrescar(Factura f) {
        if (f.getEstado() != EstadoFactura.PENDIENTE || f.getDocumentId() == null) {
            return f;
        }
        ApisunatDocumentResponse doc;
        try {
            doc = apisunatClient.getById(f.getDocumentId());
        } catch (FeignException e) {
            log.warn("getById falló para {}: {}", f.getDocumentId(), e.getMessage());
            return f;
        }

        String status = doc.status();
        if ("ACEPTADO".equals(status)) {
            f.setEstado(EstadoFactura.ACEPTADO);
            f.setUrlXml(doc.xml());
            f.setUrlCdr(doc.cdr());
            f.setMensajeSunat(resumen(doc));
            f.setFechaRespuesta(fecha(doc.responseTime()));
            descontarStock(f);
        } else if ("RECHAZADO".equals(status) || "EXCEPCION".equals(status)) {
            f.setEstado(EstadoFactura.valueOf(status));
            f.setUrlXml(doc.xml());
            f.setUrlCdr(doc.cdr());
            f.setMensajeSunat(resumen(doc));
            f.setFechaRespuesta(fecha(doc.responseTime()));
        } else {
            return f;                                         // sigue pendiente
        }
        return facturaRepository.save(f);
    }

    /** El stock solo se descuenta cuando SUNAT acepta (se ejecuta una sola vez: PENDIENTE -> ACEPTADO). */
    private void descontarStock(Factura f) {
        String ref = f.getSerie() + "-" + f.getNumero();
        for (DetalleFactura d : f.getDetalles()) {
            try {
                productoClient.descontarStock(d.getIdProducto(), d.getCantidad().intValue(), ref);
            } catch (FeignException e) {
                log.error("No se pudo descontar stock del producto {} ({}): {}", d.getIdProducto(), ref, e.getMessage());
                f.setMensajeSunat(truncar(f.getMensajeSunat() + " | FALLÓ descontar stock del producto " + d.getIdProducto()));
            }
        }
    }

    // ------------------------------------------------------------- helpers
    private String fileName(Factura f) {
        return emisorRuc + "-01-" + f.getSerie() + "-" + String.format("%08d", f.getNumero());
    }

    private Integer siguienteNumero(String serie) {
        return facturaRepository.findTopBySerieOrderByNumeroDesc(serie).map(x -> x.getNumero() + 1).orElse(1);
    }

    private String codigoTipoDocumento(String tipo) {
        if (tipo == null) {
            throw error(HttpStatus.UNPROCESSABLE_ENTITY, "El cliente no tiene tipo de documento");
        }
        return switch (tipo.trim().toUpperCase()) {
            case "RUC", "6" -> "6";
            case "DNI", "1" -> "1";
            case "CE", "4" -> "4";
            case "PASAPORTE", "7" -> "7";
            default -> tipo;
        };
    }

    private ClienteDto obtenerCliente(Long id) {
        try {
            return clienteClient.buscarPorId(id);
        } catch (FeignException.NotFound e) {
            throw error(HttpStatus.NOT_FOUND, "Cliente no existe: " + id);
        }
    }

    private ProductoDto obtenerProducto(Long id) {
        try {
            return productoClient.buscarPorId(id);
        } catch (FeignException.NotFound e) {
            throw error(HttpStatus.NOT_FOUND, "Producto no existe: " + id);
        }
    }

    private Factura obtener(Long id) {
        return facturaRepository.findById(id)
                .orElseThrow(() -> error(HttpStatus.NOT_FOUND, "Factura no existe: " + id));
    }

    private static BigDecimal money(BigDecimal v) {
        return v.setScale(2, RoundingMode.HALF_UP);
    }

    private static LocalDateTime fecha(Long epochSeconds) {
        return epochSeconds == null ? LocalDateTime.now(LIMA)
                : LocalDateTime.ofInstant(Instant.ofEpochSecond(epochSeconds), LIMA);
    }

    private static String resumen(ApisunatDocumentResponse doc) {
        StringBuilder sb = new StringBuilder(doc.status());
        if (doc.faults() != null && !doc.faults().isEmpty()) sb.append(" | faults: ").append(doc.faults());
        if (doc.notes() != null && !doc.notes().isEmpty()) sb.append(" | notes: ").append(doc.notes());
        return truncar(sb.toString());
    }

    private static String truncar(String s) {
        return s == null || s.length() <= 1000 ? s : s.substring(0, 1000);
    }

    private static ResponseStatusException error(HttpStatus status, String msg) {
        return new ResponseStatusException(status, msg);
    }

    // -------------------------------------------------------------- mapeo
    private FacturaResponseDto toDto(Factura f) {
        FacturaResponseDto dto = new FacturaResponseDto();
        dto.setIdFactura(f.getIdFactura());
        dto.setIdCliente(f.getIdCliente());
        dto.setSerie(f.getSerie());
        dto.setNumero(f.getNumero());
        dto.setFechaEmision(f.getFechaEmision());
        dto.setClienteDenominacion(f.getClienteDenominacion());
        dto.setClienteNumeroDocumento(f.getClienteNumeroDocumento());
        dto.setMoneda(f.getMoneda());
        dto.setSubtotal(f.getSubtotal());
        dto.setIgv(f.getIgv());
        dto.setTotal(f.getTotal());
        dto.setEstado(f.getEstado());
        dto.setDocumentId(f.getDocumentId());
        dto.setHashCpe(f.getHashCpe());
        dto.setUrlXml(f.getUrlXml());
        dto.setUrlCdr(f.getUrlCdr());
        dto.setUrlPdf(f.getUrlPdf());
        dto.setMensajeSunat(f.getMensajeSunat());
        dto.setFechaCreacion(f.getFechaCreacion());
        dto.setFechaRespuesta(f.getFechaRespuesta());
        dto.setDetalles(f.getDetalles().stream().map(d -> {
            DetalleResponseDto r = new DetalleResponseDto();
            r.setCodigoProducto(d.getCodigoProducto());
            r.setDescripcion(d.getDescripcion());
            r.setUnidadMedida(d.getUnidadMedida());
            r.setCantidad(d.getCantidad());
            r.setValorUnitario(d.getValorUnitario());
            r.setIgv(d.getIgv());
            r.setTotal(d.getTotal());
            return r;
        }).toList());
        return dto;
    }
}