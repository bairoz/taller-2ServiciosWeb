package com.psicometria.api.models;


import java.time.LocalDateTime;

/**
 * Modelo de dominio de la entidad Test.
 *
 * Refleja la tabla "test" de la base de datos del proyecto:
 *   id, titulo, descripcion, tipo_test, duracion_minutos, activo, fecha_creacion
 *
 * Por ahora no lleva anotaciones de JPA porque la persistencia es en memoria.
 * Para conectar PostgreSQL revisen docs/GUIA-POSTGRESQL.md
 */
public class Test {

    private Long id;
    private String titulo;
    private String descripcion;
    private TipoTest tipoTest;
    private Integer duracionMinutos;
    private Boolean activo;
    private LocalDateTime fechaCreacion;

    public Test() {
    }

    public Test(Long id, String titulo, String descripcion, TipoTest tipoTest,
                Integer duracionMinutos, Boolean activo, LocalDateTime fechaCreacion) {
        this.id = id;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.tipoTest = tipoTest;
        this.duracionMinutos = duracionMinutos;
        this.activo = activo;
        this.fechaCreacion = fechaCreacion;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public TipoTest getTipoTest() {
        return tipoTest;
    }

    public void setTipoTest(TipoTest tipoTest) {
        this.tipoTest = tipoTest;
    }

    public Integer getDuracionMinutos() {
        return duracionMinutos;
    }

    public void setDuracionMinutos(Integer duracionMinutos) {
        this.duracionMinutos = duracionMinutos;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
}
