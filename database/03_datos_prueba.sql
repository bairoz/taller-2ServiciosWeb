-- =====================================================================
--  03 - Datos de prueba
--
--  Ejecutar conectado a "psicometria_db" después de 02_esquema.sql.
--  Las claves foráneas se resuelven por campos únicos (email, nombre,
--  numero_registro) para no depender de los id generados.
-- =====================================================================

BEGIN;

-- ---------------------------------------------------------------------
--  instituciones
-- ---------------------------------------------------------------------
INSERT INTO instituciones (nombre, tipo, ciudad, direccion, email_contacto, telefono, cantidad_estudiantes, fecha_convenio, activa) VALUES
  ('Liceo Bicentenario Andrés Bello', 'LICEO',                 'Santiago',     'Av. Libertador 1450',  'orientacion@liceoandresbello.cl', '+56223456789', 1200,  '2024-03-01', TRUE),
  ('Universidad del Pacífico Sur',    'UNIVERSIDAD',           'Valparaíso',   'Calle Brasil 2950',    'bienestar@upacificosur.cl',       '+56322123456', 8500,  '2023-08-15', TRUE),
  ('Instituto Técnico Los Andes',     'INSTITUTO_PROFESIONAL', 'Concepción',   'O''Higgins 780',       'contacto@itlosandes.cl',          '+56412987654', 3100,  '2025-01-20', TRUE),
  ('Colegio San Martín',              'COLEGIO',               'La Serena',    'Balmaceda 321',        'direccion@colegiosanmartin.cl',   NULL,           640,   '2022-04-10', FALSE);

-- ---------------------------------------------------------------------
--  psicologos
-- ---------------------------------------------------------------------
INSERT INTO psicologos (nombre, apellido, email, numero_registro, especialidad, anios_experiencia, fecha_titulacion, disponible) VALUES
  ('Valentina', 'Muñoz',    'vmunoz@psicometria.cl',   'PSI-2011-0452', 'EDUCACIONAL',     14, '2011-12-15', TRUE),
  ('Andrés',    'Fuentes',  'afuentes@psicometria.cl', 'PSI-2016-1180', 'VOCACIONAL',       9, '2016-07-01', TRUE),
  ('Carolina',  'Espinoza', 'cespinoza@psicometria.cl','PSI-2008-0097', 'NEUROPSICOLOGIA', 17, '2008-03-30', FALSE);

-- ---------------------------------------------------------------------
--  evaluados
-- ---------------------------------------------------------------------
INSERT INTO evaluados (institucion_id, nombre, apellido, email, fecha_nacimiento, genero, nivel_educativo, activo)
SELECT i.id, v.nombre, v.apellido, v.email, v.fecha_nacimiento::date, v.genero::genero_evaluado, v.nivel::nivel_educativo, v.activo
FROM (VALUES
  ('Liceo Bicentenario Andrés Bello', 'Camila',   'Rojas',     'camila.rojas@correo.cl',     '2009-03-21', 'FEMENINO',          'MEDIA',         TRUE),
  ('Liceo Bicentenario Andrés Bello', 'Matías',   'González',  'matias.gonzalez@correo.cl',  '2008-11-02', 'MASCULINO',         'MEDIA',         TRUE),
  ('Universidad del Pacífico Sur',    'Sofía',    'Pérez',     'sofia.perez@correo.cl',      '2004-06-14', 'FEMENINO',          'UNIVERSITARIO', TRUE),
  ('Universidad del Pacífico Sur',    'Alex',     'Contreras', 'alex.contreras@correo.cl',   '2003-01-30', 'NO_BINARIO',        'UNIVERSITARIO', TRUE),
  ('Instituto Técnico Los Andes',     'Diego',    'Soto',      'diego.soto@correo.cl',       '2005-09-09', 'MASCULINO',         'TECNICO',       TRUE),
  ('Colegio San Martín',              'Martina',  'Vargas',    'martina.vargas@correo.cl',   '2012-04-18', 'PREFIERO_NO_DECIR', 'BASICA',        FALSE)
) AS v(institucion, nombre, apellido, email, fecha_nacimiento, genero, nivel, activo)
JOIN instituciones i ON i.nombre = v.institucion;

-- ---------------------------------------------------------------------
--  tests
-- ---------------------------------------------------------------------
INSERT INTO tests (psicologo_id, titulo, descripcion, categoria, duracion_minutos, puntaje_aprobacion, visibilidad)
SELECT p.id, v.titulo, v.descripcion, v.categoria::categoria_test, v.duracion, v.aprobacion, v.visibilidad::visibilidad_test
FROM (VALUES
  ('PSI-2016-1180', 'Test de Intereses Vocacionales',   'Identifica áreas profesionales afines a los intereses del estudiante.', 'VOCACIONAL',   30,   0.00,  'PUBLICO'),
  ('PSI-2011-0452', 'Razonamiento Lógico-Matemático',   'Evalúa la capacidad de resolver problemas numéricos y de lógica.',     'APTITUD',      45,  60.00,  'PRIVADO'),
  ('PSI-2011-0452', 'Inventario de Inteligencia Emocional', 'Mide autoconocimiento, autorregulación y empatía.',               'EMOCIONAL',    NULL, 0.00,  'BORRADOR')
) AS v(registro, titulo, descripcion, categoria, duracion, aprobacion, visibilidad)
JOIN psicologos p ON p.numero_registro = v.registro;

-- ---------------------------------------------------------------------
--  preguntas
-- ---------------------------------------------------------------------
INSERT INTO preguntas (test_id, enunciado, tipo, opciones, respuesta_correcta, puntaje, orden, obligatoria)
SELECT t.id, v.enunciado, v.tipo::tipo_pregunta, v.opciones::jsonb, v.correcta, v.puntaje, v.orden, v.obligatoria
FROM (VALUES
  ('Test de Intereses Vocacionales', 'Me gusta resolver problemas técnicos o mecánicos.', 'ESCALA_LIKERT',
     '["Muy en desacuerdo","En desacuerdo","Neutral","De acuerdo","Muy de acuerdo"]', NULL, 1.00, 1, TRUE),
  ('Test de Intereses Vocacionales', 'Disfruto ayudar a otras personas con sus problemas.', 'ESCALA_LIKERT',
     '["Muy en desacuerdo","En desacuerdo","Neutral","De acuerdo","Muy de acuerdo"]', NULL, 1.00, 2, TRUE),
  ('Test de Intereses Vocacionales', '¿Qué carrera te gustaría estudiar y por qué?', 'RESPUESTA_ABIERTA',
     NULL, NULL, 0.00, 3, FALSE),
  ('Razonamiento Lógico-Matemático', '¿Qué número sigue en la serie 2, 6, 12, 20, ...?', 'OPCION_MULTIPLE',
     '["28","30","32","36"]', '30', 2.00, 1, TRUE),
  ('Razonamiento Lógico-Matemático', 'Si todos los A son B y ningún B es C, entonces ningún A es C.', 'VERDADERO_FALSO',
     '["Verdadero","Falso"]', 'Verdadero', 1.00, 2, TRUE),
  ('Razonamiento Lógico-Matemático', 'Un tren recorre 180 km en 2 horas. ¿Cuál es su velocidad media?', 'OPCION_MULTIPLE',
     '["60 km/h","80 km/h","90 km/h","120 km/h"]', '90 km/h', 2.00, 3, TRUE),
  ('Inventario de Inteligencia Emocional', 'Reconozco fácilmente cuándo estoy estresado.', 'ESCALA_LIKERT',
     '["Nunca","Rara vez","A veces","Frecuentemente","Siempre"]', NULL, 1.00, 1, TRUE)
) AS v(test, enunciado, tipo, opciones, correcta, puntaje, orden, obligatoria)
JOIN tests t ON t.titulo = v.test;

-- ---------------------------------------------------------------------
--  tokens_acceso (soporte)
-- ---------------------------------------------------------------------
INSERT INTO tokens_acceso (test_id, codigo_pin, utilizado, fecha_expiracion, evaluado_id, fecha_uso)
SELECT t.id, 'RLM-7F3K9Q', TRUE, now() + INTERVAL '30 days', e.id, now() - INTERVAL '2 days'
FROM tests t, evaluados e
WHERE t.titulo = 'Razonamiento Lógico-Matemático' AND e.email = 'sofia.perez@correo.cl';

INSERT INTO tokens_acceso (test_id, codigo_pin, fecha_expiracion)
SELECT id, 'RLM-X2M8PA', now() + INTERVAL '30 days'
FROM tests WHERE titulo = 'Razonamiento Lógico-Matemático';

-- ---------------------------------------------------------------------
--  aplicaciones_test (soporte)
-- ---------------------------------------------------------------------
INSERT INTO aplicaciones_test (evaluado_id, test_id, token_id, estado, respuestas, puntaje_obtenido, aprobado, fecha_inicio, fecha_fin)
SELECT e.id, t.id, k.id, 'COMPLETADO', '{}', 83.33, TRUE, now() - INTERVAL '2 days', now() - INTERVAL '2 days' + INTERVAL '38 minutes'
FROM evaluados e
JOIN tokens_acceso k ON k.evaluado_id = e.id
JOIN tests t ON t.id = k.test_id
WHERE e.email = 'sofia.perez@correo.cl';

INSERT INTO aplicaciones_test (evaluado_id, test_id, estado)
SELECT e.id, t.id, 'EN_PROGRESO'
FROM evaluados e, tests t
WHERE e.email = 'camila.rojas@correo.cl' AND t.titulo = 'Test de Intereses Vocacionales';

COMMIT;
