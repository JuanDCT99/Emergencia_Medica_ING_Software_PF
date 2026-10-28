package com.ingenieria.software1.service;

import com.ingenieria.software1.model.Ambulancia;

import java.util.List;

public class AmbulanciaService {
    private static AmbulanciaService instance;
    private final AmbulanciaDAO ambulanciaDAO;

    private AmbulanciaService() {
        this.ambulanciaDAO = new AmbulanciaDAO();
    }

    public static AmbulanciaService getInstance() {
        if (instance == null) {
            instance = new AmbulanciaService();
        }
        return instance;
    }

    public Ambulancia crearAmbulancia(String id, String placa) {
        Ambulancia ambulancia = new Ambulancia(id, placa);
        if (ambulanciaDAO.crear(ambulancia)) {
            return ambulancia;
        }
        return null;
    }

    public Ambulancia crearAmbulancia(Ambulancia ambulancia) {
        if (ambulanciaDAO.crear(ambulancia)) {
            return ambulancia;
        }
        return null;
    }

    public Ambulancia obtenerPorId(String id) {
        return ambulanciaDAO.buscarPorId(id);
    }

    public List<Ambulancia> obtenerTodasLasAmbulancias() {
        return ambulanciaDAO.obtenerTodas();
    }

    public List<Ambulancia> obtenerAmbulanciasDisponibles() {
        return ambulanciaDAO.obtenerPorEstado(Ambulancia.ESTADO_DISPONIBLE);
    }

    public List<Ambulancia> obtenerAmbulanciasEnUso() {
        return ambulanciaDAO.obtenerPorEstado(Ambulancia.ESTADO_EN_USO);
    }

    public boolean actualizarAmbulancia(String id, Ambulancia ambulancia) {
        return ambulanciaDAO.actualizar(id, ambulancia);
    }

    public boolean marcarComoEnUso(String idAmbulancia) {
        Ambulancia amb = ambulanciaDAO.buscarPorId(idAmbulancia);
        if (amb != null && amb.estaDisponible()) {
            return ambulanciaDAO.actualizarEstado(idAmbulancia, Ambulancia.ESTADO_EN_USO);
        }
        return false;
    }

    public boolean marcarComoDisponible(String idAmbulancia) {
        Ambulancia amb = ambulanciaDAO.buscarPorId(idAmbulancia);
        if (amb != null && amb.estaEnUso()) {
            return ambulanciaDAO.actualizarEstado(idAmbulancia, Ambulancia.ESTADO_DISPONIBLE);
        }
        return false;
    }

    public boolean marcarComoFueraDeServicio(String idAmbulancia) {
        return ambulanciaDAO.actualizarEstado(idAmbulancia, Ambulancia.ESTADO_FUERA_SERVICIO);
    }

    public boolean eliminarAmbulancia(String id) {
        return ambulanciaDAO.eliminar(id);
    }
}