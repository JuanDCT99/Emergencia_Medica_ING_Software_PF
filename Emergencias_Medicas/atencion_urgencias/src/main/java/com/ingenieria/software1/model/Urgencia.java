package com.ingenieria.software1.model;

import java.time.LocalDateTime;
import java.util.Random;
import lombok.Data;

@Data
public class Urgencia {
    private String id;
    private Paciente paciente; // El paciente afectado
    private String sintomatologia; // Descripción de síntomas reportados
    private String ubicacion;
    private Triage triage; // Resultado de la evaluación inicial
    private LocalDateTime horaReporte;
    private String estado; // PENDIENTE, EN_CURSO, ATENDIDA
    private Empleado personalAsignado; // Personal que atiende la urgencia

    private static final String[] TIPOS_EMERGENCIA = {
        "Accidente de Tránsito", "Paro Cardiorrespiratorio", "Herida por Arma Blanca",
        "Caída de Altura", "Intoxicación Alimentaria", "Dificultad Respiratoria",
        "Fractura Expuesta", "Quemadura de 2do Grado"
    };

    private static final String[] UBICACIONES = {
        "Calle 10 con Carrera 15", "Barrio Granada", "Plaza Central",
        "Avenida Bolívar", "Sector Norte", "Zona Industrial", "Terminal de Transportes"
    };

    private static final Random random = new Random();

    public Urgencia() {
        this.id = "URG-" + (1000 + random.nextInt(9000));
        this.horaReporte = LocalDateTime.now();
        this.estado = "PENDIENTE";
    }
}
