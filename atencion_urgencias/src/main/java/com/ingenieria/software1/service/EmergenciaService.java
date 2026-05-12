package com.ingenieria.software1.service;

import com.ingenieria.software1.model.Ambulancia;
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
    private final AmbulanciaService ambulanciaService;

    private EmergenciaService() {
        this.urgenciaDAO = new UrgenciaDAO();
        this.pacienteDAO = new PacienteDAO();
        this.ambulanciaService = AmbulanciaService.getInstance();
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
     * (Método original - sin ambulancia - para compatibilidad)
     */
    public void despacharUrgencia(String idUrgencia, Empleado asignado) {
        urgenciaDAO.actualizarEstado(idUrgencia, "EN_CURSO", asignado.getId());
    }

    /**
     * Asigna personal y ambulancia, cambia el estado a EN_CURSO.
     * (Opcion C - cambio automatico de estado de ambulancia)
     */
    public boolean despacharUrgenciaConAmbulancia(String idUrgencia, Empleado asignado, Ambulancia ambulancia) {
        if (ambulancia != null && ambulancia.getId() != null) {
            Ambulancia ambDisponible = ambulanciaService.obtenerPorId(ambulancia.getId());
            if (ambDisponible == null || !ambDisponible.estaDisponible()) {
                return false;
            }
            ambulanciaService.marcarComoEnUso(ambulancia.getId());
            urgenciaDAO.actualizarEstadoConAmbulancia(idUrgencia, "EN_CURSO", asignado.getId(), ambulancia.getId());
        } else {
            urgenciaDAO.actualizarEstado(idUrgencia, "EN_CURSO", asignado.getId());
        }
        return true;
    }

    /**
     * Cambia el estado a FINALIZADO en MariaDB.
     * (Método original - para compatibilidad)
     */
    public void finalizarUrgencia(String idUrgencia) {
        Urgencia urgencia = urgenciaDAO.buscarPorId(idUrgencia);
        if (urgencia != null && urgencia.getAmbulanciaAsignada() != null) {
            ambulanciaService.marcarComoDisponible(urgencia.getAmbulanciaAsignada().getId());
        }
        urgenciaDAO.actualizarEstado(idUrgencia, "FINALIZADO", null);
    }

    /**
     * Finaliza urgencia y libera ambulancia (Opción C)
     */
    public void finalizarUrgenciaConLiberacionAmbulancia(String idUrgencia) {
        Urgencia urgencia = urgenciaDAO.buscarPorId(idUrgencia);
        if (urgencia != null && urgencia.getAmbulanciaAsignada() != null) {
            ambulanciaService.marcarComoDisponible(urgencia.getAmbulanciaAsignada().getId());
        }
        urgenciaDAO.actualizarEstado(idUrgencia, "FINALIZADO", null);
    }
}
