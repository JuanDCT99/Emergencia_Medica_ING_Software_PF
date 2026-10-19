package com.ingenieria.software1.controller;

import com.ingenieria.software1.App;
import com.ingenieria.software1.model.Ambulancia;
import com.ingenieria.software1.model.Urgencia;
import com.ingenieria.software1.service.AmbulanciaService;
import com.ingenieria.software1.service.EmergenciaService;
import com.ingenieria.software1.service.UsuarioService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

public class RecepcionistaController {

    @FXML private TableView<Urgencia> tblUrgencias;
    @FXML private TableColumn<Urgencia, String> colId;
    @FXML private TableColumn<Urgencia, String> colDescripcion;
    @FXML private TableColumn<Urgencia, String> colUbicacion;
    @FXML private TableColumn<Urgencia, Integer> colGravedad;
    @FXML private TableColumn<Urgencia, LocalDateTime> colHora;
    @FXML private TableColumn<Urgencia, String> colEstado;
    @FXML private TableColumn<Urgencia, String> colAsignado;
    @FXML private TableColumn<Urgencia, String> colAmbulancia;
    
    @FXML private ComboBox<com.ingenieria.software1.model.Empleado> cbPersonal;
    @FXML private ComboBox<Ambulancia> cbAmbulancia;
    @FXML private ComboBox<String> cbFiltroEstado;
    
    @FXML private Label lblEstadoSistema;
    @FXML private Label lblTituloTabla;
    @FXML private TableView<Urgencia> tblFinalizadas;
    @FXML private TableColumn<Urgencia, String> colFinId;
    @FXML private TableColumn<Urgencia, String> colFinDescripcion;
    @FXML private TableColumn<Urgencia, String> colFinUbicacion;
    @FXML private TableColumn<Urgencia, Integer> colFinGravedad;
    @FXML private TableColumn<Urgencia, LocalDateTime> colFinHora;
    @FXML private TableColumn<Urgencia, String> colFinEstado;
    @FXML private TableColumn<Urgencia, String> colFinAsignado;
    @FXML private TableColumn<Urgencia, String> colFinAmbulancia;
    @FXML private Button btnVerFinalizadas;

    private final EmergenciaService emergenciaService = EmergenciaService.getInstance();
    private final UsuarioService usuarioService = UsuarioService.getInstance();
    private final AmbulanciaService ambulanciaService = AmbulanciaService.getInstance();
    
    private final ObservableList<Urgencia> listaObservable = FXCollections.observableArrayList();
    private final ObservableList<Urgencia> listaFinalizadas = FXCollections.observableArrayList();
    private final ObservableList<Ambulancia> listaAmbulancias = FXCollections.observableArrayList();
    
    private boolean mostrandoFinalizadas = false;

    @FXML
    public void initialize() {
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
        
        colAsignado.setCellValueFactory(cellData -> {
            var personal = cellData.getValue().getPersonalAsignado();
            return new javafx.beans.property.SimpleStringProperty(personal != null ? personal.getNombreCompleto() : "No asignado");
        });
        
        colAmbulancia.setCellValueFactory(cellData -> {
            var ambulancia = cellData.getValue().getAmbulanciaAsignada();
            return new javafx.beans.property.SimpleStringProperty(ambulancia != null ? ambulancia.getId() : "Sin asignar");
        });

        colFinId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colFinDescripcion.setCellValueFactory(cellData -> {
            var paciente = cellData.getValue().getPaciente();
            return new javafx.beans.property.SimpleStringProperty(paciente != null ? paciente.getNombreCompleto() : cellData.getValue().getSintomatologia());
        });
        colFinUbicacion.setCellValueFactory(new PropertyValueFactory<>("ubicacion"));
        colFinGravedad.setCellValueFactory(cellData -> {
            var triage = cellData.getValue().getTriage();
            return new javafx.beans.property.SimpleObjectProperty<>(triage != null ? triage.getNivelPrioridad() : 0);
        });
        colFinHora.setCellValueFactory(new PropertyValueFactory<>("horaReporte"));
        colFinEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colFinAsignado.setCellValueFactory(cellData -> {
            var personal = cellData.getValue().getPersonalAsignado();
            return new javafx.beans.property.SimpleStringProperty(personal != null ? personal.getNombreCompleto() : "No asignado");
        });
        colFinAmbulancia.setCellValueFactory(cellData -> {
            var ambulancia = cellData.getValue().getAmbulanciaAsignada();
            return new javafx.beans.property.SimpleStringProperty(ambulancia != null ? ambulancia.getId() : "Sin asignar");
        });

        cargarPersonalEnComboBox();
        cargarAmbulanciasEnComboBox();

        cbFiltroEstado.setItems(FXCollections.observableArrayList("TODAS", "PENDIENTE", "EN_CURSO", "FINALIZADO"));
        cbFiltroEstado.setValue("TODAS");

        refrescarTabla();
        refrescarTablaFinalizadas();
    }

    @FXML
    public void onFiltroEstadoChange() {
        if (!mostrandoFinalizadas) {
            refrescarTabla();
        } else {
            refrescarTablaFinalizadas();
        }
    }

    private void refrescarTablaFinalizadas() {
        List<Urgencia> urgencias = emergenciaService.getTodasLasUrgencias().stream()
                .filter(u -> "FINALIZADO".equals(u.getEstado()))
                .collect(java.util.stream.Collectors.toList());

        listaFinalizadas.setAll(urgencias);
        tblFinalizadas.setItems(listaFinalizadas);
    }

    @FXML
    public void onVerFinalizadasClick() {
        mostrandoFinalizadas = !mostrandoFinalizadas;
        
        if (mostrandoFinalizadas) {
            tblUrgencias.setVisible(false);
            tblUrgencias.setManaged(false);
            tblFinalizadas.setVisible(true);
            tblFinalizadas.setManaged(true);
            lblTituloTabla.setText("Urgencias Finalizadas");
            btnVerFinalizadas.setText("Ver Activas");
            refrescarTablaFinalizadas();
        } else {
            tblFinalizadas.setVisible(false);
            tblFinalizadas.setManaged(false);
            tblUrgencias.setVisible(true);
            tblUrgencias.setManaged(true);
            lblTituloTabla.setText("Urgencias Activas");
            btnVerFinalizadas.setText("Ver Finalizadas");
            refrescarTabla();
        }
    }

    private void cargarPersonalEnComboBox() {
        ObservableList<com.ingenieria.software1.model.Empleado> personalDisponible = FXCollections.observableArrayList();
        personalDisponible.addAll(usuarioService.getEmpleadosPorRol(com.ingenieria.software1.model.RolEmpleado.MEDICO));
        personalDisponible.addAll(usuarioService.getEmpleadosPorRol(com.ingenieria.software1.model.RolEmpleado.ENFERMERO));
        
        cbPersonal.setItems(personalDisponible);
        cbPersonal.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(com.ingenieria.software1.model.Empleado e) { 
                return e != null ? e.getNombreCompleto() + " (" + e.getRol() + ")" : ""; 
            }
            @Override public com.ingenieria.software1.model.Empleado fromString(String string) { return null; }
        });
    }

    private void cargarAmbulanciasEnComboBox() {
        listaAmbulancias.clear();
        listaAmbulancias.add(null);
        listaAmbulancias.addAll(ambulanciaService.obtenerAmbulanciasDisponibles());
        
        cbAmbulancia.setItems(listaAmbulancias);
        cbAmbulancia.setValue(null);
        
        cbAmbulancia.setConverter(new StringConverter<Ambulancia>() {
            @Override
            public String toString(Ambulancia a) {
                if (a == null) {
                    return "Sin asignar (opcional)";
                }
                return a.getId() + " - " + a.getPlaca();
            }
            @Override
            public Ambulancia fromString(String string) {
                return null;
            }
        });
    }

    private void refrescarTabla() {
        List<Urgencia> urgencias = emergenciaService.getTodasLasUrgencias();
        String filtro = cbFiltroEstado.getValue();
        
        if (filtro != null && !filtro.equals("TODAS")) {
            urgencias = urgencias.stream()
                    .filter(u -> u.getEstado().equals(filtro))
                    .collect(java.util.stream.Collectors.toList());
        }

        listaObservable.setAll(urgencias);
        tblUrgencias.setItems(listaObservable);

        tblUrgencias.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Urgencia item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || item.getTriage() == null) {
                    setStyle("");
                } else {
                    int nivel = item.getTriage().getNivelPrioridad();
                    switch (nivel) {
                        case 5 -> setStyle("-fx-background-color: #ffcccc;");
                        case 4 -> setStyle("-fx-background-color: #ffe0b2;");
                        default -> setStyle("");
                    }
                }
            }
        });
        
        cargarAmbulanciasEnComboBox();
    }

    @FXML
    public void onNuevaUrgenciaClick() throws IOException {
        javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(com.ingenieria.software1.App.class.getResource("view/registro_urgencia.fxml"));
        javafx.scene.Parent root = loader.load();
        javafx.stage.Stage stage = new javafx.stage.Stage();
        stage.setTitle("Registrar Nueva Urgencia");
        stage.setScene(new javafx.scene.Scene(root));
        stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        stage.showAndWait();
        
        refrescarTabla();
        
        List<Urgencia> urgencias = emergenciaService.getTodasLasUrgencias();
        if (!urgencias.isEmpty()) {
            Urgencia ultima = urgencias.get(urgencias.size() - 1);
            if (ultima.getTriage() != null && 
                ultima.getTriage().getNivelPrioridad() == 5 && 
                "PENDIENTE".equals(ultima.getEstado())) {
                mostrarAlertaCritica(ultima);
            }
        }
        
        lblEstadoSistema.setText("Lista de urgencias actualizada.");
    }

    private void mostrarAlertaCritica(Urgencia u) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("¡ALERTA CRÍTICA!");
        alert.setHeaderText("URGENCIA NIVEL 5 DETECTADA");
        alert.setContentText("El paciente " + u.getPaciente().getNombreCompleto() + " requiere atención inmediata.\n" +
                            "Ubicación: " + u.getUbicacion() + "\n" +
                            "Síntomas: " + u.getSintomatologia());
        alert.show();
    }

    @FXML
    public void onDespacharClick() {
        Urgencia seleccionada = tblUrgencias.getSelectionModel().getSelectedItem();
        com.ingenieria.software1.model.Empleado asignado = cbPersonal.getSelectionModel().getSelectedItem();
        Ambulancia ambulanciaSeleccionada = cbAmbulancia.getSelectionModel().getSelectedItem();

        if (seleccionada == null) {
            lblEstadoSistema.setText("Por favor, selecciona una urgencia de la tabla.");
            return;
        }

        if (asignado == null) {
            lblEstadoSistema.setText("Error: Debes asignar un profesional para despachar la urgencia.");
            return;
        }

        if (!seleccionada.getEstado().equals("PENDIENTE")) {
            lblEstadoSistema.setText("Esta urgencia ya está siendo atendida o finalizada.");
            return;
        }

        if (ambulanciaSeleccionada != null && !ambulanciaSeleccionada.estaDisponible()) {
            lblEstadoSistema.setText("La ambulancia seleccionada no está disponible.");
            return;
        }

        boolean exito;
        if (ambulanciaSeleccionada != null) {
            exito = emergenciaService.despacharUrgenciaConAmbulancia(seleccionada.getId(), asignado, ambulanciaSeleccionada);
            if (exito) {
                lblEstadoSistema.setText("Urgencia " + seleccionada.getId() + " asignada a " + 
                    asignado.getNombreCompleto() + " con Ambulancia " + ambulanciaSeleccionada.getId());
            }
        } else {
            emergenciaService.despacharUrgencia(seleccionada.getId(), asignado);
            exito = true;
            lblEstadoSistema.setText("Urgencia " + seleccionada.getId() + " asignada a " + asignado.getNombreCompleto());
        }
        
        if (exito) {
            refrescarTabla();
            cbPersonal.setValue(null);
            cbAmbulancia.setValue(null);
        } else {
            lblEstadoSistema.setText("Error al despachar la urgencia. La ambulancia podría no estar disponible.");
        }
    }

    @FXML
    public void onLogoutClick() throws IOException {
        UsuarioService.getInstance().cerrarSesion();
        App.setRoot("view/login");
    }
}