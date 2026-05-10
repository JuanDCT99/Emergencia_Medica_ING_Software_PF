package com.ingenieria.software1.service;

import com.ingenieria.software1.model.Urgencia;
import com.ingenieria.software1.model.Paciente;
import com.ingenieria.software1.model.Triage;
import com.ingenieria.software1.model.Empleado;

import java.util.List;
import java.util.stream.Collectors;

public class EmergenciaService {
    private static EmergenciaService instance;
    private final UrgenciaDAO urgenciaDAO;
    private final PacienteDAO pacienteDAO;

    private EmergenciaService() {
        this.urgenciaDAO = new UrgenciaDAO();
        this.pacienteDAO = new PacienteDAO();
    }

    public static EmergenciaService getInstance() {
        if (instance == null) {
            instance = new EmergenciaService();
        }
        return instance;
    }

    /**
     * Registra un nuevo paciente si no existe, luego crea la urgencia en MariaDB.
     */
    public void registrarUrgenciaReal(Paciente paciente, String sintomas, String ubicacion, int nivelTriage, String signosVitales) {
        Paciente existente = pacienteDAO.buscarPorId(paciente.getId());
        if (existente == null) {
            paciente.setHistoriaClinica("");
            pacienteDAO.crear(paciente);
        }

        Urgencia nueva = new Urgencia();
        nueva.setPaciente(paciente);
        nueva.setSintomatologia(sintomas);
        nueva.setUbicacion(ubicacion);
        nueva.setEstado("PENDIENTE");

        Triage triage = new Triage();
        triage.setId(java.util.UUID.randomUUID().toString().substring(0, 8));
        triage.setNivelPrioridad(nivelTriage);
        triage.setSignosVitales(signosVitales);
        triage.setHoraEvaluacion(java.time.LocalDateTime.now());

        nueva.setTriage(triage);

        urgenciaDAO.crear(nueva);
    }

    public List<Urgencia> getUrgenciasPendientes() {
        return urgenciaDAO.obtenerPorEstado("PENDIENTE");
    }

    public List<Urgencia> getTodasLasUrgencias() {
        return urgenciaDAO.obtenerTodas();
    }

    /**
     * Asigna personal y cambia el estado a EN_CURSO en MariaDB.
     */
    public void despacharUrgencia(String idUrgencia, Empleado asignado) {
        urgenciaDAO.actualizarEstado(idUrgencia, "EN_CURSO", asignado.getId());
    }

    /**
     * Cambia el estado a FINALIZADO en MariaDB.
     */
    public void finalizarUrgencia(String idUrgencia) {
        urgenciaDAO.actualizarEstado(idUrgencia, "FINALIZADO", null);
    }
}
