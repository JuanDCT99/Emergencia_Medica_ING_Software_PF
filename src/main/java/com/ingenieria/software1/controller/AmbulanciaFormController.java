package com.ingenieria.software1.controller;

import com.ingenieria.software1.App;
import com.ingenieria.software1.model.Ambulancia;
import com.ingenieria.software1.service.AmbulanciaService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class AmbulanciaFormController {

    @FXML private TableView<Ambulancia> tblAmbulancias;
    @FXML private TableColumn<Ambulancia, String> colId;
    @FXML private TableColumn<Ambulancia, String> colPlaca;
    @FXML private TableColumn<Ambulancia, String> colEstado;
    @FXML private TableColumn<Ambulancia, String> colModelo;
    @FXML private TableColumn<Ambulancia, Integer> colKilometraje;
    @FXML private TableColumn<Ambulancia, LocalDate> colFechaAlta;
    @FXML private Label lblEstado;

    private final AmbulanciaService ambulanciaService = AmbulanciaService.getInstance();
    private final ObservableList<Ambulancia> listaObservable = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colPlaca.setCellValueFactory(new PropertyValueFactory<>("placa"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colModelo.setCellValueFactory(new PropertyValueFactory<>("modelo"));
        colKilometraje.setCellValueFactory(new PropertyValueFactory<>("kilometraje"));
        colFechaAlta.setCellValueFactory(new PropertyValueFactory<>("fechaAlta"));

        colEstado.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String estado, boolean empty) {
                super.updateItem(estado, empty);
                if (empty || estado == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(estado);
                    switch (estado) {
                        case Ambulancia.ESTADO_DISPONIBLE:
                            setStyle("-fx-text-fill: #2E7D32; -fx-font-weight: bold;");
                            break;
                        case Ambulancia.ESTADO_EN_USO:
                            setStyle("-fx-text-fill: #1565C0; -fx-font-weight: bold;");
                            break;
                        case Ambulancia.ESTADO_FUERA_SERVICIO:
                            setStyle("-fx-text-fill: #C62828; -fx-font-weight: bold;");
                            break;
                        default:
                            setStyle("");
                    }
                }
            }
        });

        refrescarTabla();
    }

    private void refrescarTabla() {
        List<Ambulancia> ambulancias = ambulanciaService.obtenerTodasLasAmbulancias();
        listaObservable.setAll(ambulancias);
        tblAmbulancias.setItems(listaObservable);
        lblEstado.setText("Total de ambulancias: " + ambulancias.size());
    }

    @FXML
    public void onNuevaClick() {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("view/ambulancia_dialog.fxml"));
            Parent root = loader.load();
            
            AmbulanciaDialogController controller = loader.getController();
            controller.setModoNuevo();
            
            Stage stage = new Stage();
            stage.setTitle("Nueva Ambulancia");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            
            refrescarTabla();
        } catch (IOException e) {
            mostrarAlerta("Error", "No se pudo abrir el formulario: " + e.getMessage());
        }
    }

    @FXML
    public void onEditarClick() {
        Ambulancia seleccionada = tblAmbulancias.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            mostrarAlerta("Advertencia", "Por favor, seleccione una ambulancia para editar.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("view/ambulancia_dialog.fxml"));
            Parent root = loader.load();
            
            AmbulanciaDialogController controller = loader.getController();
            controller.setModoEdicion(seleccionada);
            
            Stage stage = new Stage();
            stage.setTitle("Editar Ambulancia");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            
            refrescarTabla();
        } catch (IOException e) {
            mostrarAlerta("Error", "No se pudo abrir el formulario: " + e.getMessage());
        }
    }

    @FXML
    public void onEliminarClick() {
        Ambulancia seleccionada = tblAmbulancias.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            mostrarAlerta("Advertencia", "Por favor, seleccione una ambulancia para eliminar.");
            return;
        }

        if (seleccionada.estaEnUso()) {
            mostrarAlerta("Error", "No se puede eliminar una ambulancia que está EN_USO.");
            return;
        }

        Optional<ButtonType> resultado = mostrarConfirmacion(
            "Confirmar Eliminación",
            "¿Está seguro que desea eliminar la ambulancia " + seleccionada.getId() + "?"
        );

        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            if (ambulanciaService.eliminarAmbulancia(seleccionada.getId())) {
                lblEstado.setText("Ambulancia eliminada correctamente.");
                refrescarTabla();
            } else {
                mostrarAlerta("Error", "No se pudo eliminar la ambulancia.");
            }
        }
    }

    @FXML
    public void onFueraServicioClick() {
        Ambulancia seleccionada = tblAmbulancias.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            mostrarAlerta("Advertencia", "Por favor, seleccione una ambulancia.");
            return;
        }

        if (seleccionada.estaEnUso()) {
            mostrarAlerta("Error", "No se puede marcar como FUERA_SERVICIO una ambulancia que está EN_USO.");
            return;
        }

        if (seleccionada.estaFueraDeServicio()) {
            Optional<ButtonType> resultado = mostrarConfirmacion(
                "Habilitar Ambulancia",
                "La ambulancia está FUERA_SERVICIO. ¿Desea marcarla como DISPONIBLE?"
            );
            if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
                ambulanciaService.marcarComoDisponible(seleccionada.getId());
                lblEstado.setText("Ambulancia marcada como DISPONIBLE.");
                refrescarTabla();
            }
        } else {
            Optional<ButtonType> resultado = mostrarConfirmacion(
                "Marcar Fuera de Servicio",
                "¿Está seguro que desea marcar la ambulancia " + seleccionada.getId() + " como FUERA_SERVICIO?"
            );
            if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
                ambulanciaService.marcarComoFueraDeServicio(seleccionada.getId());
                lblEstado.setText("Ambulancia marcada como FUERA_SERVICIO.");
                refrescarTabla();
            }
        }
    }

    @FXML
    public void onVolverClick() {
        try {
            App.setRoot("view/admin_dashboard");
        } catch (IOException e) {
            mostrarAlerta("Error", "No se pudo navegar al dashboard: " + e.getMessage());
        }
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private Optional<ButtonType> mostrarConfirmacion(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        return alert.showAndWait();
    }
}