package com.ingenieria.software1.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Empleado {
    private String id;
    private String nombreCompleto;
    private String usuario;
    private String contrasena;
    private RolEmpleado rol;
}
