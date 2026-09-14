package com.psicometria.api.dto;


import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Objeto de transferencia de datos de la entidad Test.
 * Se usa para recibir informacion (POST / PUT) y para devolverla en JSON.
 *
 * Atributos: id + psicologoId, titulo, descripcion, categoria,
 * duracionMinutos, puntajeAprobacion y visibilidad.
 */
public class TestDTO {

    /** Identificador. Lo asigna el servicio, por eso no se valida. */
    private Long id;

    @NotNull(message = "El identificador del psicologo es obligatorio")
    @Positive(message = "El identificador del psicologo debe ser un numero mayor que cero")
    private Long psicologoId;

    @NotBlank(message = "El titulo es obligatorio")
    @Size(min = 5, max = 120, message = "El titulo debe tener entre 5 y 120 caracteres")
    private String titulo;

    @NotBlank(message = "La descripcion es obligatoria")
    @Size(min = 10, max = 500, message = "La descripcion debe tener entre 10 y 500 caracteres")
    private String descripcion;

    @NotNull(message = "La categoria es obligatoria (ANSIEDAD, DEPRESION, ESTRES, AUTOESTIMA, PERSONALIDAD o HABILIDADES_SOCIALES)")
    private CategoriaTest categoria;

    @NotNull(message = "La duracion en minutos es obligatoria")
    @Min(value = 5, message = "La duracion minima es de 5 minutos")
    @Max(value = 240, message = "La duracion maxima es de 240 minutos")
    private Integer duracionMinutos;

    @NotNull(message = "El puntaje de aprobacion es obligatorio")
    @DecimalMin(value = "0.01", message = "El puntaje de aprobacion debe ser mayor que cero")
    @DecimalMax(value = "100.00", message = "El puntaje de aprobacion no puede ser mayor que 100")
    private BigDecimal puntajeAprobacion;

    @NotNull(message = "La visibilidad es obligatoria (PUBLICO o PRIVADO)")
    private VisibilidadTest visibilidad;

    public TestDTO() {
    }

    public TestDTO(Long id, Long psicologoId, String titulo, String descripcion,
                   CategoriaTest categoria, Integer duracionMinutos,
                   BigDecimal puntajeAprobacion, VisibilidadTest visibilidad) {
        this.id = id;
        this.psicologoId = psicologoId;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.categoria = categoria;
        this.duracionMinutos = duracionMinutos;
        this.puntajeAprobacion = puntajeAprobacion;
        this.visibilidad = visibilidad;
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
}