-- =====================================================================
--  00 - Reiniciar el esquema (SOLO DESARROLLO)
--
--  ¡Borra todas las tablas, tipos y datos de psicometria_db!
--  Úsalo cuando cambie el modelo y quieras volver a ejecutar 02 y 03.
--  Ejecutar conectado a "psicometria_db".
-- =====================================================================

BEGIN;

DROP TABLE IF EXISTS aplicaciones_test, tokens_acceso, admins,
                     preguntas, tests, psicologos, evaluados, instituciones CASCADE;

DROP FUNCTION IF EXISTS set_actualizado_at();

DROP TYPE IF EXISTS estado_aplicacion, tipo_pregunta, visibilidad_test, categoria_test,
                    especialidad_psicologo, tipo_institucion, nivel_educativo, genero_evaluado;

COMMIT;
