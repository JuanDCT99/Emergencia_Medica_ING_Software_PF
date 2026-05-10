package com.ingenieria.software1.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Paciente {
    private String id; // Documento de identidad
    private String nombreCompleto;
    private int edad;
    private String genero;
    private String direccion;
    private String telefono;
    private String historiaClinica; // Resumen o ID de referencia
}
