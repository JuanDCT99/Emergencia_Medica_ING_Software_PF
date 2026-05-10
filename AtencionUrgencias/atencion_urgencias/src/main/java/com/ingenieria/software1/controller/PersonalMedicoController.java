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
    @FXML private ComboBox<Empleado> cbPersonal;
    @FXML private TableView<Urgencia> tblUrgencias;
    @FXML private TableColumn<Urgencia, String> colId;
    @FXML private TableColumn<Urgencia, String> colDescripcion;
    @FXML private TableColumn<Urgencia, String> colUbicacion;
    @FXML private TableColumn<Urgencia, Integer> colGravedad;
    @FXML private TableColumn<Urgencia, LocalDateTime> colHora;
    @FXML private TableColumn<Urgencia, String> colEstado;
    @FXML private Label lblEstadoSistema;
    @FXML private Label lblTareas;

    private final EmergenciaService emergenciaService = EmergenciaService.getInstance();
    private final UsuarioService usuarioService = UsuarioService.getInstance();
    private final ObservableList<Urgencia> listaObservable = FXCollections.observableArrayList();
    private Empleado personalLogueado;

    @FXML
    public void initialize() {
        personalLogueado = usuarioService.getUsuarioLogueado();
        if (personalLogueado != null) {
            lblBienvenido.setText("Bienvenido, " + personalLogueado.getNombreCompleto() + " (" + personalLogueado.getRol() + ")");
            
            // Configurar tareas según el rol
            configurarTareasSegunRol();
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
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        // Cargar personal en ComboBox según el rol del logueado
        cargarPersonalEnComboBox();
        
        // Listener para cambios en la selección del ComboBox
        cbPersonal.setOnAction(e -> refrescarTabla());

        refrescarTabla();
    }

    private void configurarTareasSegunRol() {
        if (personalLogueado == null) return;
        
        String tareas;
        switch (personalLogueado.getRol()) {
            case ENFERMERO:
                tareas = "• Revisar signos vitales • Administrar medicamentos • Actualizar triage • Apoyar en procedimientos";
                break;
            case AUXILIAR:
                tareas = "• Trasladar pacientes • Traer equipos médicos • Apoyar en logística • Mantener área limpia";
                break;
            default:
                tareas = "Sin tareas asignadas";
        }
        lblTareas.setText(tareas);
    }

    private void cargarPersonalEnComboBox() {
        ObservableList<Empleado> personal = FXCollections.observableArrayList();
        
        if (personalLogueado != null) {
            switch (personalLogueado.getRol()) {
                case ENFERMERO:
                    personal.addAll(usuarioService.getEmpleadosPorRol(com.ingenieria.software1.model.RolEmpleado.ENFERMERO));
                    break;
                case AUXILIAR:
                    personal.addAll(usuarioService.getEmpleadosPorRol(com.ingenieria.software1.model.RolEmpleado.AUXILIAR));
                    break;
                default:
                    // Por defecto cargar todos
                    personal.addAll(usuarioService.getEmpleadosPorRol(com.ingenieria.software1.model.RolEmpleado.ENFERMERO));
                    personal.addAll(usuarioService.getEmpleadosPorRol(com.ingenieria.software1.model.RolEmpleado.AUXILIAR));
            }
        }
        
        cbPersonal.setItems(personal);
        
        // Formatear cómo se ve el empleado en la lista
        cbPersonal.setConverter(new javafx.util.StringConverter<>() {
            @Override 
            public String toString(Empleado e) { 
                return e != null ? e.getNombreCompleto() + " (" + e.getId() + ")" : "Todos"; 
            }
            @Override 
            public Empleado fromString(String string) { 
                return null; 
            }
        });
        
        // Seleccionar por defecto el personal logueado
        if (personalLogueado != null) {
            cbPersonal.setValue(personalLogueado);
        }
    }

    private void refrescarTabla() {
        if (personalLogueado == null) return;

        Empleado seleccionado = cbPersonal.getValue();
        List<Urgencia> urgencias;

        if (seleccionado != null) {
            // Filtrar por personal seleccionado
            urgencias = emergenciaService.getTodasLasUrgencias().stream()
                    .filter(u -> u.getPersonalAsignado() != null && 
                                 u.getPersonalAsignado().getId().equals(seleccionado.getId()) &&
                                 u.getEstado().equals("EN_CURSO"))
                    .collect(Collectors.toList());
        } else {
            // Mostrar todas las urgencias en curso del mismo rol
            List<Empleado> personalDelRol = usuarioService.getEmpleadosPorRol(personalLogueado.getRol());
            urgencias = emergenciaService.getTodasLasUrgencias().stream()
                    .filter(u -> u.getPersonalAsignado() != null && 
                                 u.getEstado().equals("EN_CURSO") &&
                                 personalDelRol.stream().anyMatch(e -> e.getId().equals(u.getPersonalAsignado().getId())))
                    .collect(Collectors.toList());
        }

        listaObservable.setAll(urgencias);
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
