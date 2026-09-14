package com.psicometria.api.dto;


/**
 * Respuesta JSON usada por el endpoint DELETE.
 */
public class RespuestaSimple {

    private int estado;
    private String mensaje;
    private Long id;

    public RespuestaSimple() {
    }

    public RespuestaSimple(int estado, String mensaje, Long id) {
        this.estado = estado;
        this.mensaje = mensaje;
        this.id = id;
    }

    public int getEstado() {
        return estado;
    }

    public void setEstado(int estado) {
        this.estado = estado;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
