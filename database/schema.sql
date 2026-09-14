-- =====================================================================
--  Plataforma de Tests Psicométricos para Estudiantes
--  Esquema PostgreSQL (versión mejorada)
--
--  Entidades principales (CRUD, una por integrante):
--    1. evaluados          -> estudiantes que rinden los tests
--    2. tests              -> instrumentos psicométricos
--    3. preguntas          -> ítems de cada test
--    4. tokens_acceso      -> PIN de un solo uso para tests privados
--    5. aplicaciones_test  -> cada vez que un evaluado rinde un test
--
--  Tabla de soporte (autenticación, fuera del alcance del CRUD):
--    admins
-- =====================================================================

-- ---------------------------------------------------------------------
--  Tipos enumerados
-- ---------------------------------------------------------------------
CREATE TYPE genero_evaluado AS ENUM ('MASCULINO', 'FEMENINO', 'NO_BINARIO', 'PREFIERO_NO_DECIR');
CREATE TYPE nivel_educativo AS ENUM ('BASICA', 'MEDIA', 'TECNICO', 'UNIVERSITARIO', 'POSTGRADO');
CREATE TYPE categoria_test  AS ENUM ('PERSONALIDAD', 'APTITUD', 'INTELIGENCIA', 'VOCACIONAL', 'EMOCIONAL');
CREATE TYPE visibilidad_test AS ENUM ('BORRADOR', 'PRIVADO', 'PUBLICO');
CREATE TYPE tipo_pregunta   AS ENUM ('OPCION_MULTIPLE', 'VERDADERO_FALSO', 'ESCALA_LIKERT', 'RESPUESTA_ABIERTA');
CREATE TYPE estado_aplicacion AS ENUM ('EN_PROGRESO', 'COMPLETADO', 'ABANDONADO', 'EXPIRADO');

-- ---------------------------------------------------------------------
--  Función genérica para mantener actualizado_at
-- ---------------------------------------------------------------------
CREATE OR REPLACE FUNCTION set_actualizado_at()
RETURNS TRIGGER AS $$
BEGIN
  NEW.actualizado_at = now();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- ---------------------------------------------------------------------
--  Soporte: admins
-- ---------------------------------------------------------------------
CREATE TABLE admins (
  id             SERIAL PRIMARY KEY,
  usuario        VARCHAR(100) NOT NULL UNIQUE,
  email          VARCHAR(255) NOT NULL UNIQUE,
  password_hash  VARCHAR(255) NOT NULL,
  activo         BOOLEAN      NOT NULL DEFAULT TRUE,
  creado_at      TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- ---------------------------------------------------------------------
--  1. evaluados
-- ---------------------------------------------------------------------
CREATE TABLE evaluados (
  id                SERIAL PRIMARY KEY,
  nombre            VARCHAR(100)    NOT NULL,
  apellido          VARCHAR(100)    NOT NULL,
  email             VARCHAR(255)    NOT NULL UNIQUE,
  fecha_nacimiento  DATE            NOT NULL CHECK (fecha_nacimiento < CURRENT_DATE),
  genero            genero_evaluado NOT NULL DEFAULT 'PREFIERO_NO_DECIR',
  nivel_educativo   nivel_educativo NOT NULL,
  activo            BOOLEAN         NOT NULL DEFAULT TRUE,
  creado_at         TIMESTAMPTZ     NOT NULL DEFAULT now(),
  actualizado_at    TIMESTAMPTZ     NOT NULL DEFAULT now()
);

CREATE TRIGGER trg_evaluados_actualizado
  BEFORE UPDATE ON evaluados
  FOR EACH ROW EXECUTE FUNCTION set_actualizado_at();

-- ---------------------------------------------------------------------
--  2. tests
-- ---------------------------------------------------------------------
CREATE TABLE tests (
  id                  SERIAL PRIMARY KEY,
  titulo              VARCHAR(255)     NOT NULL,
  descripcion         TEXT,
  categoria           categoria_test   NOT NULL,
  duracion_minutos    INT              CHECK (duracion_minutos IS NULL OR duracion_minutos > 0), -- NULL = sin límite
  puntaje_aprobacion  NUMERIC(5,2)     NOT NULL DEFAULT 60.00
                                       CHECK (puntaje_aprobacion BETWEEN 0 AND 100),
  visibilidad         visibilidad_test NOT NULL DEFAULT 'BORRADOR',
                      -- BORRADOR = solo visible para el admin
                      -- PRIVADO  = accesible únicamente con un PIN de un solo uso
                      -- PUBLICO  = acceso abierto, aparece en la lista de tests
  creado_at           TIMESTAMPTZ      NOT NULL DEFAULT now(),
  actualizado_at      TIMESTAMPTZ      NOT NULL DEFAULT now()
);

CREATE TRIGGER trg_tests_actualizado
  BEFORE UPDATE ON tests
  FOR EACH ROW EXECUTE FUNCTION set_actualizado_at();

-- ---------------------------------------------------------------------
--  3. preguntas
-- ---------------------------------------------------------------------
CREATE TABLE preguntas (
  id                  SERIAL PRIMARY KEY,
  test_id             INT           NOT NULL REFERENCES tests(id) ON DELETE CASCADE,
  enunciado           TEXT          NOT NULL,
  tipo                tipo_pregunta NOT NULL,
  opciones            JSONB,        -- array de strings; NULL en RESPUESTA_ABIERTA
  respuesta_correcta  TEXT,         -- NULL en preguntas sin respuesta correcta (p. ej. Likert)
  puntaje             NUMERIC(5,2)  NOT NULL DEFAULT 1.00 CHECK (puntaje >= 0),
  orden               INT           NOT NULL CHECK (orden > 0),
  obligatoria         BOOLEAN       NOT NULL DEFAULT TRUE,
  CONSTRAINT uq_preguntas_test_orden UNIQUE (test_id, orden)
);

-- ---------------------------------------------------------------------
--  4. tokens_acceso  (absorbe el antiguo historial_usos_tokens)
-- ---------------------------------------------------------------------
CREATE TABLE tokens_acceso (
  id                SERIAL PRIMARY KEY,
  test_id           INT          NOT NULL REFERENCES tests(id) ON DELETE CASCADE,
  codigo_pin        VARCHAR(50)  NOT NULL UNIQUE,
  utilizado         BOOLEAN      NOT NULL DEFAULT FALSE,
  fecha_expiracion  TIMESTAMPTZ  NOT NULL,
  evaluado_id       INT          REFERENCES evaluados(id) ON DELETE SET NULL, -- quién lo usó
  fecha_uso         TIMESTAMPTZ,                                              -- cuándo lo usó
  creado_at         TIMESTAMPTZ  NOT NULL DEFAULT now(),
  CONSTRAINT ck_tokens_uso CHECK (utilizado = (fecha_uso IS NOT NULL))
);

-- ---------------------------------------------------------------------
--  5. aplicaciones_test
-- ---------------------------------------------------------------------
CREATE TABLE aplicaciones_test (
  id                SERIAL PRIMARY KEY,
  evaluado_id       INT               NOT NULL REFERENCES evaluados(id) ON DELETE CASCADE,
  test_id           INT               NOT NULL REFERENCES tests(id) ON DELETE CASCADE,
  token_id          INT               UNIQUE REFERENCES tokens_acceso(id) ON DELETE SET NULL, -- NULL en tests públicos
  estado            estado_aplicacion NOT NULL DEFAULT 'EN_PROGRESO',
  respuestas        JSONB             NOT NULL DEFAULT '{}', -- respuestas indexadas por pregunta_id
  puntaje_obtenido  NUMERIC(5,2)      CHECK (puntaje_obtenido BETWEEN 0 AND 100),
  aprobado          BOOLEAN,          -- se calcula al completar
  fecha_inicio      TIMESTAMPTZ       NOT NULL DEFAULT now(),
  fecha_fin         TIMESTAMPTZ       CHECK (fecha_fin IS NULL OR fecha_fin >= fecha_inicio),
  actualizado_at    TIMESTAMPTZ       NOT NULL DEFAULT now()
);

CREATE TRIGGER trg_aplicaciones_actualizado
  BEFORE UPDATE ON aplicaciones_test
  FOR EACH ROW EXECUTE FUNCTION set_actualizado_at();

-- ---------------------------------------------------------------------
--  Índices
-- ---------------------------------------------------------------------
CREATE INDEX idx_preguntas_test         ON preguntas (test_id);
CREATE INDEX idx_tokens_test            ON tokens_acceso (test_id);
CREATE INDEX idx_aplicaciones_evaluado  ON aplicaciones_test (evaluado_id);
CREATE INDEX idx_aplicaciones_test      ON aplicaciones_test (test_id);
