package com.ingenieria.software1.controller;

import com.ingenieria.software1.model.Paciente;
import com.ingenieria.software1.model.Urgencia;
import com.ingenieria.software1.service.EmergenciaService;
import com.ingenieria.software1.service.ValidadorDatos;
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
    private Urgencia urgenciaRegistrada;

    /** Urgencia guardada por este formulario, o null si se canceló o no se pudo guardar. */
    public Urgencia getUrgenciaRegistrada() {
        return urgenciaRegistrada;
    }

    @FXML
    public void initialize() {
        rbMasculino.setToggleGroup(grupoGenero);
        rbFemenino.setToggleGroup(grupoGenero);

        // Inicializar ComboBox de Triage (1-5)
        cbTriage.getItems().addAll(1, 2, 3, 4, 5);
        cbTriage.setValue(ValidadorDatos.NIVEL_TRIAGE_POR_DEFECTO); // Por defecto Triage 3 (Urgente pero no crítico)
    }

    @FXML
    public void onRegistrarClick() {
        String documento = txtDocumento.getText() == null ? "" : txtDocumento.getText().trim();
        String nombre = txtNombre.getText() == null ? "" : txtNombre.getText().trim();
        String edadStr = txtEdad.getText() == null ? "" : txtEdad.getText().trim();
        String genero = rbMasculino.isSelected() ? "Masculino" : "Femenino";
        String sintomas = txtSintomas.getText() == null ? "" : txtSintomas.getText().trim();
        String ubicacion = txtUbicacion.getText() == null ? "" : txtUbicacion.getText().trim();
        Integer nivelTriage = cbTriage.getValue();
        String signos = txtSignos.getText() == null ? "" : txtSignos.getText().trim();

        // F-04: solo se aceptan datos del dominio de cada campo, indicando el tipo requerido.
        String error = primerError(
                ValidadorDatos.validarDocumento(documento),
                ValidadorDatos.validarNombre(nombre),
                ValidadorDatos.validarEdad(edadStr),
                ValidadorDatos.validarTextoRequerido(sintomas, "síntomas"),
                ValidadorDatos.validarTextoOpcional(ubicacion, "ubicación", 200),
                ValidadorDatos.validarNivelTriage(nivelTriage),
                ValidadorDatos.validarTextoOpcional(signos, "signos vitales", 200));
        if (error != null) {
            mostrarAlerta("Dato inválido", error);
            return;
        }

        Paciente paciente = new Paciente(documento, nombre, Integer.parseInt(edadStr), genero, "", "", "");
        Urgencia registrada = EmergenciaService.getInstance()
                .registrarUrgenciaReal(paciente, sintomas, ubicacion, nivelTriage, signos);

        if (registrada == null) {
            mostrarAlerta("No se pudo registrar", "La urgencia no se pudo guardar. Verifique los datos e intente de nuevo.");
            return;
        }

        urgenciaRegistrada = registrada;
        cerrarVentana();
    }

    private String primerError(String... errores) {
        for (String e : errores) {
            if (e != null) return e;
        }
        return null;
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
