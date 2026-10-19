package com.ingenieria.software1.service;

import com.ingenieria.software1.model.Urgencia;
import com.ingenieria.software1.model.Paciente;
import com.ingenieria.software1.model.Triage;
import com.ingenieria.software1.model.Empleado;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
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
     * Orden de las urgencias pendientes (F-01 SWR.2): primero las más graves (nivel descendente)
     * y, en caso de empate, por hora de reporte ascendente.
     */
    public static final Comparator<Urgencia> ORDEN_PRIORIDAD = Comparator
            .comparingInt((Urgencia u) -> u.getTriage() != null ? u.getTriage().getNivelPrioridad() : 0)
            .reversed()
            .thenComparing(Urgencia::getHoraReporte, Comparator.nullsLast(Comparator.<LocalDateTime>naturalOrder()));

    /**
     * Devuelve las urgencias PENDIENTES ordenadas según ORDEN_PRIORIDAD, seguidas del resto
     * de urgencias en el orden en que llegaron.
     */
    public List<Urgencia> pendientesPrimeroPorPrioridad(List<Urgencia> urgencias) {
        List<Urgencia> resultado = new ArrayList<>(urgencias.stream()
                .filter(u -> "PENDIENTE".equals(u.getEstado()))
                .sorted(ORDEN_PRIORIDAD)
                .collect(Collectors.toList()));
        resultado.addAll(urgencias.stream()
                .filter(u -> !"PENDIENTE".equals(u.getEstado()))
                .collect(Collectors.toList()));
        return resultado;
    }

    /**
     * Registra un nuevo paciente si no existe, luego crea la urgencia en MariaDB.
     * @return la urgencia recién registrada (estado PENDIENTE), o null si no se pudo guardar
     *         (nivel de triaje fuera de 1–5 o error al persistir).
     */
    public Urgencia registrarUrgenciaReal(Paciente paciente, String sintomas, String ubicacion, int nivelTriage, String signosVitales) {
        if (ValidadorDatos.validarNivelTriage(nivelTriage) != null) {
            return null;
        }

        Paciente existente = pacienteDAO.buscarPorId(paciente.getId());
        if (existente == null) {
            paciente.setHistoriaClinica("");
            if (!pacienteDAO.crear(paciente)) {
                return null;
            }
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

        // El identificador se genera al azar: si coincide con uno existente, se reintenta con otro.
        for (int intento = 0; intento < 5; intento++) {
            if (urgenciaDAO.crear(nueva)) {
                return nueva;
            }
            nueva.setId(new Urgencia().getId());
        }
        return null;
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
