package com.ingenieria.software1.controller;

import com.ingenieria.software1.model.Empleado;
import com.ingenieria.software1.model.RolEmpleado;
import com.ingenieria.software1.service.UserManagementService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

/**
 * Controlador para el formulario de usuario (creación y edición).
 */
public class UserFormController {

    @FXML
    private TextField txtDocumento;
    @FXML
    private TextField txtUsuario;
    @FXML
    private TextField txtNombreCompleto;
    @FXML
    private PasswordField txtContrasena;
    @FXML
    private PasswordField txtConfirmarContrasena;
    @FXML
    private ComboBox<RolEmpleado> cbRol;
    @FXML
    private Label lblMensaje;
    @FXML
    private Label lblTitulo;
    @FXML
    private Button btnGuardar;
    @FXML
    private Button btnCancelar;

    private boolean esModoEdicion = false;
    private String documentoOriginal = null;
    private final UserManagementService userManagementService = new UserManagementService();
    private java.util.function.Consumer<Empleado> onUsuarioGuardado;

    @FXML
    public void initialize() {
        cbRol.getItems().setAll(RolEmpleado.values());
    }

    /**
     * Inicializa el formulario para creación o edición.
     * @param usuarioParaEditar El usuario a editar, o null para crear uno nuevo
     * @param callback Función a llamar cuando se guarde exitosamente un usuario
     */
    public void inicializarFormulario(Empleado usuarioParaEditar, java.util.function.Consumer<Empleado> callback) {
        this.onUsuarioGuardado = callback;
        
        if (usuarioParaEditar != null) {
            // Modo edición
            esModoEdicion = true;
            documentoOriginal = usuarioParaEditar.getId();
            lblTitulo.setText("Editar Usuario");
            
            txtDocumento.setText(usuarioParaEditar.getId());
            txtUsuario.setText(usuarioParaEditar.getUsuario());
            txtNombreCompleto.setText(usuarioParaEditar.getNombreCompleto());
            // No cargamos la contraseña por seguridad
            cbRol.setValue(usuarioParaEditar.getRol());
            
            // En modo edición, el documento no se puede modificar
            txtDocumento.setEditable(false);
        } else {
            // Modo creación
            esModoEdicion = false;
            documentoOriginal = null;
            lblTitulo.setText("Crear Nuevo Usuario");
            
            // Limpiar campos
            txtDocumento.setText("");
            txtUsuario.setText("");
            txtNombreCompleto.setText("");
            txtContrasena.setText("");
            txtConfirmarContrasena.setText("");
            cbRol.setValue(RolEmpleado.RECEPCIONISTA); // Valor por defecto
            
            txtDocumento.setEditable(true);
        }
        
        // Limpiar mensaje
        lblMensaje.setText("");
    }

    /**
     * Maneja el evento de click en el botón "Guardar".
     */
    @FXML
    private void onGuardarClick(ActionEvent event) {
        if (validarFormulario()) {
            Empleado usuario = crearUsuarioDesdeFormulario();
            
            UserManagementService.ResultadoOperacion resultado;
            if (esModoEdicion) {
                resultado = userManagementService.actualizarUsuario(documentoOriginal, usuario);
            } else {
                resultado = userManagementService.crearUsuario(usuario);
            }
            
            if (resultado.isExitoso()) {
                if (onUsuarioGuardado != null) {
                    onUsuarioGuardado.accept(usuario);
                }
                cerrarVentana();
                mostrarInformacion(resultado.getMensaje());
            } else {
                mostrarError(resultado.getMensaje());
            }
        }
    }

    /**
     * Maneja el evento de click en el botón "Cancelar".
     */
    @FXML
    private void onCancelarClick(ActionEvent event) {
        cerrarVentana();
    }

    /**
     * Valida el formulario antes de guardar.
     * @return true si el formulario es válido, false en caso contrario
     */
    private boolean validarFormulario() {
        // Limpiar mensaje previo
        lblMensaje.setText("");
        
        // Validar documento
        String documento = txtDocumento.getText().trim();
        if (documento.isEmpty()) {
            mostrarError("El documento es requerido");
            return false;
        }
        
        // En modo creación, verificar que el documento no exista
        if (!esModoEdicion) {
            Empleado existente = userManagementService.obtenerUsuario(documento);
            if (existente != null) {
                mostrarError("Ya existe un usuario con ese documento");
                return false;
            }
        }
        
        // Validar usuario
        String usuario = txtUsuario.getText().trim();
        if (usuario.isEmpty()) {
            mostrarError("El nombre de usuario es requerido");
            return false;
        }
        
        // En modo creación, verificar que el usuario no exista
        if (!esModoEdicion) {
            Empleado existentePorUsuario = userManagementService.buscarPorUsuario(usuario);
            if (existentePorUsuario != null) {
                mostrarError("Ya existe un usuario con ese nombre de usuario");
                return false;
            }
        }
        
        // Validar nombre completo
        String nombreCompleto = txtNombreCompleto.getText().trim();
        if (nombreCompleto.isEmpty()) {
            mostrarError("El nombre completo es requerido");
            return false;
        }
        
        // Validar contraseña
        String contrasena = txtContrasena.getText();
        String confirmarContrasena = txtConfirmarContrasena.getText();
        
        if (contrasena.isEmpty()) {
            mostrarError("La contraseña es requerida");
            return false;
        }
        
        if (contrasena.length() < 6) {
            mostrarError("La contraseña debe tener al menos 6 caracteres");
            return false;
        }
        
        if (!contrasena.equals(confirmarContrasena)) {
            mostrarError("Las contraseñas no coinciden");
            return false;
        }
        
        // Validar rol
        if (cbRol.getValue() == null) {
            mostrarError("El rol es requerido");
            return false;
        }
        
        return true;
    }

    /**
     * Crea un objeto Empleado a partir de los datos del formulario.
     * @return El empleado creado desde el formulario
     */
    private Empleado crearUsuarioDesdeFormulario() {
        return new Empleado(
                txtDocumento.getText().trim(),
                txtNombreCompleto.getText().trim(),
                txtUsuario.getText().trim(),
                txtContrasena.getText(), // Se hasheará en el servicio
                cbRol.getValue()
        );
    }

    /**
     * Cierra la ventana actual.
     */
    private void cerrarVentana() {
        Stage stage = (Stage) btnGuardar.getScene().getWindow();
        stage.close();
    }

    /**
     * Muestra un mensaje de información.
     * @param mensaje El mensaje a mostrar
     */
    private void mostrarInformacion(String mensaje) {
        lblMensaje.setStyle("-fx-text-fill: GREEN;");
        lblMensaje.setText(mensaje);
    }

    /**
     * Muestra un mensaje de error.
     * @param mensaje El mensaje de error a mostrar
     */
    private void mostrarError(String mensaje) {
        lblMensaje.setStyle("-fx-text-fill: RED;");
        lblMensaje.setText(mensaje);
    }
}