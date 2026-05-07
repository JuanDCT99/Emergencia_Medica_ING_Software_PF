package com.ingenieria.software1.service;

import com.ingenieria.software1.model.Urgencia;
import com.ingenieria.software1.model.Paciente;
import com.ingenieria.software1.model.Triage;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UrgenciaDAO {
    private final DatabaseService dbService;

    public UrgenciaDAO() {
        this.dbService = DatabaseService.getInstance();
    }

    public boolean crear(Urgencia urgencia) {
        String sql = "INSERT INTO urgencias (id, paciente_id, empleado_id, nivel_triage, sintomas, signos_vitales, estado, fecha_registro) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dbService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, urgencia.getId());
            stmt.setString(2, urgencia.getPaciente() != null ? urgencia.getPaciente().getId() : null);
            stmt.setString(3, urgencia.getPersonalAsignado() != null ? urgencia.getPersonalAsignado().getId() : null);
            stmt.setInt(4, urgencia.getTriage() != null ? urgencia.getTriage().getNivelPrioridad() : 0);
            stmt.setString(5, urgencia.getSintomatologia());
            stmt.setString(6, urgencia.getTriage() != null ? urgencia.getTriage().getSignosVitales() : null);
            stmt.setString(7, urgencia.getEstado());
            stmt.setTimestamp(8, urgencia.getHoraReporte() != null ? Timestamp.valueOf(urgencia.getHoraReporte()) : Timestamp.valueOf(LocalDateTime.now()));
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error creando urgencia: " + e.getMessage());
            return false;
        }
    }

    public Urgencia buscarPorId(String id) {
        String sql = "SELECT u.*, p.nombre_completo as paciente_nombre, e.nombre_completo as empleado_nombre FROM urgencias u LEFT JOIN pacientes p ON u.paciente_id = p.id LEFT JOIN empleados e ON u.empleado_id = e.id WHERE u.id = ?";
        try (Connection conn = dbService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToUrgencia(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error buscando urgencia: " + e.getMessage());
        }
        return null;
    }

    public List<Urgencia> obtenerTodas() {
        List<Urgencia> urgencias = new ArrayList<>();
        String sql = "SELECT u.*, p.nombre_completo as paciente_nombre, e.nombre_completo as empleado_nombre FROM urgencias u LEFT JOIN pacientes p ON u.paciente_id = p.id LEFT JOIN empleados e ON u.empleado_id = e.id ORDER BY u.fecha_registro DESC";
        try (Connection conn = dbService.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                urgencias.add(mapResultSetToUrgencia(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error obteniendo urgencias: " + e.getMessage());
        }
        return urgencias;
    }

    public List<Urgencia> obtenerPorEstado(String estado) {
        List<Urgencia> urgencias = new ArrayList<>();
        String sql = "SELECT u.*, p.nombre_completo as paciente_nombre, e.nombre_completo as empleado_nombre FROM urgencias u LEFT JOIN pacientes p ON u.paciente_id = p.id LEFT JOIN empleados e ON u.empleado_id = e.id WHERE u.estado = ? ORDER BY u.nivel_triage DESC, u.fecha_registro ASC";
        try (Connection conn = dbService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, estado);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                urgencias.add(mapResultSetToUrgencia(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error obteniendo urgencias por estado: " + e.getMessage());
        }
        return urgencias;
    }

    public boolean actualizarEstado(String id, String estado, String empleadoId) {
        String sql = "UPDATE urgencias SET estado = ?, empleado_id = ? WHERE id = ?";
        try (Connection conn = dbService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, estado);
            stmt.setString(2, empleadoId);
            stmt.setString(3, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error actualizando estado de urgencia: " + e.getMessage());
            return false;
        }
    }

    public boolean actualizar(String id, Urgencia urgencia) {
        String sql = "UPDATE urgencias SET paciente_id = ?, empleado_id = ?, nivel_triage = ?, sintomas = ?, signos_vitales = ?, estado = ? WHERE id = ?";
        try (Connection conn = dbService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, urgencia.getPaciente() != null ? urgencia.getPaciente().getId() : null);
            stmt.setString(2, urgencia.getPersonalAsignado() != null ? urgencia.getPersonalAsignado().getId() : null);
            stmt.setInt(3, urgencia.getTriage() != null ? urgencia.getTriage().getNivelPrioridad() : 0);
            stmt.setString(4, urgencia.getSintomatologia());
            stmt.setString(5, urgencia.getTriage() != null ? urgencia.getTriage().getSignosVitales() : null);
            stmt.setString(6, urgencia.getEstado());
            stmt.setString(7, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error actualizando urgencia: " + e.getMessage());
            return false;
        }
    }

    public boolean eliminar(String id) {
        String sql = "DELETE FROM urgencias WHERE id = ?";
        try (Connection conn = dbService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error eliminando urgencia: " + e.getMessage());
            return false;
        }
    }

    private Urgencia mapResultSetToUrgencia(ResultSet rs) throws SQLException {
        Urgencia urgencia = new Urgencia();
        urgencia.setId(rs.getString("id"));
        urgencia.setSintomatologia(rs.getString("sintomas"));
        urgencia.setEstado(rs.getString("estado"));
        
        Timestamp ts = rs.getTimestamp("fecha_registro");
        if (ts != null) {
            urgencia.setHoraReporte(ts.toLocalDateTime());
        }

        // Cargar paciente
        Paciente paciente = new Paciente();
        paciente.setId(rs.getString("paciente_id"));
        paciente.setNombreCompleto(rs.getString("paciente_nombre"));
        urgencia.setPaciente(paciente);

        // Cargar personal asignado (empleado)
        String empleadoId = rs.getString("empleado_id");
        if (empleadoId != null) {
            com.ingenieria.software1.model.Empleado personal = new com.ingenieria.software1.model.Empleado();
            personal.setId(empleadoId);
            personal.setNombreCompleto(rs.getString("empleado_nombre"));
            urgencia.setPersonalAsignado(personal);
        }

        // Cargar triage
        Triage triage = new Triage();
        triage.setNivelPrioridad(rs.getInt("nivel_triage"));
        triage.setSignosVitales(rs.getString("signos_vitales"));
        urgencia.setTriage(triage);

        return urgencia;
    }
}
