package com.ingenieria.software1.controller;

import com.ingenieria.software1.model.Urgencia;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;

/**
 * Resaltado por color de la fila de cada urgencia según su nivel de triaje (F-01 SWR.3 / F-14):
 * nivel 5 → rojo, nivel 4 → naranja, niveles 1–3 sin resaltar.
 */
final class ResaltadoTriage {

    private static final String ESTILO_NIVEL_5 = "-fx-background-color: #ff8080;";
    private static final String ESTILO_NIVEL_4 = "-fx-background-color: #ffb347;";

    private ResaltadoTriage() {
    }

    static void aplicar(TableView<Urgencia> tabla) {
        tabla.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Urgencia item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.getTriage() == null) {
                    setStyle("");
                    return;
                }
                switch (item.getTriage().getNivelPrioridad()) {
                    case 5 -> setStyle(ESTILO_NIVEL_5);
                    case 4 -> setStyle(ESTILO_NIVEL_4);
                    default -> setStyle("");
                }
            }
        });
    }
}
