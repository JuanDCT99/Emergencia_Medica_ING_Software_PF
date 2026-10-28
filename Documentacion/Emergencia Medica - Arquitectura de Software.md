# Emergencia Medica — Arquitectura de Software

**Proyecto:** Sistema de Gestión de Urgencias Hospitalarias  
**Universidad:** Universidad del Quindío — Ingeniería de Sistemas y Computación  
**Autor:** Juan David Cardozo Torrez  
**Fecha:** 13 de octubre de 2026  
**Referencia:** `atencion_urgencias/` (módulo principal del repositorio `Emergencia_Medica_ING_Software_PF`)

---

## Introducción

### Breve descripción del sistema y su propósito

El **Sistema de Gestión de Urgencias Hospitalarias** es una aplicación de escritorio diseñada para optimizar el flujo de atención en una sala de urgencias. Permite:

- Registrar pacientes y sus **urgencias** con clasificación de gravedad (**Triage** 1–5).
- **Despachar** personal médico y ambulancias respectivas.
- Atender y **finalizar** las urgencias, liberando los recursos asignados.
- Gestionar el **personal** (roles: Recepcionista, Médico, Enfermero, Auxiliar, Administrador) y la **flota de ambulancias**.

El sistema persiste toda la información en una base de datos **MariaDB**, de modo que los datos no se pierden al cerrar la aplicación, y permite a cada rol ver solamente lo que le corresponde según su perfil.

### Objetivos del informe

- Documentar de forma estructurada la arquitectura del software: capas, componentes, responsabilidades y relaciones.
- Mostrar los patrones de diseño aplicados y las decisiones de diseño que sustentan el sistema.
- Dejar claro cómo se despliega, se configura y se protege la aplicación en producción.
- Facilitar la mantenibilidad, evolución y conocimiento por parte de futuros desarrolladores.

---

## Contexto del sistema

### Descripción del dominio del problema

En una sala de urgencias, la atención de pacientes requiere:

1. **Apertura rápida del caso:** el recepcionista registra al paciente, anota síntomas, ubicación y la gravedad inicial (Triage).
2. **Priorización inmediata:** los niveles de gravedad (1–5, donde 5 es lo más crítico) condicionan la atención y alertas visuales.
3. **Despacho coordinado:** el recepcionista asigna al médico/enfermero correspondiente y, opcionalmente, una ambulancia disponible. Esto evita enviar un recurso ya ocupado a otro paciente.
4. **Seguimiento hasta el cierre:** el médico revisa la urgencia asignada, toma nota, finaliza el caso, y el sistema libera la ambulancia (si la había).
5. **Traza de recursos:** el estado de cada ambulancia pasa de DISPONIBLE → EN_USO → FUERA_SERVICIO, y al finalizar regresa a DISPONIBLE.

Todo esto genera datos clínicos y de personal que deben conservarse (no se pueden perder al cerrar la sesión) y que requieren control de acceso según rol.

### Requerimientos y restricciones del sistema

| Requerimiento | Detalle |
|---|---|
| **Requerimiento funcional** | Registrar, clasificar, despachar, atender y cerrar urgencias. |
| **Requerimiento de roles** | Cada perfil (Recepcionista, Médico, Enfermero, Auxiliar, Admin) ve y ejecuta solo lo suyo. |
| **Requerimiento de persistencia** | Los datos de las urgencias, pacientes, empleados y ambulancias deben sobrevivir al cierre de la sesión. |
| **Requerimiento de priorización** | Las urgencias con nivel de triage 4 y 5 deben verse destacadas y deben generar alerta crítica. |
| **Requerimiento de recursos** | Controlar que una ambulancia no se asigne dos veces al mismo tiempo. |
| **Restricción técnica** | Interfaz gráfica construida con JavaFX (MVC), persistencia mediante JDBC/MariaDB. |
| **Restricción de seguridad** | Contraseñas almacenadas como hash; control de acceso por roles; manejo de excepciones. |

### Tecnologías utilizadas

| Capa | Tecnología | Versión | Nota |
|---|---|---|---|
| **Lenguaje** | Java | 21 (JDK) | Inicialización de recursos, Lombok ahorra boilerplate. |
| **Interfaz gráfica** | JavaFX | 21.0.1 | MVC, FXML, CSS, controles de tablas y combobox. |
| **Gestión de dependencias** | Apache Maven | 3.8+ | Compilación, resolución de dependencias, ejecución del FX. |
| **Persistencia** | JDBC + MariaDB | 3.3.3 (driver) / 11.8 (imagen) | Conexión desde Java, objetos `PreparedStatement`, `ResultSet`. |
| **Seguridad de contraseñas** | SHA-256 + prefijo | — | Hash de la clave antes de guardar; verificado en login. |
| **Orquestación de BD** | Docker Compose | — | `docker compose up -d` levanta un contenedor MariaDB 11.8. |
| **Datos de prueba** | Carga semilla | — | `init_db.sql` y `docker-entrypoint-initdb.d/001-init-db.sql`. |

---

## Arquitectura de alto nivel

### Descripción general

El sistema sigue una arquitectura **por capas (n-tier)**, separando claramente:

1. **Vista** (JavaFX / FXML / CSS) — Lo que ve el usuario, los componentes gráficos y la navegación.
2. **Controlador** (JavaFX Controllers) — Wires los eventos de la vista con la lógica de negocio.
3. **Servicio** (Lógica de negocio) — Reglas de negocio, flujos de guardado, asignación de recursos.
4. **DAO** (Acceso a datos) — Persistencia con MariaDB, consultas preparadas, mapeo de resultados.
5. **Modelo** — Entidades del dominio (Paciente, Urgencia, Empleado, Triage, Ambulancia, RolEmpleado) en memoria.
6. **Base de datos** — MariaDB: tablas `empleados`, `pacientes`, `urgencias`, `ambulancias`; con tablas auxiliares de seed.

La principal ventaja es que el cambio de base de datos (o de una capa intermedia) se realiza **solo en la capa DAO/Servicio**, sin afectar la vista ni la lógica de negocio.

### Patrones arquitectónicos aplicados

| Patrón | Uso |
|---|---|
| **MVC (Modelo-Vista-Controlador)** | Cada pantalla FXML + su Controlador. |
| **Singleton** | `DatabaseService`, `UsuarioService`, `EmergenciaService`, `AmbulanciaService`, `UserManagementService`: recursos únicos del sistema. |
| **DAO (Data Access Object)** | `EmpleadoDAO`, `PacienteDAO`, `UrgenciaDAO`, `AmbulanciaDAO`: encapsulan cada operación de BD. |
| **Strategy (implícito)** | El `RolEmpleado` decide qué vistas y acciones se habilitan. |
| **Command/Event** | Los controles JavaFX (`Button`, `ComboBox`, `TableView`) disparan `ActionEvent`-es que invocan el servicio. |

### Diagrama de arquitectura de alto nivel (texto)

```
┌─────────────┐      ┌──────────────┐      ┌──────────────┐
│   VISTA     │ ───▶ │ CONTROLADOR  │ ───▶ │   SERVICIO   │
│  (FXML/CSS) │      │  (Java)      │      │  (Java)      │
└─────────────┘      └──────────────┘      └──────┬───────┘
                                                   │
┌─────────────┐      ┌──────────────┐      ┌──────▼───────┐
│ MODELO     │ ◀──── │  APP         │ ◀──── │  DAO (JDBC)  │
│  (POJOs)   │      │ (clase       │      │  (MariaDB)   │
│            │      │  App.java)   │      │              │
└─────────────┘      └──────────────┘      └──────────────┘
```

- **Vista → Controlador:** el usuario marca un botón, selecciona una fila; el controlador recibe el evento.
- **Controlador → Servicio:** el controlador delega la regla (login, despacho, finalizar) al servicio correspondiente.
- **Servicio → DAO:** el servicio consulta/escribe en la base de datos a través de las clases DAO.
- **DAO → DB:** conversión a SQL `PreparedStatement`, ejecución y mapeo a objetos Java (ResultSet → Entidad).

---

## Componentes y módulos

### Modelo (`com.ingenieria.software1.model`)

| Clase | Responsabilidad |
|---|---|
| `Paciente` | Datos básicos del ciudadano: documento, nombre, edad, género, dirección, teléfono, historia clínica. |
| `Urgencia` | Entidad central: ID, paciente, síntomas, ubicación, triage (gravedad), hora de reporte, estado, personal asignado, ambulancia asignada. |
| `Triage` | Resultado de la evaluación inicial: ID del triage, nivel de prioridad (1–5), signos vitales, hora de evaluación. |
| `Empleado` | Personal del hospital: ID, nombre completo, usuario, contraseña (hash), rol. |
| `Ambulancia` | Recurso: ID, placa, estado (DISPONIBLE/EN_USO/FUERA_SERVICIO), modelo, kilometraje, fecha alta, observaciones. |
| `RolEmpleado` | Enum de los roles del sistema. |
| `App` | Clase principal (JavaFX Application): carga la vista inicial, navega entre pantallas y carga el CSS global. |

### Controladores (Vista + Lógica de pantalla)

| Controlador | Vértice | Responsabilidad |
|---|---|---|
| `LoginController` | `login.fxml` | Autentica usuario contra `UsuarioService`; redirige al dashboard del rol correspondiente. |
| `RecepcionistaController` | `recepcionista.fxml` | Vista principal del recepcionista: tabla de urgencias (con filtro), alertas de gravedad, registro modal de urgencia, despacho (personal + ambulancia). |
| `RegistroUrgenciaController` | `registro_urgencia.fxml` | Formulario modal: documento, nombre, edad, género, síntomas, ubicación, triage (1–5), signos vitales → guarda en `EmergenciaService`. |
| `MedicoController` | `medico.fxml` | Panel del médico: muestra sus urgencias EN_CURSO, permite "tomar" una y finalizarla. |
| `PersonalMedicoController` | `personal_medico.fxml` | Panel de enfermería/auxiliar: filtra por su rol, finaliza urgencias, muestra tareas sugeridas por rol. |
| `AdminDashboardController` | `admin_dashboard.fxml` | Enlace a gestión de usuarios y ambulancias. |
| `UserFormController` | `user_form.fxml` | CRUD de usuarios: crea/edita empleado, valida formulario (documento, usuario, contraseña, rol). |
| `AmbulanciaFormController` | `ambulancia_form.fxml` | CRUD de ambulancias: lista, crea, edita, elimina (bloqueado si en uso), cambia estado DISPONIBLE/FUERA_SERVICIO. |
| `AmbulanciaDialogController` | `ambulancia_dialog.fxml` | Formulario interno para crear/editar una ambulancia. |

### Servicios y DAOs

| Capa | Clase | Responsabilidades |
|---|---|---|
| **Servicio** | `EmergenciaService` | Lógica central: registrar una urgencia real, obtener pendientes/todas, despachar (con o sin ambulancia), finalizar (liberando ambulancia). |
| **Servicio** | `UsuarioService` | Autenticación, gestión de sesión (login/logout/cerrarSesion), rol-based lookups. |
| **Servicio** | `AmbulanciaService` | Lista de ambulancias, estado, disponibilidad, marcar como EN_USO/DISPONIBLE/FUERA_SERVICIO. |
| **Servicio** | `UserManagementService` | CRUD de usuarios (crear, actualizar, eliminar, buscar por documento/usuario). |
| **DAO** | `DatabaseService` | Adaptador de conexión: carga `db.properties`, expone `getConnection()` y `testConnection()`. |
| **DAO** | `EmpleadoDAO` | CRUD de empleados (login, consultar, insertar, actualizar, eliminar). |
| **DAO** | `PacienteDAO` | CRUD de pacientes, búsqueda por ID. |
| **DAO** | `UrgenciaDAO` | CRUD de urgencias, filtros por estado, actualizaciones de estado, mapeo de resultados a objetos. |
| **DAO** | `AmbulanciaDAO` | CRUD de ambulancias, búsqueda y estado. |

### Capa de datos (MariaDB)

Tablas creadas por `init_db.sql` / `001-init-db.sql`:

- `empleados`: id, nombre_completo, usuario, contrasena_hash, rol (FK a perfil).
- `pacientes`: id, nombre_completo, edad, genero, direccion, telefono, historia_clinica.
- `ambulancias`: id, placa, estado, modelo, kilometraje, ultima_revision, fecha_alta, observaciones.
- `urgencias`: id, paciente_id, empleado_id, nivel_triage, sintomas, ubicacion, signos_vitales, estado, fecha_registro, ambulancia_id

Las relaciones fuerzan que una urgencia pertenezca a un paciente, un empleado (médico asignado) y, opcionalmente, una ambulancia.

---

## Interacciones y flujos de datos

### Escenario 1 — Registro de una nueva urgencia

```
Recepcionista.fxml (botón "Nueva Urgencia")
  → RegistroUrgenciaController (formulario modal)
    → emergenciaService.registrarUrgenciaReal(paciente, síntomas, ubicación, triage, signos)
      → PacienteDAO.crear(paciente)   [si el paciente no existía]
      → UrgenciaDAO.crear(urgencia)  [INSERT en tabla urgencias]
    → el modal se cierra
Vuelve al RecepcionistaController → refresca la tabla (tabla se actualiza con la nueva urgencia)
Si la nueva urgencia es nivel 5 y está PENDIENTE → muestra "¡ALERTA CRÍTICA!" (ventana emergente)
```

### Escenario 2 — Despacho de un médico y/o ambulancia

```
RecepcionistaController (tabla: fila seleccionada + ComboBox de personal + ComboBox de ambulancia)
  → emergenciaService.despacharUrgenciaConAmbulancia(idUrgencia, empleado, ambulancia)
    → si ambulancia != null:
        AmbulanciaService.obtenerPorId(id) → verificar estaDisponible()
        AmbulanciaService.marcarComoEnUso(id)
        UrgenciaDAO.actualizarEstadoConAmbulancia(id, "EN_CURSO", empleadoId, ambulanciaId)
    → si ambulancia == null:
        UrgenciaDAO.actualizarEstado(id, "EN_CURSO", empleadoId)
Resultado visual: la fila pasa a estado EN_CURSO; el combo personal y ambulancia se resetea.
```

### Escenario 3 — Atención médica y cierre

```
MedicoController → emergenciaService.finalizarUrgencia(idUrgencia)
  → UrgenciaDAO.buscarPorId(id) → si tiene ambulanciaAsignada:
      AmbulanciaService.marcarComoDisponible(idAmbulancia)
  → UrgenciaDAO.actualizarEstado(id, "FINALIZADO", null)
```

### Escenario 4 — Gestión de recursos (Admin)

```
AdminDashboardController → AmbulanciaFormController → AmbulanciaService
  → CRUD en la BD (tabla ambulancias): insertar, actualizar, eliminar (bloqueado si EN_USO), cambiar estado.
```

### Flujo de datos principales

```
Usuarios (login) ──► UsuarioService ──► EmpleadoDAO ──► MariaDB (empleados)
Urgentes (registro) ──► EmergenciaService ──► UrgenciaDAO ──► MariaDB (urgencias)
Recursos (despacho) ──► EmergenciaService ──► UrgenciaDAO + AmbulanciaService ──► MariaDB (urgencias, ambulancias)
Personal (admin) ──► UserFormController ──► UserManagementService ──► EmpleadoDAO ──► MariaDB
```

---

## Decisiones de diseño y justificaciones

| Decisión de diseño | Justificación |
|---|---|
| **Java 21 + JavaFX 21** | Framework gráfico de Java con soporte nativo para MVC (FXML, CSS, Controllers). Estable, ampliamente utilizado en apps empresariales. |
| **Maven** | Manejo de dependencias y ciclo de compilación. Permite a cualquier desarrollador compilar con un único comando (`mvn compile`), descargando las librerías necesarias. |
| **MariaDB (Docker)** | Reproducible en cualquier máquina; el script `init_db.sql` configura usuario, tablas y datos semilla en un solo comando de `docker compose up -d`. |
| **JDBC con PreparedStatement** | Evita inyección SQL; representa el estándar de acceso a BD con Java. El código es explícito y no depende de un ORM (más transparencia para un trabajo universitario). |
| **Lombok** | Reduce código repetitivo (getters/setters/constructores). La clase se escribe con anotaciones `@Data` y el código generado se mantiene fuera del repositorio (`target/classes`). |
| **Singleton para servicios** | `DatabaseService`, `UsuarioService`, `EmergenciaService`, `AmbulanciaService`, `UserManagementService` son recursos únicos del sistema; previene múltiples instancias que puedan ser incoherentes. |
| **Hash SHA-256 de contraseñas** | Aún no es una solución robusta para producción (falta sal y costo), pero cumple para un entorno académico: no guarda la clave en texto plano. |
| **Tabla de estado `ENUM` en BD** | `PENDIENTE`, `EN_CURSO`, `FINALIZADO` para urgencias; `DISPONIBLE`, `EN_USO`, `FUERA_SERVICIO` para ambulancias. El control de estado está en la base y refleja el dominio. |

### Aspectos de rendimiento, escalabilidad, mantenibilidad

- **Rendimiento:** La app es cliente-servidor simple; no hay requsitos de alta concurrencia. Para un hospital pequeño-escalado, la consulta es suficiente.
- **Escalabilidad:** El diseño por capas permite migrar a una base de datos más robusta (PostgreSQL, Oracle) cambiando solo la capa DAO.
- **Mantenibilidad:** El dominio está en `model/`; la regla de negocio en `service/`; la persistencia en `DAO/`; la interfaz en `controller/` — cada capa tiene una responsabilidad clara.
- **Seguridad:** El acceso a la BD se hace con MySQL con `usuario_sistema` limitado a `Emergencia_Medica.*`; no se usa usuario root desde la aplicación.

---

## Patrones de diseño utilizados

El sistema no es "un solo patrón"; es una aplicación que aplica varios, de forma natural:

| Patrón | Dónde se aplica | Ejemplo concreto |
|---|---|---|
| **Singleton** | Servicios de registro único | `DatabaseService.getInstance()`, `UsuarioService.getInstance()`, `EmergenciaService.getInstance()`, `AmbulanciaService.getInstance()`, `UserManagementService` (instancia directa en `UserFormController`). |
| **DAO** | Acceso a datos | `UrgenciaDAO`, `PacienteDAO`, `EmpleadoDAO`, `AmbulanciaDAO` — cada uno envuelve una tabla. |
| **MVC** | Cada pantalla | `login.fxml` + `LoginController`, `recepcionista.fxml` + `RecepcionistaController`, etc. |
| **Strategy (implícito)** | Rol del empleado | `RolEmpleado` define el perfil; el LoginController selecciona la vista según el rol (switch de roles). |
| **Factory (implícito)** | Creación de objetos | `EmergenciaService` crea instancias de `Urgencia` internamente; `RegistroUrgenciaController` crea `Paciente` desde el formulario. |

---

## Gestión de la calidad y pruebas

### Estrategia de prueba actual

El proyecto incluye un esqueleto de pruebas unitarias:

- **`AppTest.java`** (JUnit 4) — solo verifica `assertTrue(true)`. No cubre la lógica de negocio.

No existen pruebas automatizadas para:

- Autenticación (`UsuarioService`).
- Registrar/filtrar/despachar/finalizar urgencias (`EmergenciaService`).
- Estado de ambulancias (`AmbulanciaService`).
- CRUD de usuarios (`UserManagementService`).
- Conexión a la base de datos (`DatabaseService`).

### Métricas de calidad observadas

| Métrica | Observación |
|---|---|
| **Cobertura de código** | Bajo; el código es funcional pero los tests son un placeholder. |
| **Calidad de lectura** | Buena; es la estructura tradicional de Java con Lombok que reduce ruido. |
| **Manejo de errores** | Se capturan excepciones `SQLException` en cada DAO e imprimen mensaje en consola; no hay mecanismo centralizado de excepciones. |
| **Documentación** | La base del proyecto tiene `DOCUMENTACION.md`, `GUIA_PRUEBAS.md` y `REPORTE_ERRORES.md`. |
| **Refactorización** | Hay un "hotfix" mencionado en la documentación para evitar `ArrayIndexOutOfBoundsException` en el manejo de archivos CSV (ya se migró a BD). |

### Próximas acciones recomendadas

- Añadir pruebas unitarias reales para `UsuarioService`, `EmergenciaService` y `AmbulanciaService` (con mocks de `DatabaseService`, o ejecutar contra una BD de Docker).
- Eliminar el código muerto: `super_simple.fxml`, `ultra_simple.fxml`, `AdminTestController.java` (no están enlazados).
- Consolidar la dependencia `javafx-graphics` en `pom.xml` (redundante).

---

## Despliegue y configuración

### Entorno de desarrollo (todos los módulos)

1. **Requisitos previos:**
   - JDK 21 (o superior).
   - Apache Maven 3.8+.
   - Docker Desktop (recomendado para la base de datos).
2. **Iniciar la base de datos:**
   ```bash
   cd atencion_urgencias
   docker compose up -d
   ```
3. **Compilar y ejecutar la aplicación:**
   ```bash
   mvn javafx:run
   ```
4. (Opcional) Verificar el contenedor: `docker compose ps`.

### Configuración de la base de datos (hardcodeado en `db.properties`)

```properties
db.url=jdbc:mariadb://localhost:3307/Emergencia_Medica
db.user=usuario_sistema
db.password=1234
db.driver=org.mariadb.jdbc.Driver
```

Si se usa un perfil sin Docker (MariaDB instalado directamente), el puerto cambia a `3306` y se asegura el usuario `usuario_sistema` con la contraseña correspondiente.

### Proceso de despliegue

- **Modo local:** El contenedor de MariaDB se levanta con `docker compose up -d` y se apunta desde `db.properties`.
- **Modo sin Docker:** Se ejecuta MariaDB localmente, se crea la BD `Emergencia_Medica`, el usuario `usuario_sistema` y se carga `init_db.sql`.
- **Actualización:** Al subir nuevas versiones del código en un repositorio compartido, los miembros del equipo ejecutan `git pull`, luego `mvn javafx:run` y si cambia el esquema, se actualiza el archivo `init_db.sql` para que todos comenzen con el mismo estado.

### Infraestructura que se ejecuta en producción

| Componente | Tipo | Puerto | Nota |
|---|---|---|---|
| `urgencias_mariadb` | Contenedor Docker (MariaDB 11.8) | 3307 | Volumen `urgencias_data` para persistencia. |
| `atencion_urgencias` | Aplicación JavaFX (JVM) | — | Ejecutada desde el IDE o `mvn javafx:run`. |

---

## Consideraciones de seguridad

### Medidas implementadas actualmente

- **Hash de contraseñas:** SHA-256 con prefijo `SHA-256:` antes del hash almacenado. La verificación en login re-hashifica la clave ingresada y la compara.
- **Control de acceso por roles:** `LoginController` delega la autenticación a `UsuarioService` y luego redirige según `RolEmpleado`.
- **Credenciales en archivo de configuración:** `db.properties` guarda usuario y contraseño; ese archivo está en `src/main/resources`, no en producción. Se recomienda que en un entorno real use variables de entorno o un gestor de secretos.
- **Validación de formularios:** Los controles de texto obligatorios (`txtDocumento`, `txtUsuario`, `txtNombreCompleto`, `txtContrasena`, `txtConfirmarContrasena`, etc.) son solicitados y verificados antes de guardar. La contraseña se valida al longitud y coincidencia.

### Gestión de roles y permisos

| Rol | Permisos |
|---|---|
| `ADMIN` | Gestionar usuarios (CRUD), gestionar ambulancias (CRUD), ver dashboard. |
| `RECEPCIONISTA` | Registrar urgencias, filtrar, despachar (asignar médico/ambulancia), ver finalizadas. |
| `MEDICO` | Ver/ver urgencias asignadas, tomarlas (si no están asignadas), finalizar. |
| `ENFERMERO` / `AUXILIAR` | Ver urgencias asignadas al rol, finalizar. Tareas sugeridas según rol. |

### Limitaciones de seguridad detectadas (a resolver)

- **SHA-256 sin sal:** Vulnerable a tablas precalculadas; se recomienda reemplazar por `BCrypt` o `Argon2`.
- **Sin tiempo de inactividad:** La sesión permanece abierta hasta cerrar manualmente; se sugiere un timeout automático.
- **Sin auditoría:** No hay tabla de "quién hizo qué, cuándo, con qué IP". Idealmente se añadiría una tabla `audit_log` para registrar cada acción importante (registro, despacho, finalización, cambio de usuario).
- **Sin protección de sesión:** No se maneja token de sesión ni recarga; el login no soporta cierre forzoso de la sesión de otro dispositivo.

---

## Referencias y recursos adicionales

### Documentación de la aplicación

- **[DOCUMENTACION.md](atencion_urgencias/documentacion/DOCUMENTACION.md)** — descripción general, módulos, próximos hitos.
- **[GUIA_PRUEBAS.md](atencion_urgencias/documentacion/GUIA_PRUEBAS.md)** — credenciales de prueba, casos de uso, verificación en BD.
- **[REPORTE_ERRORES.md](atencion_urgencias/documentacion/REPORTE_ERRORES.md)** — bugs conocidos y recomendaciones de calidad.
- **[GUIÓN_PRESENTACIÓN.md](GUIÓN_PRESENTACIÓN.md)** — guion para la evaluación expositiva.
- **[init_db.sql](init_db.sql)** — esquema de BD y datos semilla.
- **[docker-compose.yml](atencion_urgencias/docker-compose.yml)** — entorno de base de datos reproducible.

### Herramientas y fuentes consultadas

- [Java 21 oficial](https://openjdk.org/projects/jdk/21/)
- [JavaFX 21](https://openjfx.io/)
- [Apache Maven](https://maven.apache.org/)
- [MariaDB JDBC driver](https://mariadb.com/kb/en/java-drveloper-guide/)
- [Docker Compose](https://docs.docker.com/compose/)
- [Lombok](https://projectlombok.org/)

### Sistema de diagramas implementados

- **C4Model** y **C4+1**: se documentan el nivel de Contexto, Contenedores, Componentes y Secuencia, representados mediante diagramas de texto (arriba) y descripciones de componentes.

---

*Documento generado a partir del código fuente y la documentación del proyecto. Última revisión: 13 de octubre de 2026.*
