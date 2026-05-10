package com.ingenieria.software1.service;

import com.ingenieria.software1.model.Paciente;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PacienteDAO {
    private final DatabaseService dbService;

    public PacienteDAO() {
        this.dbService = DatabaseService.getInstance();
    }

    public boolean crear(Paciente paciente) {
        String sql = "INSERT INTO pacientes (id, nombre_completo, edad, genero, direccion, telefono, historia_clinica) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dbService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, paciente.getId());
            stmt.setString(2, paciente.getNombreCompleto());
            stmt.setInt(3, paciente.getEdad());
            stmt.setString(4, paciente.getGenero());
            stmt.setString(5, paciente.getDireccion());
            stmt.setString(6, paciente.getTelefono());
            stmt.setString(7, paciente.getHistoriaClinica());
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error creando paciente: " + e.getMessage());
            return false;
        }
    }

    public Paciente buscarPorId(String id) {
        String sql = "SELECT id, nombre_completo, edad, genero, direccion, telefono, historia_clinica FROM pacientes WHERE id = ?";
        try (Connection conn = dbService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToPaciente(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error buscando paciente: " + e.getMessage());
        }
        return null;
    }

    public List<Paciente> obtenerTodos() {
        List<Paciente> pacientes = new ArrayList<>();
        String sql = "SELECT id, nombre_completo, edad, genero, direccion, telefono, historia_clinica FROM pacientes";
        try (Connection conn = dbService.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                pacientes.add(mapResultSetToPaciente(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error obteniendo pacientes: " + e.getMessage());
        }
        return pacientes;
    }

    public boolean actualizar(String id, Paciente paciente) {
        String sql = "UPDATE pacientes SET nombre_completo = ?, edad = ?, genero = ?, direccion = ?, telefono = ?, historia_clinica = ? WHERE id = ?";
        try (Connection conn = dbService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, paciente.getNombreCompleto());
            stmt.setInt(2, paciente.getEdad());
            stmt.setString(3, paciente.getGenero());
            stmt.setString(4, paciente.getDireccion());
            stmt.setString(5, paciente.getTelefono());
            stmt.setString(6, paciente.getHistoriaClinica());
            stmt.setString(7, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error actualizando paciente: " + e.getMessage());
            return false;
        }
    }

    public boolean eliminar(String id) {
        String sql = "DELETE FROM pacientes WHERE id = ?";
        try (Connection conn = dbService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error eliminando paciente: " + e.getMessage());
            return false;
        }
    }

    private Paciente mapResultSetToPaciente(ResultSet rs) throws SQLException {
        return new Paciente(
                rs.getString("id"),
                rs.getString("nombre_completo"),
                rs.getInt("edad"),
                rs.getString("genero"),
                rs.getString("direccion"),
                rs.getString("telefono"),
                rs.getString("historia_clinica")
        );
    }
}
