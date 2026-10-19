package com.ingenieria.software1.controller;

import com.ingenieria.software1.App;
import com.ingenieria.software1.service.UsuarioService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.io.IOException;

public class AdminDashboardController {

    @FXML
    private Label lblBienvenido;

    private final UsuarioService usuarioService = UsuarioService.getInstance();

    @FXML
    public void initialize() {
        if (usuarioService.getUsuarioLogueado() != null) {
            lblBienvenido.setText("Bienvenido, " + usuarioService.getUsuarioLogueado().getNombreCompleto() + " (" + usuarioService.getUsuarioLogueado().getRol() + ")");
        }
    }

    @FXML
    public void onGestionarUsuariosClick() throws IOException {
        App.setRoot("view/user_form");
    }

    @FXML
    public void onGestionarAmbulanciasClick() throws IOException {
        App.setRoot("view/ambulancia_form");
    }

    @FXML
    public void onCerrarSesionClick() throws IOException {
        usuarioService.cerrarSesion();
        App.setRoot("view/login");
    }
}
