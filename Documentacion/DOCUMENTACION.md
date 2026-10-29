# DOCUMENTACIÓN DEL PROYECTO: SISTEMA DE GESTIÓN DE URGENCIAS
*(Versión Actualizada - Refleja estado del código con persistencia en MariaDB)*

## 1. DESCRIPCIÓN GENERAL
Este sistema optimiza el flujo de atención en una sala de urgencias hospitalaria, permitiendo el registro, clasificación (Triage), despacho y atención de pacientes de manera eficiente. Está concebido para ser utilizado por el personal de un hospital: recepcionistas, médicos, enfermeros y auxiliares.

Se trata de una aplicación de escritorio desarrollada en Java con interfaz gráfica JavaFX, que se comunica con una base de datos MariaDB para la persistencia de información.

## 2. ESPECIFICACIONES TÉCNICAS
*   **Lenguaje:** Java 21 (OpenJDK).
*   **Interfaz Gráfica:** JavaFX 21 (Arquitectura MVC).
*   **Gestión de Proyectos:** Maven.
*   **Persistencia:** Base de datos relacional **MariaDB** (accesible en `localhost:3306` o `localhost:3307` según la configuración).
*   **Librerías:**
    *   **Lombok:** Para reducir el código repetitivo (Boilerplate).
    *   **JUnit 4:** Para pruebas unitarias.
    *   **MariaDB JDBC Driver:** Para la conexión y comunicación con la base de datos.
*   **Seguridad:** Hashing de contraseñas SHA-256 para almacenamiento seguro en la base de datos.

---

## 3. LÍNEA DE TIEMPO DE DESARROLLO

### Fase 1: Cimientos y Autenticación
*   Configuración del entorno Maven y dependencias de JavaFX.
*   Implementación de la clase `App.java` y el flujo de navegación inicial.
*   Creación del `UsuarioService` (Singleton) para gestionar la sesión activa.
*   Desarrollo de la pantalla de `login.fxml` y su controlador (`LoginController`), permitiendo el acceso por roles (RECEPCIONISTA, MEDICO, ENFERMERO, AUXILIAR, ADMIN).

### Fase 2: Panel de Recepción y Simulación
*   Diseño del panel principal para el Recepcionista (`recepcionista.fxml`).
*   Implementación de una tabla dinámica para visualizar las urgencias.
*   *Nota:* La fase 2 originalmente usaba `EmergenciaService` con persistencia en memoria/CSV, pero esta funcionalidad fue reemplazada por la integración con MariaDB.

### Fase 3: Evolución al Modelo de Triage y Base de Datos
*   **Refactorización del Modelo:** Introducción de la clase `Triage` para cumplir con la normativa de salud, permitiendo niveles de prioridad del 1 al 5.
*   **Registro Detallado:** Implementación del `RegistroUrgenciaController` para capturar datos reales: Documento, Edad, Género, Síntomatología y Signos Vitales.
*   **Lógica de Alertas:** Añadido de un sistema de alertas visuales en la tabla (colores para gravedad 4 y 5) y notificaciones emergentes para casos críticos (Nivel 5).
*   **Despacho de Recursos:** Funcionalidad para que el recepcionista asigne un médico y, opcionalmente, una ambulancia a una urgencia, cambiando su estado de `PENDIENTE` a `EN_CURSO`.
*   **Migración a MariaDB:** La capa de persistencia se migró de archivos CSV a consultas JDBC a una base de datos MariaDB. Los servicios (`EmergenciaService`, `UsuarioService`) ahora utilizan DAOs (`EmergenciaDAO`, `EmpleadoDAO`, `PacienteDAO`, `AmbulanciaDAO`) para interactuar con la base de datos.
    *   La conexión se gestiona mediante el Singleton `DatabaseService`, que carga las credenciales desde `src/main/resources/config/db.properties`.
    *   El esquema de la base de datos y los datos semilla se definen en `init_db.sql` en la raíz del proyecto.

### Fase 4: Gestión de Usuarios y Seguridad Mejorada
*   **Autenticación Segura:** Implementación de hashing de contraseñas SHA-256 para almacenamiento seguro en la base de datos.
*   **Gestión Completa de Usuarios:** Creación de interfaces (`user_form.fxml`, controlada por `UserFormController`) e integración con `UserManagementService` para realizar operaciones CRUD sobre empleados.
*   **Control de Acceso por Roles:** Rol `ADMIN` añadido para gestionar el sistema de usuarios y ambulancias.
*   **Gestión de Ambulancias:** Implementación de vistas (`ambulancia_form.fxml`, `ambulancia_dialog.fxml`) y servicios (`AmbulanciaService`, `AmbulanciaDAO`) para gestionar el ciclo de vida de las ambulancias (crear, editar, eliminar, marcar estados).
*   **Corrección de Robustez (Hotfix):** Se solucionó un error de `ArrayIndexOutOfBoundsException` al cargar archivos CSV con campos vacíos al final de la línea, mejorando la estabilidad del arranque. *(Obsoleto tras la migración a MariaDB, pero documentado por histórica)*.

---

## 4. ARQUITECTURA DEL SISTEMA

### Modelos (`com.ingenieria.software1.model`)
*   **Empleado:** Representa al personal del hospital (id, nombre, usuario, contraseña_hasheada, rol).
*   **Paciente:** Datos básicos del ciudadano (id, nombre, edad, género, dirección, teléfono, historia clínica).
*   **Urgencia:** Entidad central que vincula al paciente, el triage, el personal asignado, la ambulancia y el estado.
*   **Triage:** Clasificación de gravedad (1-5) y registro de signos vitales.
*   **Ambulancia:** Información del vehículo (id, placa, estado, modelo, kilometraje, etc.).
*   **RolEmpleado:** Enumeración de roles (`RECEPCIONISTA`, `MEDICO`, `ENFERMERO`, `AUXILIAR`, `ADMIN`).

### Servicios (`com.ingenieria.software1.service`)
*   **DatabaseService:** Singleton que gestiona la conexión JDBC a MariaDB, cargando configuraciones desde `db.properties`.
*   **UsuarioService:** Gestión de personal y autenticación (Singleton).
*   **UserManagementService:** Capa intermedia para operaciones CRUD de usuarios complicadas.
*   **EmergenciaService:** Gestión del ciclo de vida de las urgencias (Singleton).
*   **AmbulanciaService:** Gestión del ciclo de vida de las ambulancias (Singleton).
*   **DAOs (Data Access Object):**
    *   **EmpleadoDAO:** Operaciones CRUD sobre la tabla `empleados`.
    *   **PacienteDAO:** Operaciones CRUD sobre la tabla `pacientes`.
    *   **UrgenciaDAO:** Operaciones CRUD y consultas complejas sobre la tabla `urgencias`.
    *   **AmbulanciaDAO:** Operaciones CRUD sobre la tabla `ambulancias`.

### Controladores (`com.ingenieria.software1.controller`)
*   **App:** Clase principal de JavaFX. Carga la vista inicial (`login.fxml`) y contiene el método `setRoot()` para navegar entre vistas. También inyecta el CSS global.
*   **LoginController:** Maneja la autenticación y redirección basada en el rol del usuario.
*   **RecepcionistaController:** Gestión de la tabla de urgencias, filtros, despacho de personal y ambulancias, alertas críticas.
*   **RegistroUrgenciaController:** Formulario para registrar nuevas urgencias con todos sus datos.
*   **MedicoController:** Panel para médicos para tomar y finalizar urgencias.
*   **PersonalMedicoController:** Panel para enfermeros y auxiliares para gestionar urgencias asignadas.
*   **UserFormController:** Formulario para crear/editar usuarios (CRUD).
*   **AmbulanciaFormController:** Panel de gestión de ambulancias.
*   **AmbulanciaDialogController:** Diálogo para crear/editar una ambulancia.
*   **AdminDashboardController:** Dashboard de administración con acceso a otras funcionalidades.

### Vistas (`com.ingenieria.software1.view`)
Definidas en archivos `.fxml` (JavaFX Markup Language), enlazadas a sus controladores mediante el atributo `fx:controller`.

---

## 5. ESTADO DE LA BASE DE DATOS

La base de datos `Emergencia_Medica` se crea automáticamente al cargar el script `init_db.sql` (usado por Docker o manualmente). Contiene las siguientes tablas:

| Tabla        | Descripción                                  |
| :----------- | :------------------------------------------- |
| `empleados`  | Personal autorizado (administradores, médicos, etc.). |
| `pacientes`  | Historico de pacientes atendidos.            |
| `urgencias`  | Registro de eventos de urgencia con su nivel de triage asociado. |
| `ambulancias`| Vehículos hospitalarios y su estado.         |

**Schema detallado:**
Consultar el archivo `init_db.sql` en la raíz del proyecto para obtener el esquema completo con claves primarias/foráneas y restricciones.

---

## 6. CONFIGURACIÓN DE LA BASE DE DATOS

Las credenciales de conexión se almacenan en `src/main/resources/config/db.properties`. Este archivo está configurado para conectarse a una instancia de MariaDB levantada mediante Docker (`docker-compose.yml`).

*   **URL:** `jdbc:mariadb://localhost:3307/Emergencia_Medica`
*   **Usuario:** `usuario_sistema`
*   **Password:** `1234`
*   **Driver:** `org.mariadb.jdbc.Driver`

Para desarrollo local sin Docker, ajusta la URL al puerto `3306` y asegúrate de tener el usuario `usuario_sistema` creado en tu instancia de MariaDB local.

---

## 7. GESTIÓN DE DEPENDENCAS

El proyecto utiliza **Apache Maven** para la gestión de dependencias y el proceso de construcción. Las dependencias clave se definen en `pom.xml`. Para compilar y/o ejecutar el proyecto, se utiliza Maven:

*   **Compilar:** `mvn compile`
*   **Ejecutar:** `mvn javafx:run`

---

## 8. PRÓXIMOS HITOS
1.  **Implementar el panel de atención específico para médicos.** (Parcialmente hecho).
2.  **Implementar estadísticas de tiempos de espera.**
3.  **Refactorizar y mejorar la cobertura de pruebas unitarias (actualmente es un esqueleto).**
4.  **Revisar y corregir errores menores de UX (navegación post-guardado, etc.).**