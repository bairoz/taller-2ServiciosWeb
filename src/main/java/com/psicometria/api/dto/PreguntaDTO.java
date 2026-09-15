package com.psicometria.api.dto;

import com.psicometria.api.models.enums.TipoPregunta;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

public record PreguntaDTO(

        Long id,

        @NotNull(message = "El id del test es obligatorio")
        Long testId,

        @NotBlank(message = "El enunciado es obligatorio")
        @Size(min = 5, max = 2000, message = "El enunciado debe tener entre 5 y 2000 caracteres")
        String enunciado,

        @NotNull(message = "El tipo de pregunta es obligatorio")
        TipoPregunta tipo,

        List<String> opciones,

        String respuestaCorrecta,

        @NotNull(message = "El puntaje es obligatorio")
        @DecimalMin(value = "0.0", message = "El puntaje no puede ser negativo")
        BigDecimal puntaje,

        @NotNull(message = "El orden es obligatorio")
        @Min(value = 1, message = "El orden debe ser mayor o igual a 1")
        Integer orden,

        Boolean obligatoria
) {}