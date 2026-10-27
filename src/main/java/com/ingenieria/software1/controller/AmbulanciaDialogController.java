package com.ingenieria.software1.controller;

import com.ingenieria.software1.model.Ambulancia;
import com.ingenieria.software1.service.AmbulanciaService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;

public class AmbulanciaDialogController {

    @FXML private Label lblTitulo;
    @FXML private TextField txtId;
    @FXML private TextField txtPlaca;
    @FXML private ComboBox<String> cbEstado;
    @FXML private TextField txtModelo;
    @FXML private TextField txtKilometraje;
    @FXML private TextArea txtObservaciones;
    @FXML private Label lblMensaje;

    private final AmbulanciaService ambulanciaService = AmbulanciaService.getInstance();
    private boolean modoEdicion = false;
    private Ambulancia ambulanciaEditar = null;

    @FXML
    public void initialize() {
        cbEstado.getItems().addAll(
            Ambulancia.ESTADO_DISPONIBLE,
            Ambulancia.ESTADO_FUERA_SERVICIO
        );
        cbEstado.setValue(Ambulancia.ESTADO_DISPONIBLE);
    }

    public void setModoNuevo() {
        lblTitulo.setText("Nueva Ambulancia");
        modoEdicion = false;
        ambulanciaEditar = null;
        cbEstado.setDisable(true);
    }

    public void setModoEdicion(Ambulancia ambulancia) {
        this.ambulanciaEditar = ambulancia;
        this.modoEdicion = true;
        lblTitulo.setText("Editar Ambulancia");
        
        txtId.setText(ambulancia.getId());
        txtId.setDisable(true);
        txtPlaca.setText(ambulancia.getPlaca());
        txtModelo.setText(ambulancia.getModelo() != null ? ambulancia.getModelo() : "");
        txtKilometraje.setText(ambulancia.getKilometraje() != null ? String.valueOf(ambulancia.getKilometraje()) : "0");
        txtObservaciones.setText(ambulancia.getObservaciones() != null ? ambulancia.getObservaciones() : "");
        
        cbEstado.setValue(ambulancia.getEstado());
        
        if (Ambulancia.ESTADO_EN_USO.equals(ambulancia.getEstado())) {
            cbEstado.setDisable(true);
        }
    }

    @FXML
    public void onGuardarClick() {
        if (!validarFormulario()) {
            return;
        }

        String id = txtId.getText().trim();
        String placa = txtPlaca.getText().trim();
        String estado = cbEstado.getValue();
        String modelo = txtModelo.getText().trim();
        String kilometrajeStr = txtKilometraje.getText().trim();
        String observaciones = txtObservaciones.getText().trim();

        int kilometraje = 0;
        try {
            if (!kilometrajeStr.isEmpty()) {
                kilometraje = Integer.parseInt(kilometrajeStr);
            }
        } catch (NumberFormatException e) {
            mostrarError("El kilometraje debe ser un número válido.");
            return;
        }

        Ambulancia ambulancia = new Ambulancia();
        ambulancia.setId(id);
        ambulancia.setPlaca(placa);
        ambulancia.setEstado(estado);
        ambulancia.setModelo(modelo.isEmpty() ? null : modelo);
        ambulancia.setKilometraje(kilometraje);
        ambulancia.setObservaciones(observaciones.isEmpty() ? null : observaciones);

        if (modoEdicion && ambulanciaEditar != null) {
            if (ambulanciaService.actualizarAmbulancia(ambulanciaEditar.getId(), ambulancia)) {
                cerrarVentana();
            } else {
                mostrarError("No se pudo actualizar la ambulancia.");
            }
        } else {
            Ambulancia existente = ambulanciaService.obtenerPorId(id);
            if (existente != null) {
                mostrarError("Ya existe una ambulancia con ese ID.");
                return;
            }

            ambulancia.setFechaAlta(LocalDate.now());
            
            Ambulancia creada = ambulanciaService.crearAmbulancia(ambulancia);
            if (creada != null) {
                cerrarVentana();
            } else {
                mostrarError("No se pudo crear la ambulancia.");
            }
        }
    }

    @FXML
    public void onCancelarClick() {
        cerrarVentana();
    }

    private boolean validarFormulario() {
        if (txtId.getText().trim().isEmpty()) {
            mostrarError("El ID es requerido.");
            return false;
        }
        if (txtPlaca.getText().trim().isEmpty()) {
            mostrarError("La placa es requerida.");
            return false;
        }
        if (cbEstado.getValue() == null) {
            mostrarError("Debe seleccionar un estado.");
            return false;
        }
        return true;
    }

    private void mostrarError(String mensaje) {
        lblMensaje.setStyle("-fx-text-fill: #C62828; -fx-font-weight: bold;");
        lblMensaje.setText(mensaje);
    }

    private void cerrarVentana() {
        Stage stage = (Stage) txtId.getScene().getWindow();
        stage.close();
    }
}