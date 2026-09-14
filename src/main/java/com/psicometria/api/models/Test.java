package com.psicometria.api.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.generator.EventType;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Entidad JPA de la tabla "tests".
 *
 * La aplicacion corre con spring.jpa.hibernate.ddl-auto=validate, asi que
 * cada campo de esta clase debe coincidir con su columna en 02_esquema.sql.
 *
 * psicologo_id se mapea como columna simple (Long) y no como @ManyToOne para
 * no depender de la entidad Psicologo, que se desarrolla en otra rama.
 */
@Entity
@Table(name = "tests")
public class Test {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** FK hacia psicologos(id). Admite NULL: ON DELETE SET NULL. */
    @Column(name = "psicologo_id")
    private Long psicologoId;

    @Column(name = "titulo", nullable = false, length = 255, unique = true)
    private String titulo;

    @Column(name = "descripcion", columnDefinition = "text")
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "categoria", nullable = false, columnDefinition = "categoria_test")
    private CategoriaTest categoria;

    /** NULL significa que el test no tiene limite de tiempo. */
    @Column(name = "duracion_minutos")
    private Integer duracionMinutos;

    @Column(name = "puntaje_aprobacion", nullable = false, precision = 5, scale = 2)
    private BigDecimal puntajeAprobacion;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "visibilidad", nullable = false, columnDefinition = "visibilidad_test")
    private VisibilidadTest visibilidad;

    /** Lo asigna la base con DEFAULT now(); Hibernate solo lo lee. */
    @Generated(event = EventType.INSERT)
    @Column(name = "creado_at", insertable = false, updatable = false)
    private OffsetDateTime creadoAt;

    /** Lo mantiene el trigger de actualizado_at; Hibernate solo lo lee. */
    @Generated(event = {EventType.INSERT, EventType.UPDATE})
    @Column(name = "actualizado_at", insertable = false, updatable = false)
    private OffsetDateTime actualizadoAt;

    public Test() {
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

    public OffsetDateTime getActualizadoAt() {
        return actualizadoAt;
    }
}