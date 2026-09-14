package com.psicometria.api.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class EvaluadoControllerTest {

    private static final String URL = "/api/evaluados";

    private static final String EVALUADO_VALIDO = """
            {
              "nombre": "Camila",
              "apellido": "Rojas",
              "email": "Camila.Rojas@correo.cl",
              "fechaNacimiento": "2005-03-21",
              "genero": "FEMENINO",
              "nivelEducativo": "UNIVERSITARIO",
              "activo": true
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    private long crearEvaluado(String json) throws Exception {
        MvcResult result = mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andReturn();
        String location = result.getResponse().getHeader("Location");
        return Long.parseLong(location.substring(location.lastIndexOf('/') + 1));
    }

    @Test
    void crearEvaluadoValidoRetorna201() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(EVALUADO_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/evaluados/1")))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("camila.rojas@correo.cl"))
                .andExpect(jsonPath("$.genero").value("FEMENINO"))
                .andExpect(jsonPath("$.activo").value(true))
                .andExpect(jsonPath("$.creadoAt").exists());
    }

    @Test
    void crearEvaluadoInvalidoRetorna400ConErroresPorCampo() throws Exception {
        String invalido = """
                {
                  "nombre": "",
                  "apellido": "R",
                  "email": "no-es-email",
                  "fechaNacimiento": "2999-01-01",
                  "activo": null
                }
                """;
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(invalido))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Error de validación"))
                .andExpect(jsonPath("$.errores.nombre").exists())
                .andExpect(jsonPath("$.errores.apellido").exists())
                .andExpect(jsonPath("$.errores.email").value("El email debe tener un formato válido"))
                .andExpect(jsonPath("$.errores.fechaNacimiento").value("La fecha de nacimiento debe ser una fecha pasada"))
                .andExpect(jsonPath("$.errores.genero").value("El género es obligatorio"))
                .andExpect(jsonPath("$.errores.nivelEducativo").value("El nivel educativo es obligatorio"))
                .andExpect(jsonPath("$.errores.activo").value("El campo activo es obligatorio"));
    }

    @Test
    void crearConEnumInvalidoRetorna400() throws Exception {
        String json = EVALUADO_VALIDO.replace("FEMENINO", "OTRO");
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void crearConEmailDuplicadoRetorna409() throws Exception {
        crearEvaluado(EVALUADO_VALIDO);
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(EVALUADO_VALIDO))
                .andExpect(status().isConflict());
    }

    @Test
    void listarYObtenerPorId() throws Exception {
        long id = crearEvaluado(EVALUADO_VALIDO);

        mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        mockMvc.perform(get(URL + "/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Camila"));
    }

    @Test
    void obtenerInexistenteRetorna404() throws Exception {
        mockMvc.perform(get(URL + "/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Evaluado con id 99 no encontrado"));
    }

    @Test
    void actualizarEvaluado() throws Exception {
        long id = crearEvaluado(EVALUADO_VALIDO);
        String cambios = EVALUADO_VALIDO.replace("UNIVERSITARIO", "POSTGRADO").replace("true", "false");

        mockMvc.perform(put(URL + "/" + id).contentType(MediaType.APPLICATION_JSON).content(cambios))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.nivelEducativo").value("POSTGRADO"))
                .andExpect(jsonPath("$.activo").value(false));
    }

    @Test
    void actualizarConDatosInvalidosRetorna400() throws Exception {
        long id = crearEvaluado(EVALUADO_VALIDO);
        String invalido = EVALUADO_VALIDO.replace("Camila.Rojas@correo.cl", "correo-malo");

        mockMvc.perform(put(URL + "/" + id).contentType(MediaType.APPLICATION_JSON).content(invalido))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.email").exists());
    }

    @Test
    void eliminarEvaluado() throws Exception {
        long id = crearEvaluado(EVALUADO_VALIDO);

        mockMvc.perform(delete(URL + "/" + id)).andExpect(status().isNoContent());
        mockMvc.perform(get(URL + "/" + id)).andExpect(status().isNotFound());
        mockMvc.perform(delete(URL + "/" + id)).andExpect(status().isNotFound());
    }
}
