package com.example.demo.bean.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClienteDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer idCliente;

    private String tipoDocumento;

    private String numeroDocumento;

    private String nombre;

    private String direccion;

    private String correo;

    private String telefono;
}