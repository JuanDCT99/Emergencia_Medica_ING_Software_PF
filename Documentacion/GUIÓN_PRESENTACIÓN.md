# GUION PARA PRESENTACIÓN DEL PROYECTO
# Sistema de Gestión de Urgencias Hospitalarias
# Exposición individual — Preparado para Juanda

---

## DATOS RÁPIDOS (para cuando te pregunten "¿de qué va?")
- **Qué es:** Sistema de escritorio para gestionar urgencias en un hospital.
- **Quién lo usa:** Recepcionistas, médicos, enfermeros, auxiliares y administradores.
- **Qué resuelve:** Registro rápido de pacientes, clasificación por gravedad (Triage 1-5), asignación de personal y ambulancias, seguimiento del estado de cada urgencia.

---

## GUION — APROXIMADAMENTE 5-7 MINUTOS HABLADO

### 1. APERTURA (30-45 segundos)
"Buenos días/tardes. El proyecto que les voy a presentar es un Sistema de Gestión de Urgencias Hospitalarias. La idea nació de un problema clásico de ingeniería de software: un hospital necesita controlar varias urgencias al mismo tiempo, con distintos tipos de personal involucrado, y sin un sistema centralizado cada uno maneja su propia información y se pierden datos, se duplican registros, y los pacientes más graves pueden pasar desapercibidos.

Nuestra solución es una aplicación de escritorio que controla todo el ciclo de vida de una urgencia: desde que llega el paciente, se registra, se clasifica por gravedad, se asigna médico y ambulancia si hace falta, y se cierra cuando la atención termina."

### 2. ARQUITECTURA GENERAL (1-2 minutos)
"La aplicación sigue una arquitectura MVC — Modelo, Vista, Controlador — que es el patrón estándar en JavaFX.

- **Vista:** Son los archivos FXML, que son como HTML pero para JavaFX. Definen qué ve el usuario: la pantalla de login, el panel del recepcionista con la tabla de urgencias, los formularios de registro, etc.
- **Controlador:** Es la lógica que conecta botones y celdas de tabla con acciones. Por ejemplo, cuando el recepcionista da clic en 'Registrar', el controlador toma los datos del formulario, crea un objeto Paciente y una Urgencia, y los persiste.
- **Modelo:** Son las clases que representan las entidades del dominio: Empleado, Paciente, Urgencia, Triage, Ambulancia.

Para la persistencia cambiamos de CSV a una base de datos MariaDB desde el principio, porque un sistema de urgencias no puede perder información. Usamos JDBC con un patrón DAO — Data Access Object — que encapsula cada operación de base de datos en sus propias clases: EmpleadoDAO, PacienteDAO, UrgenciaDAO, AmbulanciaDAO. Esto hace que si mañana cambiamos la base de datos, solo tocamos esas capas y no el resto del sistema.

Para la conexión usamos un Singleton llamado DatabaseService que lee las credenciales desde un archivo db.properties. Esto separa la configuración del código."

### 3. FUNCIONALIDADES PRINCIPALES (2-3 minutos)
"Hablemos de lo que puede hacer el sistema desde la perspectiva del usuario:

**Primero, autenticación por roles.** Al iniciar sesión, el sistema identifica el rol del empleado — Recepcionista, Médico, Enfermero, Auxiliar o Admin — y le muestra solo lo que ese rol necesita. El recepcionista ve la tabla de urgencias y puede registrar nuevas; el médico ve solo las urgencias que se le asignaron; el admin puede gestionar usuarios y ambulancias.

**Segundo, registro de urgencias con Triage.** Cuando llega un paciente, el recepcionista registra su documento, nombre, edad, género, síntomas, ubicación y — lo más importante — el nivel de Triage del 1 al 5. El 5 es el más grave. Esto no es solo un número decorativo: el sistema usa ese nivel para mostrar alertas visuales, con colores para gravedad 4 y 5, y lanza una notificación emergente si es nivel 5, para que nadie pase por alto una emergencia crítica.

**Tercero, despacho de recursos.** Una vez registrada la urgencia, el recepcionista puede asignarle un médico y, opcionalmente, una ambulancia. Al despachar, el estado cambia de PENDIENTE a EN_CURSO y, si se asignó ambulancia, el sistema cambia automáticamente su estado a 'en uso'. Así nadie manda una ambulancia que ya está atendiendo a otro paciente.

**Cuarto, atención y cierre.** El médico ve en su panel las urgencias EN_CURSO asignadas a él, puede tomar nota de lo que revise y, cuando termina, finaliza la urgencia. Al finalizar, el estado pasa a FINALIZADO y, si había ambulancia asignada, el sistema vuelve a marcarla como disponible. Todo el ciclo queda registrado en la base de datos.

**Quinto, gestión de usuarios y ambulancias.** El admin tiene un panel completo para crear, editar y eliminar usuarios del sistema, y un módulo aparte para registrar ambulancias, ver cuáles están disponibles y cuáles están en uso."

### 4. TECNOLOGÍAS Y DECISIONES DE DISEÑO (1 minuto)
"Las decisiones técnicas que marcaron el proyecto fueron:

- **Java 21 con JavaFX 21:** elegimos JavaFX porque es el framework GUI de Java más maduro y tiene soporte nativo para MVC mediante FXML.
- **Maven:** para manejar dependencias y el ciclo de compilación. Las dependencias clave son el driver de MariaDB, Lombok para reducir código repetitivo con anotaciones, y JUnit para pruebas.
- **MariaDB en Docker:** para que cualquier miembro del equipo pueda levantar la misma base de datos con un solo comando — `docker compose up -d` — y tener el esquema y datos semilla listos sin configuración manual.
- **Hashing SHA-256 de contraseñas:** por seguridad, las contraseñas nunca se guardan en texto plano.
- **Patrón Singleton en los servicios:** UsuarioService, EmergenciaService y AmbulanciaService son singletons porque representan recursos únicos del sistema — la sesión activa, el registro de urgencias, el registro de ambulancias — y no queremos multiples instancias camino entre sí."

### 5. ESTADO ACTUAL Y PRÓXIMOS PASOS (30-45 segundos)
"El sistema ya está funcional en sus partes principales: login por roles, registro de urgencias con Triage, despacho de médico y ambulancia, atención médica, cierre de urgencias, gestión de usuarios y gestión de ambulancias.

Los próximos pasos que quedan identificados son: implementar estadísticas de tiempos de espera para medir el desempeño del servicio, mejorar la cobertura de pruebas unitarias que actualmente es un esqueleto, y revisar algunos detalles de experiencia de usuario como la navegación después de guardar un formulario."

---

## PREGUNTAS QUE TE PUEDEN HACER Y CÓMO RESPONDER

**¿Por qué MariaDB y no otra base de datos?**
MariaDB es un fork de MySQL, compatible con el driver JDBC que usamos, y se puede levantar en Docker con un solo archivo compose. Es gratuito, ligero y suficiente para el volumen de datos que maneja un sistema de urgencias.

**¿Qué pasaría si la base de datos cae?**
La aplicación no podría iniciar sesión ni registrar nada. En un entorno real se tendría un esquema de backup y failover, pero para este proyecto el enfoque fue la funcionalidad operativa. El diseño por capas — DAO separado del servicio — permite en el futuro cambiarlo por una base de datos más robusta sin reescribir la lógica de negocio.

**¿Cómo maneja la seguridad los usuarios?**
Las contraseñas se hashean con SHA-256 antes de guardarse. El login compara el hash ingresado contra el hash almacenado. Además, el control de acceso es por roles: cada controlador verifica el rol del usuario logueado antes de permitir una acción. El rol ADMIN es el único que puede crear y eliminar usuarios.

**¿Qué es el Triage y por qué es importante?**
El Triage es la clasificación de urgencia que hace el personal de salud al llegar un paciente. En nuestro sistema es un nivel del 1 al 5 donde 5 es el más crítico. No es solo un campo más: el sistema reacciona visualmente a los niveles altos con alertas, porque en una sala de urgencias real ese paciente no puede esperar en la misma fila que uno con un resfriado."

---

## SI TE PIDEN MOSTRAR EL CÓDIGO EN VIVO

Archivos clave para abrir en VS Code si te piden demo:
- `atencion_urgencias/src/main/java/com/ingenieria/software1/App.java` — punto de entrada y navegación entre vistas.
- `atencion_urgencias/src/main/java/com/ingenieria/software1/service/EmergenciaService.java` — lógica central: registrar, despachar, finalizar urgencias.
- `atencion_urgencias/src/main/java/com/ingenieria/software1/service/DatabaseService.java` — conexión a MariaDB.
- `atencion_urgencias/src/main/java/com/ingenieria/software1/controller/RecepcionistaController.java` — la ventana principal del recepcionista.
- `atencion_urgencias/src/main/resources/config/db.properties` — configuración de conexión.
- `init_db.sql` en la raíz — esquema de la base de datos.

---

Guion generado: 28 de septiembre de 2026
Proyecto: /home/juanda/Github/Software/Emergencia_Medica_ING_Software_PF
