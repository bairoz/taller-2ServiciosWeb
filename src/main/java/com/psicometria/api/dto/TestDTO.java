package com.psicometria.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.psicometria.api.models.CategoriaTest;
import com.psicometria.api.models.VisibilidadTest;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * DTO de entrada y salida de la entidad Test.
 *
 * Cada validacion refleja una restriccion real de la tabla "tests":
 *  - titulo             VARCHAR(255) NOT NULL UNIQUE
 *  - categoria          categoria_test NOT NULL
 *  - duracion_minutos   INT CHECK (... > 0), admite NULL = sin limite
 *  - puntaje_aprobacion NUMERIC(5,2) NOT NULL CHECK (BETWEEN 0 AND 100)
 *  - visibilidad        visibilidad_test NOT NULL
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TestDTO {

    /** Lo genera la base de datos; en POST y PUT se ignora. */
    private Long id;

    @Positive(message = "El identificador del psicologo debe ser un numero mayor que cero")
    private Long psicologoId;

    @NotBlank(message = "El titulo es obligatorio")
    @Size(min = 5, max = 255, message = "El titulo debe tener entre 5 y 255 caracteres")
    private String titulo;

    @Size(max = 2000, message = "La descripcion no debe superar los 2000 caracteres")
    private String descripcion;

    @NotNull(message = "La categoria es obligatoria (PERSONALIDAD, APTITUD, INTELIGENCIA, VOCACIONAL o EMOCIONAL)")
    private CategoriaTest categoria;

    @Positive(message = "La duracion debe ser mayor que cero minutos")
    @Max(value = 480, message = "La duracion no puede superar los 480 minutos")
    private Integer duracionMinutos;

    @NotNull(message = "El puntaje de aprobacion es obligatorio")
    @DecimalMin(value = "0.00", message = "El puntaje de aprobacion no puede ser negativo")
    @DecimalMax(value = "100.00", message = "El puntaje de aprobacion no puede ser mayor que 100")
    @Digits(integer = 3, fraction = 2, message = "El puntaje admite hasta 3 enteros y 2 decimales")
    private BigDecimal puntajeAprobacion;

    @NotNull(message = "La visibilidad es obligatoria (BORRADOR, PRIVADO o PUBLICO)")
    private VisibilidadTest visibilidad;

    /** Solo lectura: lo administran la base y el trigger. */
    private OffsetDateTime creadoAt;

    private OffsetDateTime actualizadoAt;

    public TestDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPsicologoId() {
        return psicologoId;
    }

    public void setPsicologoId(Long psicologoId) {
        this.psicologoId = psicologoId;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public CategoriaTest getCategoria() {
        return categoria;
    }

    public void setCategoria(CategoriaTest categoria) {
        this.categoria = categoria;
    }

    public Integer getDuracionMinutos() {
        return duracionMinutos;
    }

    public void setDuracionMinutos(Integer duracionMinutos) {
        this.duracionMinutos = duracionMinutos;
    }

    public BigDecimal getPuntajeAprobacion() {
        return puntajeAprobacion;
    }

    public void setPuntajeAprobacion(BigDecimal puntajeAprobacion) {
        this.puntajeAprobacion = puntajeAprobacion;
    }

    public VisibilidadTest getVisibilidad() {
        return visibilidad;
    }

    public void setVisibilidad(VisibilidadTest visibilidad) {
        this.visibilidad = visibilidad;
    }

    public OffsetDateTime getCreadoAt() {
        return creadoAt;
    }

    public void setCreadoAt(OffsetDateTime creadoAt) {
        this.creadoAt = creadoAt;
    }

    public OffsetDateTime getActualizadoAt() {
        return actualizadoAt;
    }

    public void setActualizadoAt(OffsetDateTime actualizadoAt) {
        this.actualizadoAt = actualizadoAt;
    }
}