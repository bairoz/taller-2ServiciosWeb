package com.psicometria.api.models;


/**
 * Corresponde al tipo enumerado de PostgreSQL:
 * CREATE TYPE categoria_test AS ENUM
 *   ('PERSONALIDAD', 'APTITUD', 'INTELIGENCIA', 'VOCACIONAL', 'EMOCIONAL');
 *
 * Los nombres deben coincidir exactamente con los valores del tipo en la base.
 */
public enum CategoriaTest {
    PERSONALIDAD,
    APTITUD,
    INTELIGENCIA,
    VOCACIONAL,
    EMOCIONAL
}