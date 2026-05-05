# MEMORIA PERSISTENTE - PROYECTO: SISTEMA DE GESTIÓN DE URGENCIAS

## Información General
- **Nombre del Proyecto:** Sistema de Gestión de Urgencias
- **Descripción:** Sistema para optimizar el flujo de atención en una sala de urgencias hospitalaria, permitiendo el registro, clasificación (Triage), despacho y atención de pacientes.
- **Fecha de Análisis:** 30 de Abril de 2026
- **Fecha de Última Actualización:** 2 de Mayo de 2026
- **Analista:** Opencode AI Assistant
- **Estado Actual:** ✅ MIGRACIÓN COMPLETADA: CSV reemplazado por MariaDB. Interfaz funcional (sin errores FXML). Funcionalidades pendientes de mejora y corrección.

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
│   │           ├── login.fxml
│   │           ├── recepcionista.fxml
│   │           ├── medico.fxml
│   │           ├── registro_urgencia.fxml
│   │           ├── personal_medico.fxml
│   │           └── user_form.fxml
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
