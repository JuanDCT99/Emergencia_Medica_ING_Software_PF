package com.ingenieria.software1.controller;

import com.ingenieria.software1.App;
import com.ingenieria.software1.model.Empleado;
import com.ingenieria.software1.model.RolEmpleado;
import com.ingenieria.software1.service.UsuarioService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import java.io.IOException;

public class LoginController {

    @FXML
    private TextField txtUsuario;

    @FXML
    private PasswordField txtContrasena;

    @FXML
    private Label lblMensaje;

    private final UsuarioService usuarioService = UsuarioService.getInstance();

    @FXML
    public void onLoginClick() {
        String usuario = txtUsuario.getText();
        String password = txtContrasena.getText();

        System.out.println("Intento de login: usuario=" + usuario);
        
        Empleado empleado = usuarioService.autenticar(usuario, password);

        if (empleado != null) {
            System.out.println("Login exitoso. Rol: " + empleado.getRol());
            lblMensaje.setText("¡Bienvenido, " + empleado.getNombreCompleto() + "!");
            lblMensaje.setStyle("-fx-text-fill: green;");
            
            try {
                switch (empleado.getRol()) {
                    case ADMIN:
                        System.out.println("Redirigiendo a admin_dashboard...");
                        App.setRoot("view/admin_dashboard");
                        break;
                    case RECEPCIONISTA:
                        System.out.println("Redirigiendo a recepcionista...");
                        App.setRoot("view/recepcionista");
                        break;
                    case MEDICO:
                        System.out.println("Redirigiendo a medico...");
                        App.setRoot("view/medico");
                        break;
                    case ENFERMERO:
                        System.out.println("Redirigiendo a personal_medico...");
                        App.setRoot("view/personal_medico");
                        break;
                    case AUXILIAR:
                        System.out.println("Redirigiendo a personal_medico...");
                        App.setRoot("view/personal_medico");
                        break;
                    default:
                        System.out.println("Rol desconocido, redirigiendo a recepcionista...");
                        App.setRoot("view/recepcionista");
                        break;
                }
            } catch (IOException e) {
                System.err.println("Error redirigiendo: " + e.getMessage());
                e.printStackTrace();
                lblMensaje.setText("Error al cargar la vista");
                lblMensaje.setStyle("-fx-text-fill: red;");
            }
        } else {
            System.out.println("Login fallido para usuario: " + usuario);
            lblMensaje.setText("Usuario o contraseña incorrectos");
            lblMensaje.setStyle("-fx-text-fill: red;");
        }
    }
    

}
