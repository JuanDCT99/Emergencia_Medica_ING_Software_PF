-- Seed data for Emergencia_Medica database
-- All passwords are SHA-256 hashes of "1234"

-- ============================================
-- SCHEMA UPDATES FOR AMBULANCIA INTEGRATION
-- ============================================

-- Create ambulancias table if not exists
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

-- Add ambulancia_id column to urgencias if not exists (nullable for backward compatibility)
ALTER TABLE urgencias ADD COLUMN IF NOT EXISTS ambulancia_id VARCHAR(20) AFTER empleado_id;
-- Add foreign key constraint
ALTER TABLE urgencias ADD CONSTRAINT IF NOT EXISTS fk_urgencia_ambulancia 
    FOREIGN KEY (ambulancia_id) REFERENCES ambulancias(id);

-- Cleanup in correct order (respect foreign keys)
DELETE FROM urgencias;
DELETE FROM ambulancias;
DELETE FROM pacientes;
DELETE FROM empleados;

-- Insert sample employees
INSERT INTO empleados (id, nombre_completo, usuario, contrasena_hash, rol) VALUES
('1001', 'Administrador Principal', 'admin', '03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4', 'ADMIN'),
('1002', 'María García - Recepción', 'recepcion', '03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4', 'RECEPCIONISTA'),
('1003', 'Dr. Juan Pérez - Médico', 'medico1', '03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4', 'MEDICO'),
('1004', 'Ana López - Enfermera', 'enfermera1', '03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4', 'ENFERMERO'),
('1005', 'Carlos Ruiz - Auxiliar', 'auxiliar1', '03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4', 'AUXILIAR');

-- Insert sample patients
INSERT INTO pacientes (id, nombre_completo, edad, genero, direccion, telefono, historia_clinica) VALUES
('PAC001', 'Pedro Martínez', 45, 'Masculino', 'Calle 5 #12-34', '3001234567', 'Hipertensión, Diabetes Tipo 2'),
('PAC002', 'Laura Sánchez', 28, 'Femenino', 'Avenida 10 #45-67', '3109876543', 'Alergia a penicilina, Asma'),
('PAC003', 'Roberto Gómez', 62, 'Masculino', 'Barrio Centro #89', '3201112233', 'Problemas cardíacos, Marcapasos'),
('PAC004', 'Carmen Díaz', 35, 'Femenino', 'Cra 15 #23-45', '3154445566', 'Embarazo - 7 meses'),
('PAC005', 'José Hernández', 8, 'Masculino', 'Vereda El Poblado', '3007778899', 'Sin antecedentes relevantes');

-- Insert sample ambulancias
INSERT INTO ambulancias (id, placa, estado, modelo, kilometraje, ultima_revision, fecha_alta, observaciones) VALUES
('AMB001', 'ABC-123', 'DISPONIBLE', 'Toyota Hiace 2023', 15000, '2026-04-15', '2023-06-10', 'Ambulancia básica - Equipo completo'),
('AMB002', 'DEF-456', 'DISPONIBLE', 'Mercedes Benz Sprinter', 28000, '2026-03-20', '2022-11-05', 'Ambulancia de soporte vital avanzado'),
('AMB003', 'GHI-789', 'DISPONIBLE', 'Ford Transit 2024', 8500, '2026-05-01', '2024-01-20', 'Ambulancia nueva - última revisión OK'),
('AMB004', 'JKL-012', 'FUERA_SERVICIO', 'Chevrolet N300', 45000, '2025-12-01', '2021-03-15', 'En mantenimiento - Problemas de frenos');

SELECT 'Seed data inserted successfully' AS result;
