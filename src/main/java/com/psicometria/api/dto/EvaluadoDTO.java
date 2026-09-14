package com.psicometria.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO de la entidad evaluados (tabla {@code evaluados}).
 * El id y las fechas de auditoría los asigna el servidor y se ignoran si vienen en la petición.
 */
public record EvaluadoDTO(

        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        Long id,

        @Positive(message = "El id de la institución debe ser un número positivo")
        Long institucionId,

        @NotBlank(message = "El nombre es obligatorio")
        @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
        @Pattern(regexp = "^[\\p{L} '-]+$", message = "El nombre solo puede contener letras, espacios, apóstrofes o guiones")
        String nombre,

        @NotBlank(message = "El apellido es obligatorio")
        @Size(min = 2, max = 100, message = "El apellido debe tener entre 2 y 100 caracteres")
        @Pattern(regexp = "^[\\p{L} '-]+$", message = "El apellido solo puede contener letras, espacios, apóstrofes o guiones")
        String apellido,

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email debe tener un formato válido")
        @Size(max = 255, message = "El email no puede superar los 255 caracteres")
        String email,

        @NotNull(message = "La fecha de nacimiento es obligatoria")
        @Past(message = "La fecha de nacimiento debe ser una fecha pasada")
        LocalDate fechaNacimiento,

        @NotNull(message = "El género es obligatorio")
        Genero genero,

        @NotNull(message = "El nivel educativo es obligatorio")
        NivelEducativo nivelEducativo,

        @NotNull(message = "El campo activo es obligatorio")
        Boolean activo,

        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        LocalDateTime creadoAt,

        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        LocalDateTime actualizadoAt
) {

    public EvaluadoDTO withAuditoria(Long id, LocalDateTime creadoAt, LocalDateTime actualizadoAt) {
        return new EvaluadoDTO(id, institucionId, nombre.trim(), apellido.trim(), email.trim().toLowerCase(), fechaNacimiento,
                genero, nivelEducativo, activo, creadoAt, actualizadoAt);
    }
}
