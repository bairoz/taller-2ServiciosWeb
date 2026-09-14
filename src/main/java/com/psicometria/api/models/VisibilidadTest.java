package com.psicometria.api.models;


/**
 * Indica quien puede ver el test.
 * PUBLICO: visible para cualquier paciente.
 * PRIVADO: solo lo ven los pacientes asignados por el psicologo.
 */
public enum VisibilidadTest {
    BORRADOR,
    PUBLICO,
    PRIVADO
}