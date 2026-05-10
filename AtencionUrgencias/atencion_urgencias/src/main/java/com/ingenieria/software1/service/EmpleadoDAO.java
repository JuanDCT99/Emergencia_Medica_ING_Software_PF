package com.ingenieria.software1.service;

import com.ingenieria.software1.model.Empleado;
import com.ingenieria.software1.model.RolEmpleado;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmpleadoDAO {
    private final DatabaseService dbService;

    public EmpleadoDAO() {
        this.dbService = DatabaseService.getInstance();
    }

    public Empleado autenticar(String usuario, String contrasenaHash) {
        String sql = "SELECT id, nombre_completo, usuario, contrasena_hash, rol FROM empleados WHERE usuario = ? AND contrasena_hash = ?";
        try (Connection conn = dbService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, usuario);
            stmt.setString(2, contrasenaHash);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToEmpleado(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error autenticando: " + e.getMessage());
        }
        return null;
    }

    public List<Empleado> obtenerTodos() {
        List<Empleado> empleados = new ArrayList<>();
        String sql = "SELECT id, nombre_completo, usuario, contrasena_hash, rol FROM empleados";
        try (Connection conn = dbService.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                empleados.add(mapResultSetToEmpleado(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error obteniendo empleados: " + e.getMessage());
        }
        return empleados;
    }

    public Empleado buscarPorId(String id) {
        String sql = "SELECT id, nombre_completo, usuario, contrasena_hash, rol FROM empleados WHERE id = ?";
        try (Connection conn = dbService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToEmpleado(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error buscando empleado: " + e.getMessage());
        }
        return null;
    }

    public Empleado buscarPorUsuario(String usuario) {
        String sql = "SELECT id, nombre_completo, usuario, contrasena_hash, rol FROM empleados WHERE usuario = ?";
        try (Connection conn = dbService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, usuario);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToEmpleado(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error buscando por usuario: " + e.getMessage());
        }
        return null;
    }

    public boolean crear(Empleado empleado) {
        String sql = "INSERT INTO empleados (id, nombre_completo, usuario, contrasena_hash, rol) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = dbService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, empleado.getId());
            stmt.setString(2, empleado.getNombreCompleto());
            stmt.setString(3, empleado.getUsuario());
            stmt.setString(4, empleado.getContrasena());
            stmt.setString(5, empleado.getRol().toString());
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error creando empleado: " + e.getMessage());
            return false;
        }
    }

    public boolean actualizar(String id, Empleado empleado) {
        String sql = "UPDATE empleados SET nombre_completo = ?, usuario = ?, contrasena_hash = ?, rol = ? WHERE id = ?";
        try (Connection conn = dbService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, empleado.getNombreCompleto());
            stmt.setString(2, empleado.getUsuario());
            stmt.setString(3, empleado.getContrasena());
            stmt.setString(4, empleado.getRol().toString());
            stmt.setString(5, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error actualizando empleado: " + e.getMessage());
            return false;
        }
    }

    public boolean eliminar(String id) {
        String sql = "DELETE FROM empleados WHERE id = ?";
        try (Connection conn = dbService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error eliminando empleado: " + e.getMessage());
            return false;
        }
    }

    public List<Empleado> obtenerPorRol(RolEmpleado rol) {
        List<Empleado> empleados = new ArrayList<>();
        String sql = "SELECT id, nombre_completo, usuario, contrasena_hash, rol FROM empleados WHERE rol = ?";
        try (Connection conn = dbService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, rol.toString());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                empleados.add(mapResultSetToEmpleado(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error obteniendo empleados por rol: " + e.getMessage());
        }
        return empleados;
    }

    private Empleado mapResultSetToEmpleado(ResultSet rs) throws SQLException {
        return new Empleado(
                rs.getString("id"),
                rs.getString("nombre_completo"),
                rs.getString("usuario"),
                rs.getString("contrasena_hash"),
                RolEmpleado.valueOf(rs.getString("rol"))
        );
    }
}
