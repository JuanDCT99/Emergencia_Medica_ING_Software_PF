package com.ingenieria.software1;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        scene = new Scene(loadFXML("view/login"), 640, 480);
        stage.setScene(scene);
        stage.setTitle("Sistema de Gestión de Urgencias");
        stage.show();
    }

    public static void setRoot(String fxml) throws IOException {
        System.out.println("Cargando vista: " + fxml);
        try {
            scene.setRoot(loadFXML(fxml));
            System.out.println("Vista cargada exitosamente: " + fxml);
        } catch (Exception e) {
            System.err.println("Error cargando " + fxml + ": " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    private static Parent loadFXML(String fxml) throws IOException {
        System.out.println("Intentando cargar recurso: " + fxml + ".fxml");
        var resource = App.class.getResource(fxml + ".fxml");
        if (resource == null) {
            System.err.println("ERROR: No se encontró el recurso: " + fxml + ".fxml");
            throw new IOException("No se encontró el archivo FXML: " + fxml + ".fxml");
        }
        System.out.println("Recurso encontrado: " + resource);
        FXMLLoader fxmlLoader = new FXMLLoader(resource);
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }
}
