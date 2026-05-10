# Guía de Pruebas - Sistema de Gestión de Urgencias

## 1. Cómo iniciar el proyecto

```bash
# Navegar al directorio del proyecto
cd "/home/torresjdc/Escritorio/Proyectos2026-1/Ingenieria de Software/atencion_urgencias"

# Compilar
mvn compile

# Ejecutar
mvn javafx:run
```

**Pre-requisito:** MariaDB debe estar corriendo en `localhost:3306` con la base de datos `Emergencia_Medica` y los datos semilla cargados. Si no estás seguro:

```bash
mariadb -u usuario_sistema -p1234 Emergencia_Medica < src/main/resources/config/seed_data.sql
```

---

## 2. Prueba: Login y Redirección por Rol

**Objetivo:** Verificar que cada rol accede a su vista.

| Paso | Acción | Resultado esperado |
|------|--------|-------------------|
| 1 | Login: `admin` / `1234` | Abre vista de administración (`user_form`) |
| 2 | Cerrar sesión → Login: `recepcion` / `1234` | Abre vista recepcionista (`recepcionista`) |
| 3 | Cerrar sesión → Login: `medico1` / `1234` | Abre vista médica (`medico`) |
| 4 | Cerrar sesión → Login: `enfermera1` / `1234` | Abre vista personal médico (`personal_medico`) |
| 5 | Cerrar sesión → Login: `auxiliar1` / `1234` | Abre vista personal médico (`personal_medico`) |
| 6 | Login con usuario/contraseña incorrectos | Mensaje "Usuario o contraseña incorrectos" en rojo |

---

## 3. Prueba: Recepcionista - Registro de Nueva Urgencia

**Objetivo:** Verificar registro de pacientes y urgencias en MariaDB.

| Paso | Acción | Datos de ejemplo |
|------|--------|-----------------|
| 1 | Login como `recepcion` / `1234` | — |
| 2 | Click en **"Nueva Urgencia"** | Se abre ventana modal |
| 3 | Llenar formulario: | |
| | - Documento | `PAC006` |
| | - Nombre | `Sofía Martínez` |
| | - Edad | `32` |
| | - Género | Femenino |
| | - Síntomas | `Dolor torácico intenso, dificultad para respirar` |
| | - Ubicación | `Sala de espera principal` |
| | - Triage | `5` |
| | - Signos vitales | `PA: 160/100, FC: 120, SpO2: 88%` |
| 4 | Click en **Registrar** | Ventana se cierra, tabla se actualiza |

**Verificación en BD:**
```bash
mariadb -u usuario_sistema -p1234 Emergencia_Medica -e "SELECT * FROM urgencias ORDER BY fecha_registro DESC LIMIT 1;"
mariadb -u usuario_sistema -p1234 Emergencia_Medica -e "SELECT * FROM pacientes WHERE id='PAC006';"
```

> **Alerta esperada:** Si el triage es 5, debe aparecer alerta **"¡ALERTA CRÍTICA!"**.

---

## 4. Prueba: Recepcionista - Despachar Urgencia

**Objetivo:** Asignar personal médico a una urgencia pendiente.

| Paso | Acción | Resultado esperado |
|------|--------|-------------------|
| 1 | Seleccionar una urgencia en la tabla (click en fila) | Fila resaltada |
| 2 | Combo "Personal": seleccionar un profesional (ej: `Dr. Juan Pérez`) | Nombre visible en selector |
| 3 | Click en **"Despachar"** | Estado cambia a `EN_CURSO`, muestra nombre asignado |

---

## 5. Prueba: Recepcionista - Filtros de Estado

**Objetivo:** Verificar filtros de la tabla.

| Paso | Acción | Resultado esperado |
|------|--------|-------------------|
| 1 | Filtro: **"PENDIENTE"** | Solo urgencias sin despachar |
| 2 | Filtro: **"EN_CURSO"** | Solo urgencias asignadas |
| 3 | Filtro: **"TODAS"** | Todas las urgencias |

---

## 6. Prueba: Médico - Atención de Urgencias

**Objetivo:** Verificar que el médico ve sus urgencias asignadas.

| Paso | Acción | Resultado esperado |
|------|--------|-------------------|
| 1 | Login como `medico1` / `1234` | Abre vista médico |
| 2 | Revisar urgencias `EN_CURSO` asignadas a él | Deben aparecer las despachadas |

> Si hay botón "Finalizar", probarlo. Estado debe cambiar a `FINALIZADO`.

---

## 7. Prueba: Personal Médico (Enfermero/Auxiliar)

**Objetivo:** Verificar acceso.

| Paso | Acción | Resultado esperado |
|------|--------|-------------------|
| 1 | Login como `enfermera1` / `1234` | Abre `personal_medico.fxml` |
| 2 | Ver urgencias asignadas | Urgencias `EN_CURSO` visibles |

---

## 8. Prueba: Admin - Gestión de Usuarios

**Objetivo:** Verificar panel de administración.

| Paso | Acción | Resultado esperado |
|------|--------|-------------------|
| 1 | Login como `admin` / `1234` | Abre `user_form.fxml` |
| 2 | Intentar crear nuevo usuario con datos válidos | Usuario creado en BD |
| 3 | Verificar que el nuevo usuario pueda iniciar sesión | Login exitoso |

---

## Resumen de Credenciales

| Username | Rol | Contraseña |
|----------|-----|------------|
| `admin` | ADMIN | `1234` |
| `recepcion` | RECEPCIONISTA | `1234` |
| `medico1` | MEDICO | `1234` |
| `enfermera1` | ENFERMERO | `1234` |
| `auxiliar1` | AUXILIAR | `1234` |

## Datos de Prueba para Urgencia

| Campo | Valor |
|-------|-------|
| Documento | `PAC006` |
| Nombre | `Sofía Martínez` |
| Edad | `32` |
| Género | Femenino |
| Síntomas | `Dolor torácico intenso, dificultad para respirar` |
| Ubicación | `Sala de espera principal` |
| Triage | `5` |
| Signos vitales | `PA: 160/100, FC: 120, SpO2: 88%` |

---

## Reportar Errores

Si alguna prueba falla, anotar:
1. **Paso exacto** donde ocurrió el error
2. **Mensaje de error** visible en pantalla (si lo hay)
3. **Output de consola** (stack trace si aparece)
4. **Rol con el que se estaba probando**
