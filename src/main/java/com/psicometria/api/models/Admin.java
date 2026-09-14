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

import java.time.OffsetDateTime;

/** Entidad JPA de la tabla {@code admins}. */
@Entity
@Table(name = "admins")
public class Admin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String apellido;

    @Column(nullable = false, unique = true, length = 50)
    private String usuario;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    // Texto plano únicamente porque así lo requiere el esquema del taller.
    @Column(nullable = false, length = 100)
    private String password;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "rol_admin")
    private RolAdmin rol;

    @Column(nullable = false)
    private Boolean activo;

    @Column(name = "intentos_fallidos", nullable = false)
    private Integer intentosFallidos;

    @Column(name = "ultimo_acceso")
    private OffsetDateTime ultimoAcceso;

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

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public RolAdmin getRol() {
        return rol;
    }

    public void setRol(RolAdmin rol) {
        this.rol = rol;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public Integer getIntentosFallidos() {
        return intentosFallidos;
    }

    public void setIntentosFallidos(Integer intentosFallidos) {
        this.intentosFallidos = intentosFallidos;
    }

    public OffsetDateTime getUltimoAcceso() {
        return ultimoAcceso;
    }

    public void setUltimoAcceso(OffsetDateTime ultimoAcceso) {
        this.ultimoAcceso = ultimoAcceso;
    }

    public OffsetDateTime getCreadoAt() {
        return creadoAt;
    }

    public OffsetDateTime getActualizadoAt() {
        return actualizadoAt;
    }
}
