-- =====================================================================
--  01 - Crear la base de datos
--
--  Ejecutar conectado a la base "postgres" (pgAdmin: clic derecho en
--  la base postgres > Query Tool). CREATE DATABASE no puede ejecutarse
--  dentro de una transacción, por eso va en un archivo separado.
-- =====================================================================

CREATE DATABASE psicometria_db
  WITH ENCODING = 'UTF8'
       TEMPLATE = template0;

COMMENT ON DATABASE psicometria_db IS 'Plataforma de tests psicométricos para estudiantes';
