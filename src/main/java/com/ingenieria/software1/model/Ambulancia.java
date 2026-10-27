package com.ingenieria.software1.model;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ambulancia {
    private String id;
    private String placa;
    private String estado;
    private String modelo;
    private Integer kilometraje;
    private LocalDate ultimaRevision;
    private LocalDate fechaAlta;
    private String observaciones;

    public static final String ESTADO_DISPONIBLE = "DISPONIBLE";
    public static final String ESTADO_EN_USO = "EN_USO";
    public static final String ESTADO_FUERA_SERVICIO = "FUERA_SERVICIO";

    public Ambulancia(String id, String placa) {
        this.id = id;
        this.placa = placa;
        this.estado = ESTADO_DISPONIBLE;
        this.kilometraje = 0;
        this.fechaAlta = LocalDate.now();
    }

    public boolean estaDisponible() {
        return ESTADO_DISPONIBLE.equals(this.estado);
    }

    public boolean estaEnUso() {
        return ESTADO_EN_USO.equals(this.estado);
    }

    public boolean estaFueraDeServicio() {
        return ESTADO_FUERA_SERVICIO.equals(this.estado);
    }
}