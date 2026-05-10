# MEMORIA PERSISTENTE - PROYECTO: SISTEMA DE GESTIÓN DE URGENCIAS

## Información General
- **Nombre del Proyecto:** Sistema de Gestión de Urgencias
- **Descripción:** Sistema para optimizar el flujo de atención en una sala de urgencias hospitalaria, permitiendo el registro, clasificación (Triage), despacho y atención de pacientes.
- **Fecha de Análisis:** 30 de Abril de 2026
- **Fecha de Última Actualización:** 6 de Mayo de 2026
- **Analista:** Opencode AI Assistant
- **Estado Actual:** ✅ CORRECCIONES CRÍTICAS COMPLETADAS (Fases 1-5). ✅ Navegación UserForm corregida. ✅ Tabla FINALIZADAS en recepcionista. ✅ Campo ubicación agregado. ✅ Funcionalidad diferenciada ENFERMERO/AUXILIAR.

## Especificaciones Técnicas
- **Lenguaje:** Java 21
- **Interfaz Gráfica:** JavaFX 21 (Arquitectura MVC)
- **Gestión de Proyectos:** Maven
- **Persistencia:** MariaDB (Base de datos relacional) - JDBC via `mariadb-java-client:3.3.3`
- **Librerías:**
  - Lombok para reducción de código repetitivo
  - MariaDB Java Client para conectividad JDBC
- **Seguridad:**
  - Hashing de contraseñas SHA-256
  - PreparedStatement para prevenir SQL Injection

## Estructura del Proyecto (Actualizada - Post-Migración)
```
atencion_urgencias/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/ingenieria/software1/
│   │   │       ├── App.java
│   │   │       ├── controller/
│   │   │       │   ├── LoginController.java
│   │   │       │   ├── MedicoController.java
│   │   │       │   ├── RecepcionistaController.java
│   │   │       │   ├── RegistroUrgenciaController.java
│   │   │       │   ├── UserManagementController.java
│   │   │       │   ├── UserFormController.java
│   │   │       │   └── PersonalMedicoController.java
│   │   │       ├── model/
│   │   │       │   ├── Empleado.java
│   │   │       │   ├── Paciente.java
│   │   │       │   ├── RolEmpleado.java
│   │   │       │   ├── Triage.java
│   │   │       │   └── Urgencia.java
│   │   │       └── service/
│   │   │           ├── DatabaseService.java       (Singleton JDBC)
│   │   │           ├── EmpleadoDAO.java           (CRUD empleados)
│   │   │           ├── PacienteDAO.java           (CRUD pacientes)
│   │   │           ├── UrgenciaDAO.java           (CRUD urgencias)
│   │   │           ├── UsuarioService.java        (Auth + session, usa EmpleadoDAO)
│   │   │           ├── UserManagementService.java (CRUD con validaciones)
│   │   │           └── EmergenciaService.java     (Urgencias, usa UrgenciaDAO + PacienteDAO)
│   │   └── resources/
│   │       ├── config/
│   │       │   ├── db.properties                  (Credenciales BD)
│   │       │   └── seed_data.sql                  (Datos iniciales)
│   │       └── com/ingenieria/software1/view/
│   │       ├── login.fxml
│   │       ├── recepcionista.fxml
│   │       ├── medico.fxml
│   │       ├── registro_urgencia.fxml
│   │       ├── personal_medico.fxml
│   │       ├── user_form.fxml
│   │       └── styles.css
│   └── test/
│       └── java/
│           └── com/ingenieria/software1/
│               └── AppTest.java
├── pom.xml
├── DOCUMENTACION.md
├── avances_proyecto.txt
├── MEMORIA_PERSISTENTE.md
└── GUIA_PRUEBAS.md
```

## Archivos Eliminados (Post-Migración)
- ❌ `service/CSVService.java`
- ❌ `data/empleados.csv`, `data/pacientes.csv`, `data/urgencias.csv`
- ❌ `AGENTS.md`

## Configuración de Base de Datos
- **Host:** `localhost:3306`
- **Database:** `Emergencia_Medica`
- **User:** `usuario_sistema`
- **Password:** `1234`
- **Config:** `src/main/resources/config/db.properties`
- **Seed data:** `src/main/resources/config/seed_data.sql`

## Esquema de Base de Datos
```sql
CREATE TABLE empleados (
    id VARCHAR(20) PRIMARY KEY,
    nombre_completo VARCHAR(100) NOT NULL,
    usuario VARCHAR(50) UNIQUE NOT NULL,
    contrasena_hash VARCHAR(64) NOT NULL,
    rol ENUM('RECEPCIONISTA', 'MEDICO', 'ENFERMERO', 'AUXILIAR', 'ADMIN') NOT NULL
);

CREATE TABLE pacientes (
    id VARCHAR(20) PRIMARY KEY,
    nombre_completo VARCHAR(100) NOT NULL,
    edad INT, genero VARCHAR(10), direccion VARCHAR(200),
    telefono VARCHAR(20), historia_clinica TEXT
);

CREATE TABLE urgencias (
    id VARCHAR(20) PRIMARY KEY,
    paciente_id VARCHAR(20), empleado_id VARCHAR(20),
    nivel_triage INT, sintomas TEXT, signos_vitales TEXT,
    estado ENUM('PENDIENTE', 'EN_CURSO', 'FINALIZADO'),
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (paciente_id) REFERENCES pacientes(id),
    FOREIGN KEY (empleado_id) REFERENCES empleados(id)
);
```

## Arquitectura de Persistencia
### Capa DAO
| Clase | Operaciones |
|-------|-------------|
| `DatabaseService` | Singleton JDBC connection |
| `EmpleadoDAO` | autenticar, crear, actualizar, eliminar, obtenerTodos, obtenerPorRol, buscarPorId, buscarPorUsuario |
| `PacienteDAO` | crear, buscarPorId, obtenerTodos, actualizar, eliminar |
| `UrgenciaDAO` | crear, buscarPorId, obtenerTodas, obtenerPorEstado, actualizarEstado, actualizar, eliminar |

### Capa de Servicios
| Clase | Usa |
|-------|-----|
| `UsuarioService` | `EmpleadoDAO` |
| `UserManagementService` | `UsuarioService` |
| `EmergenciaService` | `UrgenciaDAO`, `PacienteDAO` |

## Credenciales de Prueba (todas con contraseña: `1234`)
| Username | Rol | ID |
|----------|-----|-----|
| `admin` | ADMIN | 1001 |
| `recepcion` | RECEPCIONISTA | 1002 |
| `medico1` | MEDICO | 1003 |
| `enfermera1` | ENFERMERO | 1004 |
| `auxiliar1` | AUXILIAR | 1005 |

## Estado Actual de Implementación
### Completado:
- ✅ Autenticación con SHA-256 + MariaDB (funcional)
- ✅ Login con redirección por rol (funcional)
- ✅ Migración completa CSV → MariaDB
- ✅ DAOs con CRUD completo
- ✅ EmergenciaService actualizado a MariaDB
- ✅ Seed data (5 empleados + 5 pacientes)
- ✅ Interfaz funcional - sin errores de carga FXML
- ✅ `user_form.fxml` corregido (eliminados fx:constant, agregado initialize en controller)

### Pendiente de Mejora y Corrección:
- ⏳ Recepcionista: verificar que registro de urgencias guarde correctamente en BD
- ⏳ Recepcionista: verificar despacho de urgencias (asignar personal)
- ⏳ Recepcionista: verificar filtros de estado
- ⏳ Médico: verificar funcionalidad de atención de urgencias
- ⏳ Personal Médico (Enfermero/Auxiliar): verificar funcionalidad
- ⏳ Admin: verificar CRUD de gestión de usuarios
- ⏳ Mejorar diseño visual de FXMLs
- ⏳ Tests unitarios para capa DAO
- ⏳ Connection pooling para producción

## Run/Build Commands
```bash
mvn compile          # Compilar
mvn javafx:run       # Ejecutar
```

## Notas Importantes
- **Interfaz funciona** pero las funcionalidades internas necesitan mejora y corrección
- `urgencias.id` es VARCHAR(20) (no INT AUTO_INCREMENT)
- Modelo `Triage` usa `nivelPrioridad` (no `nivel`)
- Flujo estados urgencia: `PENDIENTE` → `EN_CURSO` → `FINALIZADO`
- Todos los FXMLs usan `xmlns="http://javafx.com/javafx/21"`
- Ver `GUIA_PRUEBAS.md` para guía detallada de pruebas

## Correcciones Realizadas (3 de Mayo de 2026)
### Paso 1: UrgenciaDAO - Carga de personal asignado
- Corregido `mapResultSetToUrgencia()`: ahora crea objeto `Empleado` con `id` y `nombreCompleto` desde `empleado_id` y `empleado_nombre`
- Antes no se cargaba el personal asignado, causando que las urgencias no aparecieran en la pantalla del médico

### Paso 2: MedicoController y medica.fxml
- Eliminado texto fijo "Dr. Juan Perez" del Label en FXML
- Asegurado que `lblBienvenida` se actualice dinámicamente con el nombre del médico logueado
- CSS `label-subtitle` aplicado correctamente

### Paso 3: EmergenciaService - Corrección de errores
- Corregido error tipográfico: `Urgencia nva` → `Urgencia nueva` (consistencia de variables)
- Eliminado bloque de código duplicado en `registrarUrgenciaReal()`

### Paso 4: PersonalMedicoController - Implementación completa
- Reescrito `PersonalMedicoController.java` para ser similar a `MedicoController`
- Agregada tabla `tblUrgencias` con columnas: ID, Descripción, Ubicación, Gravedad, Hora
- Implementado método `refrescarTabla()` para filtrar urgencias asignadas al personal logueado
- Agregado botón "Finalizar Atención" con método `onFinalizarClick()`
- CSS aplicado: `label-title`, `label-subtitle`, `label-status`, `panel-header`, `toolbar`, `btn-success`, `btn-danger`

### Paso 5: Admin Dashboard - Nueva funcionalidad
- Creado `admin_dashboard.fxml` con panel principal para administrador
- Creado `AdminDashboardController.java` con:
  - Label de bienvenida dinámico
  - Botón "Gestionar Usuarios" → redirige a `user_form.fxml`
  - Botón "Cerrar Sesión" → vuelve a login
- Actualizado `LoginController`: rol ADMIN ahora redirige a `admin_dashboard` en lugar de `user_form`

### Resultados esperados tras correcciones
- ✅ Médicos ven sus urgencias asignadas (personalAsignado se carga correctamente)
- ✅ Enfermeros y Auxiliares tienen pantalla funcional con tabla de urgencias
- ✅ Admin tiene panel principal con navegación correcta
- ✅ Labels de bienvenida muestran nombre real del usuario logueado
- ✅ Asignación de urgencias (despacho) funciona correctamente vía `UrgenciaDAO.actualizarEstado()`

## Mejoras Implementadas (4 de Mayo de 2026)
### Paso 1: Admin Dashboard - Navegación completa
- `admin_dashboard.xml` ya tiene botón "Gestionar Usuarios" → `user_form.xml`
- Agregado botón "Volver" en `user_form.xml` con `@FXML onVolverClick()` en `UserFormController`
- Al hacer clic en "Volver", cierra la ventana y regresa al dashboard

### Paso 2: Médico - Selector de médicos y "Tomar" urgencia
- `medico.fxml` actualizado: agregado ComboBox `cbMedicos` y botón "Tomar Urgencia"
- `MedicoController.java` reescrito:
  - `cargarMedicosEnComboBox()`: carga todos los médicos registrados
  - `cbMedicos`: permite alternar entre médicos para ver sus urgencias
  - `onTomarClick()`: asigna la urgencia al médico logueado (cambia `personalAsignado` a sí mismo)
  - `refrescarTabla()`: filtra por médico seleccionado o muestra todas en curso

### Paso 3: Personal Médico (Enfermero/Auxiliar) - Tabla funcional
- `PersonalMedicoController.java` reescrito para ser similar a `MedicoController`
- `personal_medico.fxml` actualizado con:
  - Tabla `tblUrgencias` con columnas: ID, Descripción, Ubicación, Gravedad, Hora
  - Botón "Finalizar Atención" para cambiar estado a `FINALIZADO`
  - Label de bienvenida dinámico

### Paso 4: User Form - Botón "Volver"
- `user_form.xml` agregado botón "Volver" en el HBox de botones
- `UserFormController.java` agregado:
  - `@FXML private Button btnVolver;`
  - `@FXML public void onVolverClick()` que llama a `cerrarVentana()` para regresar a admin_dashboard

### Paso 5: MedicoController - Corrección de carga
- Verificado que `UrgenciaDAO.mapResultSetToUrgencia()` carga `personalAsignado` correctamente
- Al loguearse como médico, el ComboBox muestra todos los médicos registrados
- Se puede seleccionar un médico y ver sus urgencias asignadas en estado `EN_CURSO`
- Botón "Tomar Urgencia" asigna la urgencia seleccionada al médico logueado

## Guía de Pruebas Paso a Paso (Actualizada 4 de Mayo)

### 1. Iniciar el proyecto
```bash
cd "/home/torresjdc/Escritorio/Proyectos2026-1/Ingenieria de Software/atencion_urgencias"
mvn compile && mvn javafx:run
```

### 2. Prueba de Login y Redirección
| Paso | Acción | Resultado Esperado |
|------|--------|-------------------|
| 1 | `admin` / `1234` | Redirige a **admin_dashboard** |
| 2 | Cerrar sesión → Login: `medico1` / `1234` | Redirige a **medico** (Panel de Atención Médica) |
| 3 | Cerrar sesión → Login: `enfermera1` / `1234` | Redirige a **personal_medico** |
| 4 | Cerrar sesión → Login: `recepcion` / `1234` | Redirige a **recepcionista** |

### 3. Prueba: Admin Dashboard
| Paso | Acción | Resultado Esperado |
|------|--------|-------------------|
| 1 | Login: `admin` / `1234` | Label: "Bienvenido, Administrador Principal (ADMIN)" |
| 2 | Click "Gestionar Usuarios" | Abre **user_form** en modo creación |
| 3 | En user_form, click "Volver" | Cierra ventana, regresa a admin_dashboard |
| 4 | Click "Cerrar Sesión" | Regresa a login |

### 4. Prueba: Crear Nuevo Médico (desde Admin)
| Paso | Acción | Datos |
|------|--------|-------|
| 1 | Desde admin_dashboard → "Gestionar Usuarios" | - |
| 2 | Llenar: Documento: `DOC002`, Usuario: `nuevomedico`, Nombre: `Dr. Nuevo Test`, Contraseña: `123456`, Rol: `MEDICO` | - |
| 3 | Click "Guardar" | Mensaje verde: "Usuario creado exitosamente", ventana se cierra |
| 4 | Verificar en BD: | `SELECT * FROM empleados WHERE usuario='nuevomedico';` debe aparecer |

### 5. Prueba: Médico - Ver todos los médicos y tomar urgencia
| Paso | Acción | Resultado Esperado |
|------|--------|-------------------|
| 1 | Login: `medico1` / `1234` | Abre panel médico, Label muestra "Bienvenido, Dr. Juan Pérez - Médico (MEDICO)" |
| 2 | Ver ComboBox "Filtrar por Médico:" | Debe mostrar lista: "Dr. Juan Pérez (1003)", "Dr. Nuevo Test (DOC002)", etc. |
| 3 | Seleccionar "Dr. Juan Pérez" | Tabla muestra urgencias asignadas a él en `EN_CURSO` |
| 4 | Seleccionar urgencia en tabla | Fila resaltada |
| 5 | Click "Tomar Urgencia" | Urgencia se asigna a médico1, estado sigue `EN_CURSO`, tabla se actualiza |
| 6 | Click "Finalizar Atención" | Estado cambia a `FINALIZADO`, urgencia desaparece de la tabla `EN_CURSO` |

### 6. Prueba: Enfermero/Auxiliar - Tabla funcional
| Paso | Acción | Resultado Esperado |
|------|--------|-------------------|
| 1 | Login: `enfermera1` / `1234` | Label: "Bienvenido, Ana López - Enfermera (ENFERMERO)" |
| 2 | Ver tabla | Debe mostrar urgencias asignadas a ella en `EN_CURSO` |
| 3 | Seleccionar urgencia → "Finalizar Atención" | Estado cambia a `FINALIZADO` |

### 7. Prueba: Recepcionista - Registro y Despacho
| Paso | Acción | Datos |
|------|--------|-------|
| 1 | Login: `recepcion` / `1234` | Abre panel recepcionista |
| 2 | Click "Registrar Nueva Urgencia" | Abre ventana modal |
| 3 | Llenar: Doc: `PACNEW01`, Nombre: `Paciente Prueba`, Edad: `25`, Síntomas: `Dolor de cabeza`, Ubicación: `Sala de espera`, Triage: `3`, Signos: `PA: 120/80` | - |
| 4 | Click "Registrar" | Ventana se cierra, tabla se actualiza, urgencia en `PENDIENTE` |
| 5 | Seleccionar urgencia → ComboBox "Asignar a:" seleccionar `Dr. Juan Pérez` | - |
| 6 | Click "Despachar Urgencia" | Estado cambia a `EN_CURSO`, columna "Personal Asignado" muestra "Dr. Juan Pérez - Médico (MEDICO)" |

### 8. Verificación en Base de Datos
```bash
# Ver urgencias recién creadas
mariadb -u usuario_sistema -p1234 Emergencia_Medica -e "SELECT u.id, u.estado, p.nombre_completo as paciente, e.nombre_completo as asignado FROM urgencias u LEFT JOIN pacientes p ON u.paciente_id = p.id LEFT JOIN empleados e ON u.empleado_id = e.id ORDER BY u.fecha_registro DESC LIMIT 5;"

# Ver médicos registrados
mariadb -u usuario_sistema -p1234 Emergencia_Medica -e "SELECT id, nombre_completo, usuario, rol FROM empleados WHERE rol='MEDICO';"
```

### 9. Prueba de CSS
| Paso | Acción | Resultado Esperado |
|------|--------|-------------------|
| 1 | Login | Fondo azul claro (#ECEFF1), tarjeta blanca centrada con sombra |
| 2 | Panel admin/médico/recepcionista | Header azul (#1565C0) con texto blanco, botones coloreados |
| 3 | Tablas | Encabezados azules, filas alternadas blanco/gris, hover azul claro |
| 4 | Campos de texto | Borde gris, focus azul, radio buttons estilizados |

## Notas Importantes
- **Interfaz funciona** con CSS aplicado uniformemente
- **Navegación completa**: Admin → user_form → Volver → login (no cierra app)
- **Selector de médicos**: En panel médico se puede alternar entre todos los médicos registrados
- **Botón "Tomar"**: Asigna urgencia al médico logueado
- `urgencias.id` es VARCHAR(20) (no INT AUTO_INCREMENT)
- Modelo `Triage` usa `nivelPrioridad` (no `nivel`)
- Flujo estados urgencia: `PENDIENTE` → `EN_CURSO` → `FINALIZADO`
- Todos los FXMLs usan `xmlns="http://javafx.com/javafx/21"`

## Correcciones Críticas Implementadas (6 de Mayo de 2026)

### FASE 1: Error #1 - Navegación UserForm
**Problema:** El programa se cerraba al presionar "Volver", "Guardar" o "Cancelar" en user_form.fxml

**Causa:** El método `cerrarVentana()` usaba `stage.close()` que cerraba toda la aplicación

**Solución implementada en `UserFormController.java`:**
- `onVolverClick()` → Ahora usa `App.setRoot("view/login")`
- `onGuardarClick()` → Al guardar exitosamente, navega a `login.fxml`
- `onCancelarClick()` → Navega a `login.fxml`
- Eliminado el método `cerrarVentana()` que causaba el cierre

**Resultado:** ✅ Ya no se cierra la aplicación, ahora navega correctamente al login

---

### FASE 2: Errores #2 y #3 - Recepcionista

**Error #2 - Tabla para urgencias FINALIZADAS:**
- Modificado `recepcionista.fxml`:
  - Agregada segunda TableView `tblFinalizadas` para mostrar urgencias atendidas
  - Agregado botón "Ver Finalizadas" para alternar entre tablas
  - Agregado Label `lblTituloTabla` para mostrar qué tabla está activa
  - Agregadas columnas para la tabla de finalizadas

- Modificado `RecepcionistaController.java`:
  - Nuevo método `refrescarTablaFinalizadas()` - carga urgencias con estado FINALIZADO
  - Nuevo método `onVerFinalizadasClick()` - alterna visibilidad de tablas
  - Actualizado ComboBox filtro: cambiado "ATENDIDA" por "FINALIZADO"
  - Variable `mostrandoFinalizadas` para controlar qué tabla se muestra

**Error #3 - Alerta nivel 5 aparece aunque ya atendida:**
- Modificado `onNuevaUrgenciaClick()`:
- **Antes:** La alerta se mostraba sin verificar el estado
- **Ahora:** Verifica que `urgencia.getEstado().equals("PENDIENTE")` antes de mostrar alerta
- La alerta solo aparece para urgencias que realmente necesitan atención inmediata

**Resultado:** ✅ Tabla separadapara FINALIZADOS + alerta corregida

---

### FASE 3: Error #4 - Campo Ubicación no aparecía

**Problema:** La ubicación del paciente no se mostraba en las tablas de recepcionista

**Causa:** 
1. La columna `ubicacion` no existía en la tabla `urgencias` de la base de datos
2. El DAO no incluía este campo en las consultas SQL

**Solución implementada:**
1. **Base de datos:** Agregada columna `ubicacion VARCHAR(200) DEFAULT NULL`
   ```sql
   ALTER TABLE urgencias ADD COLUMN ubicacion VARCHAR(200) DEFAULT NULL AFTER sintomas;
   ```

2. **UrgenciaDAO.java actualizado:**
   - `crear()`: INSERT agora incluye `ubicacion`
   - `actualizar()`: UPDATE agora incluye `ubicacion`
   - `mapResultSetToUrgencia()`: Carga `ubicacion` del ResultSet

3. **Verificado:** `RegistroUrgenciaController.java` ya guardaba la ubicación correctamente ✅
4. **Verificado:** `EmergenciaService.registrarUrgenciaReal()` ya usaba `nueva.setUbicacion(ubicacion)` ✅

**Resultado:** ✅ La ubicación ahora se guarda y muestra correctamente en todas las tablas

---

### FASE 4 y 5: Error #5 - ENFERMERO y AUXILIAR sin funcionalidad real

**Problema:** Ambos roles no tenían tareas diferenciadas ni forma de cambiar entre personal registrado

**Solución implementada en `personal_medico.fxml` y `PersonalMedicoController.java`:**

1. **Selector de personal (ComboBox):**
   - Agregado ComboBox `cbPersonal` para seleccionar entre enfermeros/auxiliares registrados
   - Agregado Label `lblTareas` para mostrar las tareas del rol actual
   - Agregada columna "Estado" en la tabla de urgencias

2. **Carga de personal diferenciada:**
   - Nuevo método `cargarPersonalEnComboBox()`:
     - Si el usuario logueado es ENFERMERO → carga solo enfermeros
     - Si el usuario logueado es AUXILIAR → carga solo auxiliares
     - Selecciona por defecto el usuario logueado

3. **Tareas diferenciadas por rol:**
   - Nuevo método `configurarTareasSegunRol()`:
   
   | Rol | Tareas Asignadas |
   |-----|-----------------|
   | **ENFERMERO** | Revisar signos vitales • Administrar medicamentos • Actualizar triage • Apoyar en procedimientos |
   | **AUXILIAR** | Trasladar pacientes • Traer equipos médicos • Apoyar en logística • Mantener área limpia |

4. **Tabla filtrada por selection:**
   - Actualizado `refrescarTabla()` para filtrar por el personal seleccionado en el ComboBox

**Resultado:** ✅ Roles tienen funcionalidad y tareas diferenciadas

---

## Guía de Ejecución y Pruebas (6 de Mayo 2026)

### Comando para Ejecutar
```bash
cd "/home/torresjdc/Escritorio/Proyectos2026-1/Ingenieria de Software/atencion_urgencias"
mvn compile && mvn javafx:run
```

### Pruebas de Verificación Post-Correcciones

| # | Prueba | Credenciales | Resultado Esperado |
|---|-------|--------------|-------------------|
| 1 | Login: admin / 1234 → "Volver" | - | Navega a login, NO cierra la app |
| 2 | Login: recepcion / 1234 → Registrar urgencia con ubicación | Llenar todos los campos | Ubicación aparece en la tabla |
| 3 | Login: recepcion / 1234 → Click "Ver Finalizadas" | Haber urgencias finalizadas | Muestra segunda tabla |
| 4 | Login: recepcion / 1234 → Registrar nivel 5 | Triage: 5 | Alerta SOLO si está PENDIENTE |
| 5 | Login: enfermera1 / 1234 | - | Muestra tareas de ENFERMERO |
| 6 | Login: auxiliar1 / 1234 | - | Muestra tareas de AUXILIAR |
