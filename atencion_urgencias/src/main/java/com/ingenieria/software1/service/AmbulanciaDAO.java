package com.ingenieria.software1.service;

import com.ingenieria.software1.model.Ambulancia;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AmbulanciaDAO {
    private final DatabaseService dbService;

    public AmbulanciaDAO() {
        this.dbService = DatabaseService.getInstance();
    }

    public boolean crear(Ambulancia ambulancia) {
        String sql = "INSERT INTO ambulancias (id, placa, estado, modelo, kilometraje, ultima_revision, fecha_alta, observaciones) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dbService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, ambulancia.getId());
            stmt.setString(2, ambulancia.getPlaca());
            stmt.setString(3, ambulancia.getEstado());
            stmt.setString(4, ambulancia.getModelo());
            stmt.setInt(5, ambulancia.getKilometraje() != null ? ambulancia.getKilometraje() : 0);
            
            if (ambulancia.getUltimaRevision() != null) {
                stmt.setDate(6, Date.valueOf(ambulancia.getUltimaRevision()));
            } else {
                stmt.setNull(6, Types.DATE);
            }
            
            if (ambulancia.getFechaAlta() != null) {
                stmt.setDate(7, Date.valueOf(ambulancia.getFechaAlta()));
            } else {
                stmt.setDate(7, Date.valueOf(LocalDate.now()));
            }
            
            stmt.setString(8, ambulancia.getObservaciones());
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error creando ambulancia: " + e.getMessage());
            return false;
        }
    }

    public Ambulancia buscarPorId(String id) {
        String sql = "SELECT * FROM ambulancias WHERE id = ?";
        try (Connection conn = dbService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToAmbulancia(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error buscando ambulancia: " + e.getMessage());
        }
        return null;
    }

    public List<Ambulancia> obtenerTodas() {
        List<Ambulancia> ambulancias = new ArrayList<>();
        String sql = "SELECT * FROM ambulancias ORDER BY id";
        try (Connection conn = dbService.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                ambulancias.add(mapResultSetToAmbulancia(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error obteniendo ambulancias: " + e.getMessage());
        }
        return ambulancias;
    }

    public List<Ambulancia> obtenerPorEstado(String estado) {
        List<Ambulancia> ambulancias = new ArrayList<>();
        String sql = "SELECT * FROM ambulancias WHERE estado = ? ORDER BY id";
        try (Connection conn = dbService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, estado);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                ambulancias.add(mapResultSetToAmbulancia(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error obteniendo ambulancias por estado: " + e.getMessage());
        }
        return ambulancias;
    }

    public boolean actualizarEstado(String id, String nuevoEstado) {
        String sql = "UPDATE ambulancias SET estado = ? WHERE id = ?";
        try (Connection conn = dbService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nuevoEstado);
            stmt.setString(2, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error actualizando estado de ambulancia: " + e.getMessage());
            return false;
        }
    }

    public boolean actualizar(String id, Ambulancia ambulancia) {
        String sql = "UPDATE ambulancias SET placa = ?, estado = ?, modelo = ?, kilometraje = ?, ultima_revision = ?, fecha_alta = ?, observaciones = ? WHERE id = ?";
        try (Connection conn = dbService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, ambulancia.getPlaca());
            stmt.setString(2, ambulancia.getEstado());
            stmt.setString(3, ambulancia.getModelo());
            stmt.setInt(4, ambulancia.getKilometraje() != null ? ambulancia.getKilometraje() : 0);
            
            if (ambulancia.getUltimaRevision() != null) {
                stmt.setDate(5, Date.valueOf(ambulancia.getUltimaRevision()));
            } else {
                stmt.setNull(5, Types.DATE);
            }
            
            if (ambulancia.getFechaAlta() != null) {
                stmt.setDate(6, Date.valueOf(ambulancia.getFechaAlta()));
            } else {
                stmt.setDate(6, Date.valueOf(LocalDate.now()));
            }
            
            stmt.setString(7, ambulancia.getObservaciones());
            stmt.setString(8, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error actualizando ambulancia: " + e.getMessage());
            return false;
        }
    }

    public boolean eliminar(String id) {
        String sql = "DELETE FROM ambulancias WHERE id = ?";
        try (Connection conn = dbService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error eliminando ambulancia: " + e.getMessage());
            return false;
        }
    }

    private Ambulancia mapResultSetToAmbulancia(ResultSet rs) throws SQLException {
        Ambulancia ambulancia = new Ambulancia();
        ambulancia.setId(rs.getString("id"));
        ambulancia.setPlaca(rs.getString("placa"));
        ambulancia.setEstado(rs.getString("estado"));
        ambulancia.setModelo(rs.getString("modelo"));
        ambulancia.setKilometraje(rs.getInt("kilometraje"));
        
        Date ultimaRevision = rs.getDate("ultima_revision");
        if (ultimaRevision != null) {
            ambulancia.setUltimaRevision(ultimaRevision.toLocalDate());
        }
        
        Date fechaAlta = rs.getDate("fecha_alta");
        if (fechaAlta != null) {
            ambulancia.setFechaAlta(fechaAlta.toLocalDate());
        }
        
        ambulancia.setObservaciones(rs.getString("observaciones"));
        return ambulancia;
    }
}