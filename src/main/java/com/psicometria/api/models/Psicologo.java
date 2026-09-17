package com.psicometria.api.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * Entidad JPA de la tabla {@code psicologos}.
 */
@Entity
@Table(name = "psicologos")
public class Psicologo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String apellido;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "numero_registro", nullable = false, unique = true, length = 30)
    private String numeroRegistro;

    @Column(name = "fecha_titulacion", nullable = false)
    private LocalDate fechaTitulacion;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "especialidad_psicologo")
    private EspecialidadPsicologo especialidad;

    @Column(name = "anios_experiencia", nullable = false)
    private Integer aniosExperiencia;

    @Column(nullable = false)
    private Boolean disponible;

    @CreationTimestamp
    @Column(name = "creado_at", nullable = false, updatable = false)
    private OffsetDateTime creadoAt;

    @UpdateTimestamp
    @Column(name = "actualizado_at", nullable = false)
    private OffsetDateTime actualizadoAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDate getFechaTitulacion() {
        return fechaTitulacion;
    }

    public void setFechaTitulacion(LocalDate fechaTitulacion) {
        this.fechaTitulacion = fechaTitulacion;
    }

    public EspecialidadPsicologo getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(EspecialidadPsicologo especialidad) {
        this.especialidad = especialidad;
    }

    public Integer getAniosExperiencia() {
        return aniosExperiencia;
    }

    public void setAniosExperiencia(Integer aniosExperiencia) {
        this.aniosExperiencia = aniosExperiencia;
    }

    public Boolean getDisponible() {
        return disponible;
    }

    public void setDisponible(Boolean disponible) {
        this.disponible = disponible;
    }

    public String getNumeroRegistro() { return numeroRegistro; }

    public void setNumeroRegistro(String numeroRegistro) { this.numeroRegistro = numeroRegistro; }

    public OffsetDateTime getCreadoAt() {
        return creadoAt;
    }

    public OffsetDateTime getActualizadoAt() {
        return actualizadoAt;
    }
}
