package com.psicometria.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.psicometria.api.models.RolAdmin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

/**
 * Entrada y salida del CRUD de admins.
 * El servidor asigna el id y las fechas de auditoría. La contraseña solo se recibe.
 */
public record AdminDTO(

        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        Long id,

        @NotBlank(message = "El nombre es obligatorio")
        @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
        @Pattern(regexp = "^[\\p{L} '-]+$", message = "El nombre solo puede contener letras, espacios, apóstrofes o guiones")
        String nombre,

        @NotBlank(message = "El apellido es obligatorio")
        @Size(min = 2, max = 100, message = "El apellido debe tener entre 2 y 100 caracteres")
        @Pattern(regexp = "^[\\p{L} '-]+$", message = "El apellido solo puede contener letras, espacios, apóstrofes o guiones")
        String apellido,

        @NotBlank(message = "El usuario es obligatorio")
        @Size(max = 50, message = "El usuario no puede superar los 50 caracteres")
        String usuario,

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email debe tener un formato válido")
        @Size(max = 255, message = "El email no puede superar los 255 caracteres")
        String email,

        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8, max = 100, message = "La contraseña debe tener entre 8 y 100 caracteres")
        String password,

        @NotNull(message = "El rol es obligatorio")
        RolAdmin rol,

        @NotNull(message = "El campo activo es obligatorio")
        Boolean activo,

        @NotNull(message = "Los intentos fallidos son obligatorios")
        @Min(value = 0, message = "Los intentos fallidos no pueden ser negativos")
        Integer intentosFallidos,

        @PastOrPresent(message = "El último acceso no puede ser una fecha futura")
        OffsetDateTime ultimoAcceso,

        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        OffsetDateTime creadoAt,

        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        OffsetDateTime actualizadoAt
) {
}
