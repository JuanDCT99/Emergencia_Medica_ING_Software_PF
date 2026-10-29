package com.ingenieria.software1.service;

import com.ingenieria.software1.model.Empleado;
import com.ingenieria.software1.model.RolEmpleado;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio encargado de gestionar el personal (empleados) y la autenticación.
 * Usa MariaDB para persistencia en lugar de CSV.
 */
public class UsuarioService {
    private static UsuarioService instance;
    private final EmpleadoDAO empleadoDAO;
    private Empleado usuarioLogueado;

    private UsuarioService() {
        this.empleadoDAO = new EmpleadoDAO();
    }

    public static synchronized UsuarioService getInstance() {
        if (instance == null) {
            instance = new UsuarioService();
        }
        return instance;
    }

    /**
     * Hashea una contraseña usando SHA-256.
     */
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error al hashear la contraseña", e);
        }
    }

    /**
     * Valida las credenciales de un usuario.
     * @return El empleado si es exitoso, null de lo contrario.
     */
    public Empleado autenticar(String usuario, String contrasena) {
        String hashedPassword = hashPassword(contrasena);
        Empleado empleado = empleadoDAO.autenticar(usuario, hashedPassword);
        if (empleado != null) {
            usuarioLogueado = empleado;
        }
        return empleado;
    }

    /**
     * Crea un nuevo empleado y lo guarda en MariaDB.
     * @return true si se creó exitosamente, false si ya existe el documento o usuario
     */
    public boolean crearEmpleado(Empleado empleado) {
        // Verificar si ya existe
        if (empleadoDAO.buscarPorId(empleado.getId()) != null || 
            empleadoDAO.buscarPorUsuario(empleado.getUsuario()) != null) {
            return false;
        }
        
        // Hashear la contraseña antes de guardar
        Empleado empleadoConHash = new Empleado(
                empleado.getId(),
                empleado.getNombreCompleto(),
                empleado.getUsuario(),
                hashPassword(empleado.getContrasena()),
                empleado.getRol()
        );
        
        return empleadoDAO.crear(empleadoConHash);
    }

    /**
     * Actualiza un empleado existente.
     * @return true si se actualizó exitosamente, false si no se encontró
     */
    public boolean actualizarEmpleado(String documento, Empleado empleadoActualizado) {
        // Hashear la contraseña si es texto plano
        String passwordFinal = empleadoActualizado.getContrasena();
        if (!esPasswordHasheada(passwordFinal)) {
            passwordFinal = hashPassword(passwordFinal);
        }
        
        Empleado empleadoConHash = new Empleado(
                empleadoActualizado.getId(),
                empleadoActualizado.getNombreCompleto(),
                empleadoActualizado.getUsuario(),
                passwordFinal,
                empleadoActualizado.getRol()
        );
        
        return empleadoDAO.actualizar(documento, empleadoConHash);
    }

    /**
     * Elimina un empleado por su documento.
     * @return true si se eliminó exitosamente, false si no se encontró
     */
    public boolean eliminarEmpleado(String documento) {
        return empleadoDAO.eliminar(documento);
    }

    /**
     * Busca un empleado por su documento.
     */
    public Empleado buscarPorDocumento(String documento) {
        return empleadoDAO.buscarPorId(documento);
    }

    /**
     * Busca un empleado por su nombre de usuario.
     */
    public Empleado buscarPorUsuario(String usuario) {
        return empleadoDAO.buscarPorUsuario(usuario);
    }

    public Empleado getUsuarioLogueado() {
        return usuarioLogueado;
    }

    public void cerrarSesion() {
        this.usuarioLogueado = null;
    }

    public List<Empleado> getEmpleadosPorRol(RolEmpleado rol) {
        return empleadoDAO.obtenerPorRol(rol);
    }

    /**
     * Obtiene todos los empleados.
     */
    public List<Empleado> obtenerTodosEmpleados() {
        return empleadoDAO.obtenerTodos();
    }

    /**
     * Verifica si una contraseña ya está hasheada.
     */
    private boolean esPasswordHasheada(String password) {
        return password != null && password.matches("[0-9a-fA-F]{64}");
    }
}
