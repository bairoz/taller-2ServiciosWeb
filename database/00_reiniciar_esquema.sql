-- =====================================================================
--  00 - Reiniciar el esquema (SOLO DESARROLLO)
--
--  ¡Borra todas las tablas, tipos y datos de psicometria_db!
--  Úsalo cuando cambie el modelo y quieras volver a ejecutar 02 y 03.
--  Ejecutar conectado a "psicometria_db".
-- =====================================================================

BEGIN;

DROP TABLE IF EXISTS aplicaciones_test, tokens_acceso,
                     preguntas, tests, psicologos, admins, evaluados CASCADE;

DROP FUNCTION IF EXISTS set_actualizado_at();

DROP TYPE IF EXISTS estado_aplicacion, tipo_pregunta, visibilidad_test, categoria_test,
                    especialidad_psicologo, rol_admin, nivel_educativo, genero_evaluado;

-- Restos de versiones anteriores del modelo
DROP TABLE IF EXISTS instituciones CASCADE;
DROP TYPE IF EXISTS tipo_institucion;

COMMIT;
