# Diagnóstico Técnico y Propuesta de Refactorización

**Asignatura:** Ingeniería de Software II  
**Proyecto:** Sistema de Gestión de Urgencias Hospitalarias (`Emergencia_Medica_ING_Software_PF`)  
**Fecha:** 26 de Octubre de 2026  
**Documento base:** `Taller_Refactorizacion_y_Cierre_Arquitectura_IS_II.pdf`

---

## 1. Contexto

* **Equipo de Desarrollo:** Equipo de Ingeniería de Software II (Rama: `dev_juan`).
* **Repositorio:** `https://github.com/juanda/Software/tree/dev_juan/Emergencia_Medica_ING_Software_PF`
* **Funcionalidad Seleccionada:** **Flujo Transaccional de Admisión, Registro con Triage y Despacho de Urgencias**.
* **Requisitos Relacionados:**
  * **RF-01 (Registro y Clasificación):** El recepcionista debe registrar los datos demográficos del paciente junto con su sintomatología, ubicación y signos vitales, determinando un nivel de prioridad de Triage (escala 1 a 5).
  * **RF-02 (Detección de Casos Críticos):** El sistema debe emitir una alerta visual inmediata ante casos de Triage Nivel 5 (Riesgo vital inminente).
  * **RF-03 (Despacho de Personal):** El recepcionista debe asignar un recurso médico calificado (Médico o Enfermero) a una urgencia en estado `PENDIENTE`, transicionando su estado a `EN_CURSO`.
  * **RNF-01 (Integridad y Consistencia de Datos):** La persistencia en MariaDB debe garantizar que la relación entre el paciente y su urgencia asociada sea atómica y coherente.

---

## 2. Arquitectura Real y Flujo de la Funcionalidad

El sistema actual implementa una arquitectura por capas basada en el patrón Modelo-Vista-Controlador (MVC) y Objetos de Acceso a Datos (DAO):

```
[ Vista (JavaFX FXML) ]
   ├── registro_urgencia.fxml ──> RegistroUrgenciaController
   └── recepcionista.fxml     ──> RecepcionistaController
             │
             ▼
[ Capa de Negocio / Servicios ]
   └── EmergenciaService (Singleton)
             │
             ▼
[ Capa de Persistencia / Acceso a Datos ]
   ├── PacienteDAO ──┐
   ├── UrgenciaDAO ──┼──> DatabaseService (Singleton JDBC - MariaDB)
   └── EmpleadoDAO ──┘
```

### Flujo de la Transacción Actual:
1. `RegistroUrgenciaController.onRegistrarClick()` extrae los datos de la interfaz gráfica y crea un objeto `Paciente`.
2. Se invoca `EmergenciaService.getInstance().registrarUrgenciaReal(...)`.
3. El servicio verifica con `pacienteDAO.buscarPorId(...)`; si no existe, invoca `pacienteDAO.crear(...)`.
4. El servicio instancia `Urgencia` (cuyo constructor asigna un ID aleatorio `URG-XXXX`), genera un `Triage` con UUID truncado y llama a `urgenciaDAO.crear(...)`.
5. Al cerrarse la ventana modal, `RecepcionistaController.onNuevaUrgenciaClick()` consulta todas las urgencias de la BD (`getTodasLasUrgencias()`), toma el último elemento de la lista para inspeccionar si fue nivel 5 y dispara la alerta visual.

---

## 3. Fichas de Hallazgos Técnicos

### Ficha H-01: Ausencia de Transaccionalidad Atómica y Manejo Silencioso de Fallos
* **ID:** `H-01`
* **Tipo:** Riesgo Arquitectónico / Defecto Potencial de Integridad
* **Ubicación:** 
  * Clase: `com.ingenieria.software1.service.EmergenciaService`
  * Método: `registrarUrgenciaReal(Paciente paciente, String sintomas, String ubicacion, int nivelTriage, String signosVitales)`
  * Líneas: 31–53
* **Evidencia:**
  ```java
  // EmergenciaService.java (Líneas 32-36 y 52)
  Paciente existente = pacienteDAO.buscarPorId(paciente.getId());
  if (existente == null) {
      paciente.setHistoriaClinica("");
      pacienteDAO.crear(paciente); // Se ignora si retorna false o si falla
  }
  ...
  urgenciaDAO.crear(nueva); // Se ejecuta en una conexión diferente e independiente
  ```
* **Observación:** La operación de admisión de urgencia involucra dos inserciones en base de datos (`pacientes` y `urgencias`), donde la segunda depende formalmente de la primera mediante una clave foránea (`FOREIGN KEY (paciente_id) REFERENCES pacientes(id)`). Actualmente, cada DAO abre y cierra su propia conexión (`dbService.getConnection()`) con `autoCommit = true`. Si la creación de la urgencia falla (por ejemplo por colisión de ID o desconexión), el paciente queda persistido sin urgencia, o si `pacienteDAO.crear()` falla, `urgenciaDAO.crear()` lanzará un error de clave foránea. No existe una frontera transaccional (`commit`/`rollback`).
* **Criterio Afectado:** Principio de Responsabilidad Única (SRP en capa de servicio), Consistencia de Transacciones (ACID), Robustez de Persistencia.
* **Impacto y Riesgo:** 
  * *Impacto:* Alto. Inconsistencia de datos en la base de datos de producción; fallos no propagados a la capa superior dejan al usuario sin retroalimentación de si la urgencia se guardó realmente.
  * *Riesgo:* Alto. Ante cualquier error de red o de sintaxis en `urgenciaDAO`, se producen registros huérfanos o fallos silenciosos.
* **Acción Propuesta:** Modificar el flujo para que el servicio coordine una transacción compartida o propague excepciones de negocio checked/runtime (`ServiceException`) validando el éxito de las operaciones en persistencia.
* **Verificación:** Prueba unitaria/integración simulando fallo en la inserción de `Urgencia` y verificando que no queden datos corruptos o huérfanos en la tabla `pacientes`.
* **Estado:** Pendiente / Seleccionado para intervención.

---

### Ficha H-02: Fuga de Reglas de Negocio en la UI y Consulta Inconsistente de Alerta Crítica
* **ID:** `H-02`
* **Tipo:** Deuda Técnica / Defecto Funcional
* **Ubicación:** 
  * Clase: `com.ingenieria.software1.controller.RecepcionistaController`
  * Métodos: `onNuevaUrgenciaClick()` (Líneas 204–212) y `onDespacharClick()` (Líneas 242–246)
* **Evidencia:**
  ```java
  // RecepcionistaController.java (Líneas 204-212)
  List<Urgencia> urgencias = emergenciaService.getTodasLasUrgencias();
  if (!urgencias.isEmpty()) {
      Urgencia ultima = urgencias.get(urgencias.size() - 1);
      if (ultima.getTriage() != null && 
          ultima.getTriage().getNivelPrioridad() == 5 && 
          "PENDIENTE".equals(ultima.getEstado())) {
          mostrarAlertaCritica(ultima);
      }
  }
  ```
  ```java
  // RecepcionistaController.java (Líneas 242-245)
  if (!seleccionada.getEstado().equals("PENDIENTE")) {
      lblEstadoSistema.setText("Esta urgencia ya está siendo atendida o finalizada.");
      return;
  }
  emergenciaService.despacharUrgencia(seleccionada.getId(), asignado);
  ```
* **Observación:** 
  1. En `UrgenciaDAO.obtenerTodas()` la consulta SQL tiene la cláusula `ORDER BY u.fecha_registro DESC`. Esto implica que el elemento `0` es el más nuevo, y el elemento `urgencias.size() - 1` es **el registro más antiguo de la base de datos**. El controlador asume que la última posición de la lista es la urgencia recién ingresada, lo que ocasiona que la alerta de Triage 5 no evalúe el paciente actual sino el histórico más viejo.
  2. Las validaciones de transición de estados (`PENDIENTE -> EN_CURSO`) y la determinación de si una urgencia recién registrada es crítica están escritas dentro del controlador de JavaFX en vez de ser provistas por el modelo de dominio o el servicio de negocio.
* **Criterio Afectado:** Separación de Capas (SoC), Acoplamiento, Principio de Inversión de Dependencias (DIP) y Cohesión de Controladores.
* **Impacto y Riesgo:**
  * *Impacto:* Alto. El recepcionista puede no recibir la alerta de Triage 5 de un paciente en paro o código azul porque la condición evalúa un elemento incorrecto.
  * *Riesgo:* Crítico en el contexto médico asistencial.
* **Acción Propuesta:** Hacer que `EmergenciaService.registrarUrgenciaReal(...)` retorne la instancia creada de `Urgencia`. De esta forma, el controlador de registro o recepción recibe directamente la entidad creada y determina la alerta sin releer toda la base de datos ni asumir posiciones erróneas en listas. Adicionalmente, trasladar la validación de estado al servicio `despacharUrgencia(...)`.
* **Verificación:** Registrar una urgencia con Triage 5 existiendo previamente 10 urgencias de Triage 1 a 4. Comprobar que la alerta se dispare inequívocamente para el paciente registrado.
* **Estado:** Pendiente / Seleccionado para intervención.

---

### Ficha H-03: Generación No Concurrente de Identificadores de Clave Primaria en el Constructor de Entidad
* **ID:** `H-03`
* **Tipo:** Riesgo Arquitectónico / Deuda Técnica
* **Ubicación:** 
  * Clase: `com.ingenieria.software1.model.Urgencia`
  * Constructor: Líneas 29–35
* **Evidencia:**
  ```java
  // Urgencia.java (Líneas 29-35)
  private static final Random random = new Random();

  public Urgencia() {
      this.id = "URG-" + (1000 + random.nextInt(9000));
      this.horaReporte = LocalDateTime.now();
      this.estado = "PENDIENTE";
  }
  ```
* **Observación:** El modelo POJO `Urgencia` acopla la generación de su clave primaria en su constructor por defecto mediante `random.nextInt(9000)`. Esto solo admite 9.000 identificadores posibles. De acuerdo con la paradoja del cumpleaños, con menos de 120 registros la probabilidad de colisión de clave primaria supera el 50%. En caso de colisión, el `INSERT INTO urgencias` fallará con un error de clave duplicada (`PRIMARY KEY constraint violation`), y como `UrgenciaDAO.crear()` captura la excepción retornando simplemente `false`, la creación fallará de forma totalmente silenciosa.
* **Criterio Afectado:** Principio de Responsabilidad Única (el POJO no debe autogenerar IDs con pseudo-azar), Robustez y Escalabilidad del Modelo de Dominio.
* **Impacto y Riesgo:**
  * *Impacto:* Medio/Alto. Fallos de inserción conforme crece el volumen de urgencias del hospital.
  * *Riesgo:* Medio. El sistema se degrada exponencialmente con el uso continuo.
* **Acción Propuesta:** Delegar la generación del identificador a un generador determinista o UUID (ej. `URG-` + UUID) o delegar el ID al autoincrementable de la base de datos, separando la lógica de generación del constructor del modelo.
* **Verificación:** Ejecutar un test de generación masiva (1.000 instancias) asegurando colisiones = 0 y persistencia exitosa.
* **Estado:** Pendiente.

---

## 4. Priorización de Hallazgos

| ID | Hallazgo | Impacto (1–3) | Riesgo (1–3) | Esfuerzo (1–3) | Prioridad Global | Razón de Selección |
|---|---|:---:|:---:|:---:|:---:|---|
| **H-02** | Fuga de lógica de negocio y cálculo invertido de alerta crítica en `RecepcionistaController` | 3 | 3 | 1 | **1 (Máxima)** | Defecto directo que afecta la seguridad del paciente (alerta de código crítico no mostrada adecuadamente) con un esfuerzo de corrección muy acotado. |
| **H-01** | Ausencia de transaccionalidad atómica y retorno de estado en `EmergenciaService` | 3 | 3 | 2 | **2 (Alta)** | Garantiza que la capa de servicio informe el resultado real a la UI y prevenga inconsistencias entre `Paciente` y `Urgencia`. |
| **H-03** | Generador de IDs en `Urgencia` vulnerable a colisiones | 2 | 2 | 1 | **3 (Media)** | Deuda técnica latente que provocará fallos de clave duplicada a mediano plazo. |

**Decisión de Intervención:**
Se selecciona **H-02** (en conjunto con el retorno tipado de **H-01** en `EmergenciaService`) para la refactorización principal, dado que corrige un defecto funcional crítico en la trazabilidad del Triage 5 y desacopla la capa de presentación de la lógica de negocio.

---

## 5. Propuesta de Intervención (Diseño Antes vs. Después)

### A. Diagnóstico del Diseño Actual (Antes)
* El método `EmergenciaService.registrarUrgenciaReal(...)` tiene firma `void`.
* El `RecepcionistaController` después de abrir el diálogo no sabe qué urgencia se creó:
  ```
  [RecepcionistaController]
          │
          ├─> Abre ventana modal (RegistroUrgenciaController)
          │        └─> Invoca EmergenciaService.registrarUrgenciaReal() [retorna void]
          │
          ├─> Re-consulta todas las urgencias: getTodasLasUrgencias()
          └─> Extrae urgencias.get(size() - 1) [¡ÍNDICE INVERTIDO: Obtiene la más antigua!]
  ```

### B. Diseño Propuesto (Después)
* Refactorizar `EmergenciaService.registrarUrgenciaReal(...)` para que retorne `Urgencia`:
  * Asigna explícitamente la entidad creada.
  * Valida que la persistencia en `PacienteDAO` y `UrgenciaDAO` haya sido exitosa; de lo contrario lanza una excepción controlada del dominio (`OperacionUrgenciaException`).
* `RegistroUrgenciaController` (o un listener/callback) puede comunicar la urgencia registrada, o el método `registrarUrgenciaReal` expone el evento de criticidad.
* En `RecepcionistaController`, la evaluación de alerta crítica se realiza sobre la urgencia recién creada retornada por el flujo, eliminando la recarga completa ciega y el acceso al índice erróneo `size() - 1`:

```
[RecepcionistaController / Registro]
          │
          ├─> Invoca EmergenciaService.registrarUrgencia(...)
          │        │
          │        ├─> Persiste Paciente & Urgencia con validación
          │        └─> Retorna la instancia 'Urgencia' persistida
          │
          ├─> Si urgencia.getTriage().getNivelPrioridad() == 5:
          │        └─> Dispara alerta crítica directamente sobre la urgencia retornada
          │
          └─> refrescarTabla()
```

### C. Archivos Afectados por la Refactorización
1. `com/ingenieria/software1/service/EmergenciaService.java`
   * Cambio de firma: `public Urgencia registrarUrgenciaReal(...) throws PersistenciaException`.
   * Validación del resultado de `pacienteDAO.crear(...)` y `urgenciaDAO.crear(...)`.
   * Método `despacharUrgencia(String idUrgencia, Empleado asignado)`: validación de negocio sobre el estado actual antes de delegar al DAO.
2. `com/ingenieria/software1/controller/RegistroUrgenciaController.java`
   * Manejo adecuado del objeto retornado y captura de errores de persistencia para mostrar diálogo informativo al usuario.
3. `com/ingenieria/software1/controller/RecepcionistaController.java`
   * Eliminación del bloque frágil de `urgencias.get(urgencias.size() - 1)`.
   * Reorganización de la alerta visual sin acoplamiento a la posición en la lista.

---

## 6. Plan de Verificación y Pruebas

Para garantizar la conservación del comportamiento observable (no romper funcionalidades existentes):

### 1. Pruebas Antes de la Refactorización
* **Prueba Base 1:** Registrar un paciente con Triage 3. Verificar que aparece en la tabla de urgencias activas con estado `PENDIENTE`.
* **Prueba Base 2:** Intentar despachar la urgencia asignando un médico. Verificar que el estado cambie a `EN_CURSO`.
* **Prueba Base 3 (Demostración del Defecto):** Tener registradas al menos 2 urgencias en la base de datos (ej. una antigua con Triage 1). Registrar una nueva urgencia con Triage 5. Constatar que el modal de alerta crítica toma los datos del registro incorrecto debido a `urgencias.get(urgencias.size() - 1)`.

### 2. Pruebas Después de la Refactorización
* **Prueba de Regresión 1:** Registrar paciente con Triage 3. El registro se guarda y se visualiza en la tabla exactamente como antes.
* **Prueba de Corrección 2:** Registrar una urgencia con Triage 5. El sistema detecta inmediatamente la urgencia creada y abre el diálogo `¡ALERTA CRÍTICA!` con el nombre, ubicación y síntomas correctos del nuevo paciente.
* **Prueba de Robustez 3:** Simular fallo de base de datos (por ejemplo, desconexión intencional). Comprobar que no se cierra la ventana modal y se informa al usuario mediante alerta de error en pantalla sin corromper el estado de la vista.

---

## 7. Cierre y Pregunta de Reflexión

### Lecciones Aprendidas
* La separación clara de responsabilidades entre Controladores y Servicios evita que los controladores asuman premisas falsas sobre el ordenamiento o almacenamiento interno de los datos (como suponer que la última fila de una consulta es el último registro ingresado).
* Las firmas `void` en métodos de servicio de creación limitan la capacidad de la interfaz de usuario para reaccionar de forma reactiva al objeto creado sin tener que reconsultar toda la base de datos.
* Los contratos y tipos de retorno deben reflejar la realidad del dominio: si una operación de guardado puede fallar, la capa de servicio debe informar dicho resultado para mantener la consistencia del sistema.

### Deuda Técnica Pendiente
* Implementación de un Connection Pool (como HikariCP) en `DatabaseService` en reemplazo de conexiones directas con `DriverManager`.
* Creación de pruebas unitarias automatizadas con mocks (Mockito) para `EmergenciaService` aislando los DAOs.
* Migración del identificador de `Urgencia` a secuencia relacional o UUID completo para erradicar el riesgo de colisión de `H-03`.

### Pregunta de Cierre Obligatoria
> **¿Qué práctica del proceso de desarrollo habría permitido detectar estos problemas antes de entregar la aplicación?**

**Respuesta Técnica:**  
La implementación rigurosa de **Pruebas de Integración Automatizadas** (o pruebas unitarias guiadas por **TDD - Test Driven Development**) junto con **Revisiones de Código por Pares (Code Reviews / Pull Request Reviews)** con foco en arquitectura.  
* Con una prueba de integración automatizada del flujo de registro donde se insertaran múltiples urgencias en orden secuencial, el error de `urgencias.get(urgencias.size() - 1)` habría fallado inmediatamente al verificar que los datos del evento crítico no coincidían con el paciente recién persistido.  
* Asimismo, una revisión de código atenta habría detectado de inmediato la violación del principio de responsabilidad única (SRP) al ver que un controlador de vista contenía lógica de búsqueda y validación de estados de negocio, y que los constructores de entidades generaban números aleatorios para claves primarias en lugar de delegarlo a la persistencia.

---

## 8. Trazabilidad Git (Commits del Taller)

* **Commit 1 (Diagnóstico Técnico):**
  * Mensaje: `docs: registrar diagnostico tecnico de refactorizacion para modulo de urgencias`
  * Archivos: `docs/refactorizacion/DIAGNOSTICO_REFACTORIZACION.md`
* **Commit 2 (Intervención y Refactorización):**
  * Mensaje: `refactor: separar responsabilidades en registro y alerta critica de urgencias`
  * Archivos afectados: `EmergenciaService.java`, `RecepcionistaController.java`, `RegistroUrgenciaController.java`
* **Commit 3 (Verificación y Cierre):**
  * Mensaje: `docs: documentar verificacion de refactorizacion y cierre de arquitectura`
  * Archivos: `docs/refactorizacion/DIAGNOSTICO_REFACTORIZACION.md`
