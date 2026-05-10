-- Seed data for Emergencia_Medica database
-- All passwords are SHA-256 hashes of "1234"

DELETE FROM urgencias;
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

SELECT 'Seed data inserted successfully' AS result;
