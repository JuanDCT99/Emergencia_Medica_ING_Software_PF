---
id: readme-equipo
title: README Equipo - Sistema Gestión Urgencias
---

# 📋 README para el Equipo de Desarrollo

> Sistema de Gestión de Urgencias Hospitalarias  
> Universidad del Quindío - Ingeniería de Sistemas y Computación  
> Desarrollado por: Camilo Ruiz López, Juan David Cardozo Torrez, Tomás Castaño

---

## 🗂️ Estructura del Proyecto

```
atencion_urgencias/
├── pom.xml                          # Configuración de Maven (Java 21, JavaFX 21, Lombok, MariaDB JDBC)
├── docker-compose.yml               # Entorno de base de datos reproducible (MariaDB 11.8)
├── init_db.sql                      # Script de esquema y datos semilla para la BD
├── documentacion/                   # Documentación del proyecto (mira aquí)
├── src/
│   ├── main/
│   │   ├── java/com/ingenieria/software1/
│   │   │   ├── App.java              # Punto de entrada de la aplicación JavaFX
│   │   │   ├── controller/           # Controladores (lógica de las vistas FXML)
│   │   │   ├── model/                # Modelos (Urgencia, Paciente, Empleado, Triage, Ambulancia, RolEmpleado)
│   │   │   └── service/              # Servicios y DAOs (EmergenciaService, UsuarioService, DAOs)
│   │   └── resources/
│   │       ├── com/ingenieria/software1/view/*.fxml   # Vistas definidas en FXML
│   │       └── config/
│   │           ├── db.properties       # Credenciales de conexión a la BD
│   │           └── seed_data.sql       # [OBSOLETE] Se reemplazó por ../init_db.sql
│   └── test/                           # Tests unitarios (actualizado: AppTest.java es esqueleto)
└── target/                             # Output de la compilación
```

---

## 🛠️ Requisitos Previos

Asegúrate de tener instalado lo siguiente en tu máquina:

1.  **Java Development Kit (JDK) 21 o superior**
    *   Puedes verificarlo corriendo: `java --version`
    *   Debes ver algo como `openjdk version "21.x.x"`.
2.  **Apache Maven 3.8 o superior**
    *   Verifícalo con: `mvn --version`
3.  **Docker Desktop** (altamente recomendado para la base de datos).
    *   Descárgalo en: https://www.docker.com/products/docker-desktop
    *   *Alternativa*: Tener MariaDB instalado localmente (ver sección "Sin Docker").

---

## 🐳 Puesta en Marcha con Docker (Recomendado para el Equipo)

Esta es la forma más sencilla y reproducible de poner en marcha el proyecto. Docker se encargará de crear y configurar la base de datos MariaDB con las credenciales y los datos correctos, garantizando que todos los miembros del equipo trabajen con el mismo entorno.

1.  **Abrir una terminal** y navegar al directorio `atencion_urgencias` (donde está `docker-compose.yml`).
2.  **Levantar la base de datos:**
    ```bash
    docker compose up -d
    ```
    *   Esto descargará la imagen de MariaDB, la iniciará en segundo plano (`-d`), la conectará al puerto `3307` de tu máquina, y cargará el script `init_db.sql` para crear la base de datos `Emergencia_Medica`, sus tablas y los datos semilla.
    *   El primer levantamiento puede tardar unos minutos en descargar la imagen.
3.  **(Opcional) Verificar que el contenedor esté corriendo:**
    ```bash
    docker compose ps
    ```
    Deberías ver el estado `Up` para el contenedor `urgencias_mariadb`.
4.  **Abrir el proyecto en tu IDE favorito** (VS Code, IntelliJ IDEA, NetBeans).
    *   Si usas VS Code, abre la carpeta `atencion_urgencias`.
    *   Asegúrate de tener la extensión de Java y Maven instaladas.
5.  **Compilar y correr la aplicación desde la terminal (dentro de `atencion_urgencias/`):**
    ```bash
    mvn javafx:run
    ```
    *   Esto compilará el código, descargará las dependencias de Maven (JavaFX, Lombok, etc.) la primera vez, y lanzará la aplicación.
6.  **¡Listo!** La aplicación debería abrirse en una ventana. Usa las credenciales de prueba para iniciar sesión.

---

## 💻 Puesta en Marcha Sin Docker (Alternativa Local)

Si prefieres no usar Docker, puedes instalar y configurar MariaDB directamente en tu máquina.

1.  **Instalar MariaDB** en tu sistema operativo (si aún no lo has hecho).
2.  **Acceder a MariaDB como administrador (root)** y ejecutar el script `init_db.sql`:
    *   Necesitarás ajustar el archivo `src/main/resources/config/db.properties` para apuntar al puerto `3306` (el estándar para MariaDB local).
    *   *Ejemplo de cómo correr el script (los comandos varían según el SO):*
        ```bash
        # En Linux, posiblemente necesites sudo:
        sudo mariadb < init_db.sql
        ```
3.  **Configurar `db.properties`:** Asegúrate de que las credenciales coincidan con las de tu instalación local. El código espera:
    *   URL: `jdbc:mariadb://localhost:3306/Emergencia_Medica`
    *   Usuario: `usuario_sistema`
    *   Password: `1234`
    *   Si tu usuario local tiene un nombre distinto, créalo o actualiza este archivo:
        ```sql
        CREATE USER 'usuario_sistema'@'localhost' IDENTIFIED BY '1234';
        GRANT ALL PRIVILEGES ON Emergencia_Medica.* TO 'usuario_sistema'@'localhost';
        FLUSH PRIVILEGES;
        ```
4.  **Compilar y correr:** `mvn javafx:run` (igual que en el paso 5 de Docker).

---

## 🔑 Credenciales de Prueba

Todas las contraseñas son `1234`.

| Usuario     | Rol             | Funcionalidad                                           |
| :---------- | :-------------- | :------------------------------------------------------ |
| `admin`     | **ADMIN**       | Panel de admin: gestionar usuarios y ambulancias.       |
| `recepcion` | **RECEPCIONISTA** | Registrar nuevas urgencias, despachar (asignar médico). |
| `medico1`   | **MEDICO**      | Ver y atender urgencias asignadas, finalizarlas.        |
| `enfermera1`| **ENFERMERO**   | Ver y gestionar urgencias asignadas.                    |
| `auxiliar1` | **AUXILIAR**    | Ver y gestionar urgencias asignadas.                    |

---

## 🧪 Guía de Pruebas Manuales

Para validar el funcionamiento de las funcionalidades, consulta el documento completo en `documentacion/GUIA_PRUEBAS.md`.

---

## 📝 Reporte de Errores Conocidos

Un análisis detallado de bugs y mejoras está disponible en `documentacion/REPORTE_ERRORES.md`.

---

## 🤝 Notas de Desarrollo para el Equipo

*   **Commits atómicos:** Realiza commits pequeños y descriptivos para cada funcionalidad o corrección.
    *   *Ejemplo bueno:* `fix: corregir NPE en despacho de urgencia sin ambulancia`
    *   *Ejemplo malo:* `fix: varios bugs`
*   **Branches:** Se recomienda usar ramas para nuevas funcionalidades:
    *   `git checkout -b feature/nueva-funcionalidad`
*   **Sincroniza con frecuencia:** Antes de empezar a trabajar, ejecuta `git pull` para obtener los últimos cambios.
*   **Docker es tu amigo:** Usar `docker compose` asegura que la base de datos sea siempre la misma para todos. Si cambias el esquema (`init_db.sql`), avisa al equipo o actualiza todo el mundo.
*   **Documenta:** Mantén esta documentación actualizada. Si cambias algo importante, anótalo aquí o en el reporte de errores.