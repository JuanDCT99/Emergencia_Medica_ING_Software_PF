package com.ingenieria.software1.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Triage {
    private String id;
    private int nivelPrioridad; // 1 a 5 (según la Regla de Negocio RN-001)
    private String observaciones;
    private String signosVitales; // Presión, Ritmo Cardiaco, etc.
    private LocalDateTime horaEvaluacion;
}
