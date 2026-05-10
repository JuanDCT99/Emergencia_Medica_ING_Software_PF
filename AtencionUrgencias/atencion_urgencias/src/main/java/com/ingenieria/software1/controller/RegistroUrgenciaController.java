package com.ingenieria.software1.controller;

import com.ingenieria.software1.model.Paciente;
import com.ingenieria.software1.service.EmergenciaService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class RegistroUrgenciaController {

    @FXML private TextField txtDocumento;
    @FXML private TextField txtNombre;
    @FXML private TextField txtEdad;
    @FXML private RadioButton rbMasculino;
    @FXML private RadioButton rbFemenino;
    @FXML private TextArea txtSintomas;
    @FXML private TextField txtUbicacion;
    @FXML private ComboBox<Integer> cbTriage;
    @FXML private TextField txtSignos;

    private final ToggleGroup grupoGenero = new ToggleGroup();

    @FXML
    public void initialize() {
        rbMasculino.setToggleGroup(grupoGenero);
        rbFemenino.setToggleGroup(grupoGenero);
        
        // Inicializar ComboBox de Triage (1-5)
        cbTriage.getItems().addAll(1, 2, 3, 4, 5);
        cbTriage.setValue(3); // Por defecto Triage 3 (Urgente pero no crítico)
    }

    @FXML
    public void onRegistrarClick() {
        try {
            String documento = txtDocumento.getText();
            String nombre = txtNombre.getText();
            String edadStr = txtEdad.getText();
            String genero = rbMasculino.isSelected() ? "Masculino" : "Femenino";
            String sintomas = txtSintomas.getText();
            String ubicacion = txtUbicacion.getText();
            Integer nivelTriage = cbTriage.getValue();
            String signos = txtSignos.getText();

            if (documento.isBlank() || nombre.isBlank() || sintomas.isBlank() || nivelTriage == null) {
                mostrarAlerta("Error", "Todos los campos obligatorios deben estar llenos (Documento, Nombre, Síntomas y Triage).");
                return;
            }

            int edad = Integer.parseInt(edadStr);
            Paciente paciente = new Paciente(documento, nombre, edad, genero, "", "", "");
            EmergenciaService.getInstance().registrarUrgenciaReal(paciente, sintomas, ubicacion, nivelTriage, signos);

            cerrarVentana();
        } catch (NumberFormatException e) {
            mostrarAlerta("Error de formato", "La edad debe ser un número válido.");
        }
    }

    @FXML
    public void onCancelarClick() {
        cerrarVentana();
    }

    private void cerrarVentana() {
        Stage stage = (Stage) txtDocumento.getScene().getWindow();
        stage.close();
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
