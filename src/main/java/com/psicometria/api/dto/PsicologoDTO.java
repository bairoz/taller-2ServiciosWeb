package com.psicometria.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.psicometria.api.models.EspecialidadPsicologo;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record PsicologoDTO(
        @JsonProperty(access = JsonProperty.Access.READ_ONLY) Long id,

        @NotBlank(message = "El nombre es obligatorio")
        @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
        String nombre,

        @NotBlank(message = "El apellido es obligatorio")
        @Size(min = 2, max = 100, message = "El apellido debe tener entre 2 y 100 caracteres")
        String apellido,

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email debe tener un formato válido")
        @Size(max = 255, message = "El email no puede superar los 255 caracteres")
        String email,

        @NotBlank(message = "El número de registro es obligatorio")
        @Size(max = 30, message = "El número de registro no puede superar los 30 caracteres")
        String numeroRegistro,

        @NotNull(message = "La especialidad es obligatoria")
        EspecialidadPsicologo especialidad,

        @NotNull(message = "Los años de experiencia son obligatorios")
        @Min(value = 0, message = "Los años de experiencia no pueden ser negativos")
        @Max(value = 60, message = "Los años de experiencia no pueden superar 60")
        Integer aniosExperiencia,

        @NotNull(message = "La fecha de titulación es obligatoria")
        @PastOrPresent(message = "La fecha de titulación no puede ser futura")
        LocalDate fechaTitulacion,

        @NotNull(message = "El campo disponible es obligatorio")
        Boolean disponible,

        @JsonProperty(access = JsonProperty.Access.READ_ONLY) OffsetDateTime creadoAt,
        @JsonProperty(access = JsonProperty.Access.READ_ONLY) OffsetDateTime actualizadoAt
) {
}
