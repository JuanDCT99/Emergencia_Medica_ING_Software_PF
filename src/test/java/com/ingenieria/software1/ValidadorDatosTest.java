package com.ingenieria.software1;

import com.ingenieria.software1.service.ValidadorDatos;
import org.junit.Test;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class ValidadorDatosTest {

    @Test
    public void testValidarDocumento() {
        assertNull(ValidadorDatos.validarDocumento("PAC006"));
        assertNotNull(ValidadorDatos.validarDocumento(""));
        assertNotNull(ValidadorDatos.validarDocumento("PAC 006"));
        assertNotNull(ValidadorDatos.validarDocumento("DOCUMENTO_MUY_LARGO_QUE_SUPERA_LOS_20"));
    }

    @Test
    public void testValidarNombre() {
        assertNull(ValidadorDatos.validarNombre("Sofía Martínez"));
        assertNull(ValidadorDatos.validarNombre("O'Connor"));
        assertNotNull(ValidadorDatos.validarNombre(""));
        assertNotNull(ValidadorDatos.validarNombre("Juan123"));
    }

    @Test
    public void testValidarEdad() {
        assertNull(ValidadorDatos.validarEdad("32"));
        assertNull(ValidadorDatos.validarEdad("0"));
        assertNotNull(ValidadorDatos.validarEdad(""));
        assertNotNull(ValidadorDatos.validarEdad("abc"));
        assertNotNull(ValidadorDatos.validarEdad("150"));
    }

    @Test
    public void testValidarNivelTriage() {
        assertNull(ValidadorDatos.validarNivelTriage(5));
        assertNull(ValidadorDatos.validarNivelTriage(1));
        assertNotNull(ValidadorDatos.validarNivelTriage(null));
        assertNotNull(ValidadorDatos.validarNivelTriage(0));
        assertNotNull(ValidadorDatos.validarNivelTriage(6));
    }

    @Test
    public void testValidarUsuario() {
        assertNull(ValidadorDatos.validarUsuario("admin"));
        assertNull(ValidadorDatos.validarUsuario("medico_1"));
        assertNotNull(ValidadorDatos.validarUsuario(""));
        assertNotNull(ValidadorDatos.validarUsuario("admin 123"));
    }
}