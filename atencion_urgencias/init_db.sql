-- ============================================================
--  Script de Inicialización de Base de Datos
--  Sistema de Gestión de Urgencias - Universidad del Quindío
--  Motor: MariaDB 11.x
--  Este script se ejecuta una sola vez al iniciar el contenedor
--  de Docker por primera vez. Crea la base de datos, el usuario
--  'usuario_sistema', el esquema completo y los datos semilla.
-- ============================================================

-- 1. Crear la base de datos (solo si no existe)
CREATE DATABASE IF NOT EXISTS Emergencia_Medica;
USE Emergencia_Medica;

-- 2. Crear el usuario 'usuario_sistema' con password '1234'
--    Otorgamos todos los privilegios sobre la base de datos Emergencia_Medica.
CREATE USER IF NOT EXISTS 'usuario_sistema'@'%' IDENTIFIED BY '1234';
GRANT ALL PRIVILEGES ON Emergencia_Medica.* TO 'usuario_sistema'@'%';
FLUSH PRIVILEGES;

-- 3. Crear tabla: empleados
CREATE TABLE IF NOT EXISTS empleados (
    id VARCHAR(20) PRIMARY KEY,
    nombre_completo VARCHAR(100) NOT NULL,
    usuario VARCHAR(50) NOT NULL UNIQUE,
    contrasena_hash VARCHAR(64) NOT NULL,
    rol ENUM('RECEPCIONISTA', 'MEDICO', 'ENFERMERO', 'AUXILIAR', 'ADMIN') NOT NULL
);

-- 4. Crear tabla: pacientes
CREATE TABLE IF NOT EXISTS pacientes (
    id VARCHAR(20) PRIMARY KEY,
    nombre_completo VARCHAR(100) NOT NULL,
    edad INT,
    genero VARCHAR(20),
    direccion VARCHAR(200),
    telefono VARCHAR(20),
    historia_clinica TEXT
);

-- 5. Crear tabla: ambulancias
CREATE TABLE IF NOT EXISTS ambulancias (
    id VARCHAR(20) PRIMARY KEY,
    placa VARCHAR(20) NOT NULL UNIQUE,
    estado ENUM('DISPONIBLE', 'EN_USO', 'FUERA_SERVICIO') DEFAULT 'DISPONIBLE',
    modelo VARCHAR(50),
    kilometraje INT DEFAULT 0,
    ultima_revision DATE,
    fecha_alta DATE,
    observaciones TEXT
);

-- 6. Crear tabla: urgencias (con referencias a las anteriores)
CREATE TABLE IF NOT EXISTS urgencias (
    id VARCHAR(20) PRIMARY KEY,
    paciente_id VARCHAR(20),
    empleado_id VARCHAR(20),
    nivel_triage INT DEFAULT 3 CHECK (nivel_triage >= 1 AND nivel_triage <= 5),
    sintomas TEXT,
    ubicacion VARCHAR(200),
    signos_vitales VARCHAR(200),
    estado ENUM('PENDIENTE', 'EN_CURSO', 'FINALIZADO') DEFAULT 'PENDIENTE',
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ambulancia_id VARCHAR(20),
    FOREIGN KEY (paciente_id) REFERENCES pacientes(id),
    FOREIGN KEY (empleado_id) REFERENCES empleados(id),
    FOREIGN KEY (ambulancia_id) REFERENCES ambulancias(id)
);

-- 7. Insertar empleados (contraseñas: SHA-256 de "1234")
INSERT INTO empleados (id, nombre_completo, usuario, contrasena_hash, rol) VALUES
('1001', 'Administrador Principal', 'admin', '03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4', 'ADMIN'),
('1002', 'María García - Recepción', 'recepcion', '03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4', 'RECEPCIONISTA'),
('1003', 'Dr. Juan Pérez - Médico', 'medico1', '03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4', 'MEDICO'),
('1004', 'Ana López - Enfermera', 'enfermera1', '03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4', 'ENFERMERO'),
('1005', 'Carlos Ruiz - Auxiliar', 'auxiliar1', '03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4', 'AUXILIAR');

-- 8. Insertar pacientes
INSERT INTO pacientes (id, nombre_completo, edad, genero, direccion, telefono, historia_clinica) VALUES
('PAC001', 'Pedro Martínez', 45, 'Masculino', 'Calle 5 #12-34', '3001234567', 'Hipertensión, Diabetes Tipo 2'),
('PAC002', 'Laura Sánchez', 28, 'Femenino', 'Avenida 10 #45-67', '3109876543', 'Alergia a penicilina, Asma'),
('PAC003', 'Roberto Gómez', 62, 'Masculino', 'Barrio Centro #89', '3201112233', 'Problemas cardíacos, Marcapasos'),
('PAC004', 'Carmen Díaz', 35, 'Femenino', 'Cra 15 #23-45', '3154445566', 'Embarazo - 7 meses'),
('PAC005', 'José Hernández', 8, 'Masculino', 'Vereda El Poblado', '3007778899', 'Sin antecedentes relevantes');

-- 9. Insertar ambulancias
INSERT INTO ambulancias (id, placa, estado, modelo, kilometraje, ultima_revision, fecha_alta, observaciones) VALUES
('AMB001', 'ABC-123', 'DISPONIBLE', 'Toyota Hiace 2023', 15000, '2026-04-15', '2023-06-10', 'Ambulancia básica - Equipo completo'),
('AMB002', 'DEF-456', 'DISPONIBLE', 'Mercedes Benz Sprinter', 28000, '2026-03-20', '2022-11-05', 'Ambulancia de soporte vital avanzado'),
('AMB003', 'GHI-789', 'DISPONIBLE', 'Ford Transit 2024', 8500, '2026-05-01', '2024-01-20', 'Ambulancia nueva - última revisión OK'),
('AMB004', 'JKL-012', 'FUERA_SERVICIO', 'Chevrolet N300', 45000, '2025-12-01', '2021-03-15', 'En mantenimiento - Problemas de frenos');

-- 10. Confirmación
SELECT 'Base de datos Emergencia_Medica inicializada correctamente.' AS resultado;