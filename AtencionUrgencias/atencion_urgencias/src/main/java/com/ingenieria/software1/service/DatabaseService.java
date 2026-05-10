package com.ingenieria.software1.service;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseService {
    private static DatabaseService instance;
    private String url;
    private String user;
    private String password;

    private DatabaseService() {
        cargarConfiguracion();
    }

    public static synchronized DatabaseService getInstance() {
        if (instance == null) {
            instance = new DatabaseService();
        }
        return instance;
    }

    private void cargarConfiguracion() {
        Properties props = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config/db.properties")) {
            if (input == null) {
                System.err.println("No se encontró el archivo db.properties");
                return;
            }
            props.load(input);
            this.url = props.getProperty("db.url");
            this.user = props.getProperty("db.user");
            this.password = props.getProperty("db.password");
        } catch (IOException e) {
            System.err.println("Error cargando configuración: " + e.getMessage());
        }
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    public void testConnection() {
        try (Connection conn = getConnection()) {
            System.out.println("Conexión exitosa a MariaDB: " + url);
        } catch (SQLException e) {
            System.err.println("Error de conexión: " + e.getMessage());
        }
    }
}
