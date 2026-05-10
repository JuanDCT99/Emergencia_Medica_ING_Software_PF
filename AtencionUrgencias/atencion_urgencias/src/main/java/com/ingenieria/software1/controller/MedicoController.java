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

public class MedicoController {

    @FXML private Label lblBienvenido;
    @FXML private ComboBox<Empleado> cbMedicos;
    @FXML private TableView<Urgencia> tblUrgencias;
    @FXML private TableColumn<Urgencia, String> colId;
    @FXML private TableColumn<Urgencia, String> colDescripcion;
    @FXML private TableColumn<Urgencia, String> colUbicacion;
    @FXML private TableColumn<Urgencia, Integer> colGravedad;
    @FXML private TableColumn<Urgencia, LocalDateTime> colHora;
    @FXML private TableColumn<Urgencia, String> colAsignado;
    @FXML private Label lblEstadoSistema;

    private final EmergenciaService emergenciaService = EmergenciaService.getInstance();
    private final UsuarioService usuarioService = UsuarioService.getInstance();
    private final ObservableList<Urgencia> listaObservable = FXCollections.observableArrayList();
    private Empleado medicoLogueado;

    @FXML
    public void initialize() {
        medicoLogueado = usuarioService.getUsuarioLogueado();
        if (medicoLogueado != null) {
            lblBienvenido.setText("Bienvenido, " + medicoLogueado.getNombreCompleto() + " (" + medicoLogueado.getRol() + ")");
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
        
        colAsignado.setCellValueFactory(cellData -> {
            var personal = cellData.getValue().getPersonalAsignado();
            return new javafx.beans.property.SimpleStringProperty(personal != null ? personal.getNombreCompleto() : "No asignado");
        });

        // Cargar médicos en ComboBox
        cargarMedicosEnComboBox();
        
        // Cargar datos iniciales
        refrescarTabla();
    }

    private void cargarMedicosEnComboBox() {
        ObservableList<Empleado> medicos = FXCollections.observableArrayList();
        medicos.addAll(usuarioService.getEmpleadosPorRol(com.ingenieria.software1.model.RolEmpleado.MEDICO));
        cbMedicos.setItems(medicos);
        
        // Formatear cómo se ve el médico en la lista
        cbMedicos.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(Empleado e) { 
                return e != null ? e.getNombreCompleto() + " (" + e.getId() + ")" : "Todos los médicos"; 
            }
            @Override public Empleado fromString(String string) { return null; }
        });
        
        // Seleccionar por defecto el médico logueado
        if (medicoLogueado != null) {
            cbMedicos.setValue(medicoLogueado);
        }
        
        // Listener para cambios en la selección
        cbMedicos.setOnAction(e -> refrescarTabla());
    }

    private void refrescarTabla() {
        if (medicoLogueado == null) return;

        Empleado medicoSeleccionado = cbMedicos.getValue();
        List<Urgencia> urgencias;

        if (medicoSeleccionado != null) {
            // Filtrar por médico seleccionado
            urgencias = emergenciaService.getTodasLasUrgencias().stream()
                    .filter(u -> u.getPersonalAsignado() != null && 
                                 u.getPersonalAsignado().getId().equals(medicoSeleccionado.getId()) &&
                                 u.getEstado().equals("EN_CURSO"))
                    .collect(Collectors.toList());
        } else {
            // Mostrar todas las urgencias en curso
            urgencias = emergenciaService.getUrgenciasPendientes().stream()
                    .filter(u -> "EN_CURSO".equals(u.getEstado()))
                    .collect(Collectors.toList());
        }

        listaObservable.setAll(urgencias);
        tblUrgencias.setItems(listaObservable);
    }

    @FXML
    public void onTomarClick() {
        Urgencia seleccionada = tblUrgencias.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            lblEstadoSistema.setText("Por favor, seleccione una urgencia de la tabla.");
            return;
        }

        if (medicoLogueado == null) {
            lblEstadoSistema.setText("Error: No hay médico logueado.");
            return;
        }

        // Asignar la urgencia al médico logueado
        emergenciaService.despacharUrgencia(seleccionada.getId(), medicoLogueado);
        refrescarTabla();
        lblEstadoSistema.setText("Urgencia " + seleccionada.getId() + " tomada por " + medicoLogueado.getNombreCompleto());
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
    public void onLogoutClick() throws IOException {
        usuarioService.cerrarSesion();
        App.setRoot("view/login");
    }
}
