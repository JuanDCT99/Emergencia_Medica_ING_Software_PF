# DOCUMENTACIÓN DEL PROYECTO: SISTEMA DE GESTIÓN DE URGENCIAS

## 1. DESCRIPCIÓN GENERAL
Este sistema ha sido diseñado para optimizar el flujo de atención en una sala de urgencias hospitalaria, permitiendo el registro, clasificación (Triage), despacho y atención de pacientes de manera eficiente.

## 2. ESPECIFICACIONES TÉCNICAS
- **Lenguaje:** Java 21.
- **Interfaz Gráfica:** JavaFX 21 (Arquitectura MVC).
- **Gestión de Proyectos:** Maven.
- **Persistencia:** Archivos planos (CSV) para asegurar portabilidad inicial.
- **Librerías:** Lombok para la reducción de código repetitivo (Boilerplate).
- **Seguridad:** Hashing de contraseñas SHA-256 para almacenamiento seguro.

---

## 3. LÍNEA DE TIEMPO DE DESARROLLO

### Fase 1: Cimientos y Autenticación
- Configuración del entorno Maven y dependencias de JavaFX.
- Implementación de la clase `App.java` y el flujo de navegación inicial.
- Creación del `UsuarioService` (Singleton) para gestionar la sesión activa.
- Desarrollo de la pantalla de `login.fxml` y su controlador, permitiendo el acceso por roles (RECEPCIONISTA, MEDICO, etc.).

### Fase 2: Panel de Recepción y Simulación
- Diseño del panel principal para el Recepcionista (`recepcionista.fxml`).
- Implementación de una tabla dinámica para visualizar las urgencias pendientes.
- Creación del `EmergenciaService` para gestionar la lógica de las urgencias en memoria.
- Integración de persistencia básica en CSV mediante `CSVService`.

### Fase 3: Evolución al Modelo de Triage (Estado Actual)
- **Refactorización del Modelo:** Se introdujo la clase `Triage` para cumplir con la normativa de salud, permitiendo niveles de prioridad del 1 al 5.
- **Registro Detallado:** Implementación del `RegistroUrgenciaController` para capturar datos reales: Documento, Edad, Género, Sintomatología y Signos Vitales.
- **Lógica de Alertas:** Se añadió un sistema de alertas visuales en la tabla (colores para gravedad 4 y 5) y notificaciones emergentes para casos críticos (Nivel 5).
- **Despacho de Recursos:** Funcionalidad para que el recepcionista asigne un médico o enfermero específico a una urgencia, cambiando su estado de `PENDIENTE` a `EN_CURSO`.
- **Corrección de Robustez (Hotfix):** Se solucionó un error de `ArrayIndexOutOfBoundsException` al cargar archivos CSV con campos vacíos al final de la línea, mejorando la estabilidad del arranque.

### Fase 4: Gestión de Usuarios y Seguridad Mejorada
- **Autenticación Segura:** Implementación de hashing de contraseñas SHA-256 para almacenamiento seguro en CSV.
- **Gestión Completa de Usuarios:** Creación de interfaz para administradores para crear, leer, actualizar y eliminar usuarios (CRUD).
- **Control de Acceso por Roles:** Nuevo rol ADMIN añadido para gestionar el sistema de usuarios.
- **Interfaces de Usuario:** Diseño de `user_management.fxml` y `user_form.fxml` para gestión intuitiva de usuarios.
- **Servicios Especializados:** Creación de `UserManagementService` como capa intermedia para operaciones de usuario.

---

## 4. ARQUITECTURA DEL SISTEMA

### Modelos (`com.ingenieria.software1.model`)
- **Empleado:** Representa al personal del hospital (nombre, usuario, contraseña, rol).
- **Paciente:** Datos básicos del ciudadano.
- **Urgencia:** Entidad central que vincula al paciente, el triage, el personal asignado y el estado.
- **Triage:** Clasificación de gravedad y registro de signos vitales.
- **RolEmpleado:** Enumeración de roles (RECEPCIONISTA, MEDICO, ENFERMERO, AUXILIAR).

### Servicios (`com.ingenieria.software1.service`)
- **CSVService:** Manejo directo de lectura y escritura de archivos en la carpeta `data/`.
- **UsuarioService:** Gestión de personal y seguridad.
- **EmergenciaService:** Cerebro del sistema; maneja la carga, registro y actualización de estados de las urgencias.

---

## 5. ESTADO DE LOS ARCHIVOS DE DATOS (`data/`)
- `empleados.csv`: Registro del personal autorizado.
- `pacientes.csv`: Histórico de pacientes atendidos.
- `urgencias.csv`: Registro de eventos de urgencia con su nivel de triage asociado.

---

## 6. PRÓXIMOS HITOS
1. Finalizar la implementación del panel de atención específico para médicos.
2. Implementar estadísticas de tiempos de espera.
3. Transición de CSV a una base de datos relacional (MariaDB/MySQL).
