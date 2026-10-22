package com.ingenieria.software1.controller;

import com.ingenieria.software1.model.Urgencia;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;

final class ResaltadoTriage {

    private static final String ESTILO_NIVEL_5 = "-fx-background-color: #ff8080;";
    private static final String ESTILO_NIVEL_4 = "-fx-background-color: #ffb347;";

    private ResaltadoTriage() {
    }

    static void aplicar(TableView<Urgencia> tableView) {
        tableView.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Urgencia urgencia, boolean empty) {
                super.updateItem(urgencia, empty);
                if (empty || urgencia == null || urgencia.getTriage() == null) {
                    setStyle("");
                    return;
                }
                switch (urgencia.getTriage().getNivelPrioridad()) {
                    case 5 -> setStyle(ESTILO_NIVEL_5);
                    case 4 -> setStyle(ESTILO_NIVEL_4);
                    default -> setStyle("");
                }
            }
        });
    }
}
