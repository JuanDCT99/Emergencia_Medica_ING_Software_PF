package com.ingenieria.software1.service;

import java.util.regex.Pattern;

/**
 * Validaciones de dominio (F-04): el sistema solo acepta datos que pertenezcan al dominio
 * del campo y le indica al usuario el tipo de dato requerido en cada segmento.
 * Los límites de longitud coinciden con las columnas de la base de datos existente.
 * Cada metodo devuelve null si el dato es válido, o el mensaje de error si no lo es.
 */
public final class ValidadorDatos {

    public static final int NIVEL_TRIAGE_MIN = 1;
    public static final int NIVEL_TRIAGE_MAX = 5;
    public static final int NIVEL_TRIAGE_POR_DEFECTO = 3;
    public static final int EDAD_MAX = 120;

    private static final Pattern DOCUMENTO = Pattern.compile("[A-Za-z0-9]+");
    private static final Pattern NOMBRE = Pattern.compile("[\\p{L}]+([ .'\\-]+[\\p{L}]+)*[.]?");
    private static final Pattern USUARIO = Pattern.compile("[A-Za-z0-9._\\-]+");

    private ValidadorDatos() {
    }

    // Documento de identidad: solo letras y números, máximo 20 caracteres (pacientes.id / empleados.id).
    public static String validarDocumento(String valor) {
        if (esVacio(valor)) return "El documento es requerido (solo letras y números, sin espacios).";
        String v = valor.trim();
        if (v.length() > 20) return "El documento no puede superar 20 caracteres.";
        if (!DOCUMENTO.matcher(v).matches()) return "El documento solo admite letras y números, sin espacios ni símbolos.";
        return null;
    }

    // Nombre completo: solo letras (con tildes), espacios, punto, apóstrofo y guion; máximo 100 caracteres.
    public static String validarNombre(String valor) {
        if (esVacio(valor)) return "El nombre completo es requerido (solo letras).";
        String v = valor.trim();
        if (v.length() > 100) return "El nombre no puede superar 100 caracteres.";
        if (!NOMBRE.matcher(v).matches()) return "El nombre solo admite letras y espacios (no números ni símbolos).";
        return null;
    }

    // Edad: número entero entre 0 y 120.
    public static String validarEdad(String valor) {
        if (esVacio(valor)) return "La edad es requerida (número entero entre 0 y " + EDAD_MAX + ").";
        int edad;
        try {
            edad = Integer.parseInt(valor.trim());
        } catch (NumberFormatException e) {
            return "La edad debe ser un número entero (sin letras ni decimales).";
        }
        if (edad < 0 || edad > EDAD_MAX) return "La edad debe estar entre 0 y " + EDAD_MAX + ".";
        return null;
    }

    // Nivel de triaje: únicamente valores 1 a 5.
    public static String validarNivelTriage(Integer nivel) {
        if (nivel == null) return "El nivel de triaje es requerido (valor entre 1 y 5).";
        if (nivel < NIVEL_TRIAGE_MIN || nivel > NIVEL_TRIAGE_MAX) return "El nivel de triaje solo admite valores de 1 a 5.";
        return null;
    }

    // Texto libre obligatorio con longitud máxima (la base usa TEXT para síntomas: sin límite práctico).
    public static String validarTextoRequerido(String valor, String campo) {
        if (esVacio(valor)) return "El campo " + campo + " es requerido.";
        return null;
    }

    // Texto libre opcional con longitud máxima.
    public static String validarTextoOpcional(String valor, String campo, int maximo) {
        if (valor != null && valor.trim().length() > maximo) {
            return "El campo " + campo + " no puede superar " + maximo + " caracteres.";
        }
        return null;
    }

    // Nombre de usuario: letras, números, punto, guion y guion bajo; máximo 50 caracteres (empleados.usuario).
    public static String validarUsuario(String valor) {
        if (esVacio(valor)) return "El nombre de usuario es requerido (letras, números, punto, guion o guion bajo).";
        String v = valor.trim();
        if (v.length() > 50) return "El nombre de usuario no puede superar 50 caracteres.";
        if (!USUARIO.matcher(v).matches()) return "El nombre de usuario solo admite letras, números, punto, guion y guion bajo (sin espacios).";
        return null;
    }

    private static boolean esVacio(String valor) {
        return valor == null || valor.trim().isEmpty();
    }
}