package com.ingenieria.software1.controller;

import com.ingenieria.software1.App;
import com.ingenieria.software1.model.Empleado;
import com.ingenieria.software1.model.Urgencia;
import com.ingenieria.software1.service.EmergenciaService;
import com.ingenieria.software1.service.UsuarioService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class PersonalMedicoController {

    @FXML private Label lblBienvenido;
    @FXML private TableView<Urgencia> tblUrgencias;
    @FXML private TableColumn<Urgencia, String> colId;
    @FXML private TableColumn<Urgencia, String> colDescripcion;
    @FXML private TableColumn<Urgencia, String> colUbicacion;
    @FXML private TableColumn<Urgencia, Integer> colGravedad;
    @FXML private TableColumn<Urgencia, LocalDateTime> colHora;
    @FXML private Label lblEstadoSistema;

    private final EmergenciaService emergenciaService = EmergenciaService.getInstance();
    private final UsuarioService usuarioService = UsuarioService.getInstance();
    private final ObservableList<Urgencia> listaObservable = FXCollections.observableArrayList();
    private Empleado personalLogueado;

    @FXML
    public void initialize() {
        personalLogueado = usuarioService.getUsuarioLogueado();
        if (personalLogueado != null) {
            lblBienvenido.setText("Bienvenido, " + personalLogueado.getNombreCompleto() + " (" + personalLogueado.getRol() + ")");
        }

        // Configurar columnas
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        
        colDescripcion.setCellValueFactory(cellData -> {
            var paciente = cellData.getValue().getPaciente();
            return new javafx.beans.property.SimpleStringProperty(paciente != null ? paciente.getNombreCompleto() : cellData.getValue().getSintomatologia());
        });
        
        colUbicacion.setCellValueFactory(new PropertyValueFactory<>("ubicacion"));
        
        colGravedad.setCellValueFactory(cellData -> {
            var triage = cellData.getValue().getTriage();
            return new javafx.beans.property.SimpleObjectProperty<>(triage != null ? triage.getNivelPrioridad() : 0);
        });
        
        colHora.setCellValueFactory(new PropertyValueFactory<>("horaReporte"));

        refrescarTabla();
    }

    private void refrescarTabla() {
        if (personalLogueado == null) return;

        List<Urgencia> misUrgencias = emergenciaService.getTodasLasUrgencias().stream()
                .filter(u -> u.getPersonalAsignado() != null && 
                             u.getPersonalAsignado().getId().equals(personalLogueado.getId()) &&
                             u.getEstado().equals("EN_CURSO"))
                .collect(Collectors.toList());

        listaObservable.setAll(misUrgencias);
        tblUrgencias.setItems(listaObservable);
    }

    @FXML
    public void onFinalizarClick() {
        Urgencia seleccionada = tblUrgencias.getSelectionModel().getSelectedItem();
        if (seleccionada != null) {
            emergenciaService.finalizarUrgencia(seleccionada.getId());
            refrescarTabla();
            lblEstadoSistema.setText("Urgencia " + seleccionada.getId() + " finalizada correctamente.");
        } else {
            lblEstadoSistema.setText("Por favor, seleccione una urgencia de la tabla.");
        }
    }

    @FXML
    public void onCerrarSesionClick() throws IOException {
        usuarioService.cerrarSesion();
        App.setRoot("view/login");
    }
}
