package com.ingenieria.software1.service;

import com.ingenieria.software1.model.Empleado;
import com.ingenieria.software1.model.RolEmpleado;

/**
 * Servicio de gestión de usuarios para operaciones CRUD completas.
 * Actúa como capa intermedia entre la UI y UsuarioService.
 */
public class UserManagementService {
    private final UsuarioService usuarioService;
    
    public UserManagementService() {
        this.usuarioService = UsuarioService.getInstance();
    }
    
    /**
     * Crea un nuevo usuario.
     * @param nuevoUsuario El usuario a crear (con contraseña en texto plano)
     * @return Resultado de la operación
     */
    public ResultadoOperacion crearUsuario(Empleado nuevoUsuario) {
        // Validaciones básicas
        if (nuevoUsuario == null) {
            return new ResultadoOperacion(false, "El usuario no puede ser nulo");
        }
        
        if (nuevoUsuario.getId() == null || nuevoUsuario.getId().trim().isEmpty()) {
            return new ResultadoOperacion(false, "El documento es requerido");
        }
        
        if (nuevoUsuario.getUsuario() == null || nuevoUsuario.getUsuario().trim().isEmpty()) {
            return new ResultadoOperacion(false, "El nombre de usuario es requerido");
        }
        
        if (nuevoUsuario.getNombreCompleto() == null || nuevoUsuario.getNombreCompleto().trim().isEmpty()) {
            return new ResultadoOperacion(false, "El nombre completo es requerido");
        }
        
        if (nuevoUsuario.getContrasena() == null || nuevoUsuario.getContrasena().trim().isEmpty()) {
            return new ResultadoOperacion(false, "La contraseña es requerida");
        }
        
        if (nuevoUsuario.getRol() == null) {
            return new ResultadoOperacion(false, "El rol es requerido");
        }
        
        // Intentar crear el usuario
        boolean creado = usuarioService.crearEmpleado(nuevoUsuario);
        if (creado) {
            return new ResultadoOperacion(true, "Usuario creado exitosamente");
        } else {
            return new ResultadoOperacion(false, "Ya existe un usuario con ese documento o nombre de usuario");
        }
    }
    
    /**
     * Actualiza un usuario existente.
     * @param documento El documento del usuario a actualizar
     * @param usuarioActualizado Los datos actualizados (contraseña puede ser en texto plano)
     * @return Resultado de la operación
     */
    public ResultadoOperacion actualizarUsuario(String documento, Empleado usuarioActualizado) {
        if (documento == null || documento.trim().isEmpty()) {
            return new ResultadoOperacion(false, "El documento es requerido");
        }
        
        if (usuarioActualizado == null) {
            return new ResultadoOperacion(false, "Los datos del usuario no pueden ser nulos");
        }
        
        // Verificar que el documento coincida
        if (!documento.equals(usuarioActualizado.getId())) {
            return new ResultadoOperacion(false, "El documento no puede ser modificado");
        }
        
        boolean actualizado = usuarioService.actualizarEmpleado(documento, usuarioActualizado);
        if (actualizado) {
            return new ResultadoOperacion(true, "Usuario actualizado exitosamente");
        } else {
            return new ResultadoOperacion(false, "No se encontró el usuario con el documento especificado");
        }
    }
    
    /**
     * Elimina un usuario por su documento.
     * @param documento El documento del usuario a eliminar
     * @return Resultado de la operación
     */
    public ResultadoOperacion eliminarUsuario(String documento) {
        if (documento == null || documento.trim().isEmpty()) {
            return new ResultadoOperacion(false, "El documento es requerido");
        }
        
        boolean eliminado = usuarioService.eliminarEmpleado(documento);
        if (eliminado) {
            return new ResultadoOperacion(true, "Usuario eliminado exitosamente");
        } else {
            return new ResultadoOperacion(false, "No se encontró el usuario con el documento especificado");
        }
    }
    
    /**
     * Obtiene un usuario por su documento.
     * @param documento El documento del usuario a buscar
     * @return El usuario si se encuentra, null si no se encuentra
     */
    public Empleado obtenerUsuario(String documento) {
        if (documento == null || documento.trim().isEmpty()) {
            return null;
        }
        return usuarioService.buscarPorDocumento(documento);
    }

    /**
     * Obtiene un usuario por su nombre de usuario.
     * @param usuario El nombre de usuario a buscar
     * @return El usuario si se encuentra, null si no se encuentra
     */
    public Empleado buscarPorUsuario(String usuario) {
        if (usuario == null || usuario.trim().isEmpty()) {
            return null;
        }
        return usuarioService.buscarPorUsuario(usuario);
    }
    
    /**
     * Obtiene todos los usuarios.
     * @return Lista de todos los usuarios
     */
    public java.util.List<Empleado> obtenerTodosUsuarios() {
        return usuarioService.obtenerTodosEmpleados();
    }
    
    /**
     * Obtiene usuarios por rol.
     * @param rol El rol a filtrar
     * @return Lista de usuarios con el rol especificado
     */
    public java.util.List<Empleado> obtenerUsuariosPorRol(RolEmpleado rol) {
        return usuarioService.getEmpleadosPorRol(rol);
    }
    
    /**
     * Clase interna para encapsular el resultado de una operación.
     */
    public static class ResultadoOperacion {
        private final boolean exitoso;
        private final String mensaje;
        
        public ResultadoOperacion(boolean exitoso, String mensaje) {
            this.exitoso = exitoso;
            this.mensaje = mensaje;
        }
        
        public boolean isExitoso() {
            return exitoso;
        }
        
        public String getMensaje() {
            return mensaje;
        }
    }
}