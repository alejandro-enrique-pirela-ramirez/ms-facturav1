package com.example.demo.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.example.demo.bean.entity.DetalleFactura;
import com.example.demo.bean.entity.Factura;


@Component
public class UblInvoiceBuilder {

    private static final ZoneId LIMA = ZoneId.of("America/Lima");
    private static final DateTimeFormatter HMS = DateTimeFormatter.ofPattern("HH:mm:ss");

    private static final Map<String, String> UNIDADES_SUNAT = Map.of(
            "BALON", "NIU",
            "UNIDAD", "NIU",
            "M3", "MTQ",
            "LITRO", "LTR",
            "KG", "KGM",
            "CAJA", "BX");

    @Value("${emisor.ruc}") private String ruc;
    @Value("${emisor.razon-social}") private String razonSocial;
    @Value("${emisor.nombre-comercial}") private String nombreComercial;
    @Value("${emisor.direccion}") private String direccion;
    @Value("${emisor.ubigeo}") private String ubigeo;
    @Value("${emisor.departamento}") private String departamento;
    @Value("${emisor.provincia}") private String provincia;
    @Value("${emisor.distrito}") private String distrito;

    public Map<String, Object> build(Factura f, String clienteDireccion) {
        String cur = f.getMoneda();
        String id = f.getSerie() + "-" + String.format("%08d", f.getNumero());

        return o(
            "cbc:UBLVersionID", t("2.1"),
            "cbc:CustomizationID", t("2.0"),
            "cbc:ID", t(id),
            "cbc:IssueDate", t(f.getFechaEmision()),
            "cbc:IssueTime", t(LocalTime.now(LIMA).format(HMS)),
            "cbc:InvoiceTypeCode", o("_attributes", o("listID", f.getTipoOperacion()), "_text", "01"),
            "cbc:DocumentCurrencyCode", t(cur),
            "cac:AccountingSupplierParty", supplier(),
            "cac:AccountingCustomerParty", customer(f, clienteDireccion),
            "cac:PaymentTerms", o("cbc:ID", t("FormaPago"), "cbc:PaymentMeansID", t("Contado")),
            "cac:TaxTotal", o(
                "cbc:TaxAmount", amt(f.getIgv(), cur),
                "cac:TaxSubtotal", List.of(o(
                    "cbc:TaxableAmount", amt(f.getSubtotal(), cur),
                    "cbc:TaxAmount", amt(f.getIgv(), cur),
                    "cac:TaxCategory", o("cac:TaxScheme", igvScheme())))),
            "cac:LegalMonetaryTotal", o(
                "cbc:LineExtensionAmount", amt(f.getSubtotal(), cur),
                "cbc:TaxInclusiveAmount", amt(f.getTotal(), cur),
                "cbc:PayableAmount", amt(f.getTotal(), cur)),
            "cac:InvoiceLine", lines(f, cur));
    }

    private Map<String, Object> supplier() {
        return o("cac:Party", o(
            "cac:PartyIdentification", o("cbc:ID", o("_attributes", o("schemeID", "6"), "_text", ruc)),
            "cac:PartyName", o("cbc:Name", t(nombreComercial)),
            "cac:PartyLegalEntity", o(
                "cbc:RegistrationName", t(razonSocial),
                "cac:RegistrationAddress", o(
                    "cbc:ID", t(ubigeo),
                    "cbc:AddressTypeCode", t("0000"),
                    "cbc:CityName", t(provincia),
                    "cbc:CountrySubentity", t(departamento),
                    "cbc:District", t(distrito),
                    "cac:AddressLine", o("cbc:Line", t(direccion)),
                    "cac:Country", o("cbc:IdentificationCode", t("PE"))))));
    }

    private Map<String, Object> customer(Factura f, String dir) {
        Map<String, Object> legal = o("cbc:RegistrationName", t(f.getClienteDenominacion()));
        if (dir != null && !dir.isBlank()) {
            legal.put("cac:RegistrationAddress", o("cac:AddressLine", o("cbc:Line", t(dir))));
        }
        return o("cac:Party", o(
            "cac:PartyIdentification", o("cbc:ID", o(
                "_attributes", o("schemeID", f.getClienteTipoDocumento()),
                "_text", f.getClienteNumeroDocumento())),
            "cac:PartyLegalEntity", legal));
    }

    private static String codigoUnidadSunat(String unidad) {
        if (unidad == null || unidad.isBlank()) {
            return "NIU";
        }
        return UNIDADES_SUNAT.getOrDefault(unidad.trim().toUpperCase(), "NIU");
    }

    private List<Object> lines(Factura f, String cur) {
        List<Object> out = new ArrayList<>();
        int i = 1;
        for (DetalleFactura d : f.getDetalles()) {
            BigDecimal valorVenta = d.getTotal().subtract(d.getIgv());
            BigDecimal precioConIgv = d.getValorUnitario()
                    .multiply(BigDecimal.ONE.add(d.getPorcentajeIgv().movePointLeft(2)));
            String unidad = codigoUnidadSunat(d.getUnidadMedida());
            String codigo = d.getCodigoProducto() == null || d.getCodigoProducto().isBlank() ? "SIN-CODIGO" : d.getCodigoProducto();

            out.add(o(
                "cbc:ID", t(i++),
                "cbc:InvoicedQuantity", o("_attributes", o("unitCode", unidad),
                        "_text", d.getCantidad().stripTrailingZeros().toPlainString()),
                "cbc:LineExtensionAmount", amt(valorVenta, cur),
                "cac:PricingReference", o("cac:AlternativeConditionPrice", o(
                    "cbc:PriceAmount", amt(precioConIgv, cur),
                    "cbc:PriceTypeCode", t("01"))),
                "cac:TaxTotal", o(
                    "cbc:TaxAmount", amt(d.getIgv(), cur),
                    "cac:TaxSubtotal", List.of(o(
                        "cbc:TaxableAmount", amt(valorVenta, cur),
                        "cbc:TaxAmount", amt(d.getIgv(), cur),
                        "cac:TaxCategory", o(
                            "cbc:ID", t("S"),
                            "cbc:Percent", t(d.getPorcentajeIgv().stripTrailingZeros().toPlainString()),
                            "cbc:TaxExemptionReasonCode", t("10"),
                            "cac:TaxScheme", igvScheme())))),
                "cac:Item", o(
                    "cbc:Description", t(d.getDescripcion()),
                    "cac:SellersItemIdentification", o("cbc:ID", t(codigo))),
                "cac:Price", o("cbc:PriceAmount", amt(d.getValorUnitario(), cur))));
        }
        return out;
    }

    private Map<String, Object> igvScheme() {
        return o("cbc:ID", t("1000"), "cbc:Name", t("IGV"), "cbc:TaxTypeCode", t("VAT"));
    }

    // ---- helpers ----
    private static Map<String, Object> o(Object... kv) {
        Map<String, Object> m = new LinkedHashMap<>();
        for (int i = 0; i < kv.length; i += 2) {
            m.put((String) kv[i], kv[i + 1]);
        }
        return m;
    }

    private static Map<String, Object> t(Object v) {
        return o("_text", String.valueOf(v));
    }

    private static Map<String, Object> amt(BigDecimal v, String cur) {
        return o("_attributes", o("currencyID", cur),
                "_text", v.setScale(2, RoundingMode.HALF_UP).toPlainString());
    }
}