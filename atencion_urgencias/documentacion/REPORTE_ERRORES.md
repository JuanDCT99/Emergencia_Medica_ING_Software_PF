# 📋 REPORTE DE ERRORES CONOCIDOS
> Sistema de Gestión de Urgencias

Este documento detalla los problemas, bugs y áreas de mejora identificados en el código y en la infraestructura del proyecto.

---

## 🔴 CRÍTICOS (Bloquean la ejecución)

### ERROR 1: Base de datos no inicializada por defecto (VERIFICADO Y FUNCIONANDO)

**Estado:** ✅ **Completamente Resuelto y Verificado**

**Descripción:** El `seed_data.sql` original no creaba las tablas `empleados`, `pacientes` y `urgencias`. Además, las credenciales de usuario (`usuario_sistema` / `1234`) no existían en una instalación local estándar de MariaDB, causando errores de "Access denied".

**Solución Implementada y Verificada:**
*   Se creó un nuevo archivo `init_db.sql` en la raíz del proyecto (y mirror en `docker-entrypoint-initdb.d/001-init-db.sql`) con el esquema completo (`CREATE TABLE` para empleados, pacientes, ambulancias, urgencias) y todos los datos semilla. El script también crea el usuario `usuario_sistema` y otorga privilegios.
*   Se creó un archivo `docker-compose.yml` que levanta un contenedor de MariaDB 11.8.8 listo para usar. Automáticamente crea el usuario, la base de datos y carga el script `init_db.sql` al iniciar.
*   Se actualizó `db.properties` para apuntar al puerto `3307` (Docker por defecto).
*   Se verificó la conexión JDBC desde Java: `✅ Conexión exitosa a MariaDB: jdbc:mariadb://localhost:3307/Emergencia_Medica`.
*   Se verificó el contenido de la base de datos: 4 tablas (empleados, pacientes, urgencias, ambulancias), 5 empleados, 5 pacientes, 4 ambulancias.
*   **Instrucciones para el equipo:** `docker compose up -d` desde la raíz del proyecto (`atencion_urgencias/`). Esperar ~30 segundos. Luego `mvn javafx:run`.

---

## 🟠 ALTOS (Afectan funcionalidad o UX significativamente)

### ERROR 2: Documentación (DOCUMENTACION.md) desactualizada y mezclada

**Estado:** 🟡 **Resuelto** — Se ha creado una nueva versión en `documentacion/DOCUMENTACION.md`.

**Descripción:** La `DOCUMENTACION.md` original describía el sistema como si usara persistencia en CSV, mientras que el código Java real utiliza MariaDB. También contenía referencias confusas a otro proyecto (`run_console.py` del ChatBot).

**Solución Implementada:**
*   La nueva `documentacion/DOCUMENTACION.md` refleja con precisión la arquitectura actual con MariaDB, los DAOs, los servicios y la estructura MVC + capa de servicios.

---

### ERROR 3: Navegación post-guardado de usuario (envía al login)

**Estado:** 🟡 **Abierto**

**Descripción:** En `UserFormController.onGuardarClick()`, después de crear o actualizar un usuario exitosamente, la aplicación navega a `view/login` (`App.setRoot("view/login")`). Esto desconecta visualmente al administrador, quien luego debe iniciar sesión nuevamente, lo cual es confuso.

**Impacto:** Mala experiencia de usuario. El administrador no entiende por qué vuelve a la pantalla de login.

**Sugerencia de Solución:**
*   Navegar a `view/admin_dashboard` en lugar de `view/login`.
*   O, dejar al admin en el formulario con un mensaje de éxito y permitirle continuar trabajando.
```java
// En UserFormController.java, dentro del bloque 'if (resultado.isExitoso())':
// Cambiar:
//   App.setRoot("view/login");
// Por:
//   App.setRoot("view/admin_dashboard");
```

---

### ERROR 4: ComboBox de estados de ambulancia en diálogo omite "EN_USO"

**Estado:** ✅ **Aceptado / Comportamiento Intencional**

**Descripción:** El `AmbulanciaDialogController` solo muestra los estados `DISPONIBLE` y `FUERA_SERVICIO` en su ComboBox, omitiendo `EN_USO`. Esto es técnicamente correcto (una ambulancia en uso no debería ser editada manualmente), pero no está documentado en el código.

**Impacto:** Potencial confusión si un desarrollador o usuario espera ver el estado "EN_USO".

**Sugerencia de Solución:**
*   Añadir un comentario claro en `initialize()` del controlador explicando por qué `EN_USO` no está incluido.

---

## 🟡 MEDIOS (Problemas o riesgos menores)

### ERROR 5: Orden de operaciones de limpieza en seed_data.sql es confuso

**Estado:** 🟡 **Resuelto** — El nuevo `init_db.sql` corrige el orden y añade comentarios.

**Descripción:** El script original hacía `DELETE FROM urgencias; DELETE FROM ambulancias; ...` en un orden que, aunque no provocaba errores por FK en ese momento, era confuso. El nuevo script `init_db.sql` utiliza `DROP TABLE IF EXISTS` de forma cascada o comenta claramente el orden de limpieza.

---

### ERROR 6: `App.java` usa ruta absoluta para cargar `styles.css`

**Estado:** ✅ **Aceptable**

**Descripción:** En `App.java`, el CSS se carga con `App.class.getResource("/com/ingenieria/software1/view/styles.css")` (ruta absoluta desde classpath). Funciona, pero es inconsistente con otras cargas de recursos que usan rutas relativas como `"view/login"`.

**Impacto:** Ninguno funcional.

---

### ERROR 7: Posible `NullPointerException` si `txtContrasena` es nulo en `UserFormController`

**Estado:** 🟡 **Abierto**

**Descripción:** En `UserFormController.validarFormulario()`, si `txtContrasena.getText()` devuelve `null` (poco común, pero posible si el FXML no inyecta correctamente), `contrasena.isEmpty()` lanzará un NPE. Debería usar `contrasena == null || contrasena.isEmpty()`.

**Impacto:** Bajo. Solo ocurre si hay un fallo en la inyección FXML.

---

## 🟢 BAJOS (Tareas de limpieza / testing)

### ERROR 8: Archivos FXML de prueba son código muerto

**Estado:** ✅ **Aceptado**

**Descripción:** `super_simple.fxml` y `ultra_simple.fxml` no son referenciados por ningún controlador ni archivo. `AdminTestController.java` tampoco está enlazado a un FXML.

**Sugerencia de Solución:** Eliminar estos archivos de prueba o documentarlos claramente como tales.

---

### ERROR 9: Tests unitarios son un esqueleto

**Estado:** 🟡 **Abierto**

**Descripción:** `AppTest.java` solo verifica `assertTrue(true)`. No hay cobertura real de la lógica del negocio (autenticación, despacho de urgencias, etc.).

**Sugerencia de Solución:** Escribir tests para los servicios clave (`UsuarioService`, `EmergenciaService`) usando mocks para `DatabaseService` o ejecutando contra la base de datos de Docker.

---

### ERROR 10: El `pom.xml` incluye `javafx-graphics`

**Estado:** ✅ **Aceptable**

**Descripción:** La dependencia `javafx-graphics` en `pom.xml` no es estrictamente necesaria para la mayoría de las aplicaciones JavaFX (viene implícita vía `javafx-controls` y `javafx-fxml`). No causa problemas, pero es redundante.

**Sugerencia de Solución:** Considerar su eliminación para mantener el `pom.xml` limpio.

---

## 📌 RESUMEN EJECUTIVO

| Prioridad | Estado | # de Errores | Acción |
| :--- | :--- | :--- | :--- |
| 🔴 Críticos | ✅ Resuelto | 1 | Database setup automatizado y verificado con Docker. |
| 🟠 Altos | 🟡 Resuelto | 1 | Documentación actualizada. |
| 🟠 Altos | 🟡 Abierto | 1 | Corregir navegación post-guardado de usuario. |
| 🟠 Altos | ✅ Aceptado | 1 | Documentar ComboBox de ambulancias. |
| 🟡 Medios | 🟡 Resuelto | 1 | Orden de limpieza en SQL. |
| 🟡 Medios | ✅ Aceptable | 2 | Inconsistencia de rutas / Posible NPE. |
| 🟢 Bajos | ✅ Aceptado | 1 | Código muerto (FXML de prueba). |
| 🟢 Bajos | 🟡 Abierto | 1 | Tests unitarios son esqueleto. |
| 🟢 Bajos | ✅ Aceptable | 1 | Dependencia redundante (`javafx-graphics`). |