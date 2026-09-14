package com.psicometria.api.controllers;

import com.psicometria.api.dto.EvaluadoDTO;
import com.psicometria.api.exceptions.DuplicateResourceException;
import com.psicometria.api.exceptions.ResourceNotFoundException;
import com.psicometria.api.models.Genero;
import com.psicometria.api.models.NivelEducativo;
import com.psicometria.api.services.EvaluadoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EvaluadoController.class)
class EvaluadoControllerTest {

    private static final String URL = "/api/evaluados";

    private static final String EVALUADO_VALIDO = """
            {
              "nombre": "Camila",
              "apellido": "Rojas",
              "email": "camila.rojas@correo.cl",
              "fechaNacimiento": "2005-03-21",
              "genero": "FEMENINO",
              "nivelEducativo": "UNIVERSITARIO",
              "activo": true
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EvaluadoService evaluadoService;

    private static EvaluadoDTO evaluadoGuardado(Long id) {
        OffsetDateTime ahora = OffsetDateTime.now();
        return new EvaluadoDTO(id, "Camila", "Rojas", "camila.rojas@correo.cl", LocalDate.of(2005, 3, 21),
                Genero.FEMENINO, NivelEducativo.UNIVERSITARIO, true, ahora, ahora);
    }

    @Test
    void crearEvaluadoValidoRetorna201() throws Exception {
        when(evaluadoService.crear(any())).thenReturn(evaluadoGuardado(1L));

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

        verifyNoInteractions(evaluadoService);
    }

    @Test
    void crearConEnumInvalidoRetorna400() throws Exception {
        String json = EVALUADO_VALIDO.replace("FEMENINO", "OTRO");
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void crearConEmailDuplicadoRetorna409() throws Exception {
        when(evaluadoService.crear(any()))
                .thenThrow(new DuplicateResourceException("Ya existe un evaluado con el email camila.rojas@correo.cl"));

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(EVALUADO_VALIDO))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Ya existe un evaluado con el email camila.rojas@correo.cl"));
    }

    @Test
    void listarEvaluados() throws Exception {
        when(evaluadoService.listar()).thenReturn(List.of(evaluadoGuardado(1L), evaluadoGuardado(2L)));

        mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void obtenerPorId() throws Exception {
        when(evaluadoService.obtenerPorId(1L)).thenReturn(evaluadoGuardado(1L));

        mockMvc.perform(get(URL + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Camila"));
    }

    @Test
    void obtenerInexistenteRetorna404() throws Exception {
        when(evaluadoService.obtenerPorId(99L)).thenThrow(new ResourceNotFoundException("Evaluado", 99L));

        mockMvc.perform(get(URL + "/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Evaluado con id 99 no encontrado"));
    }

    @Test
    void obtenerConIdNoNumericoRetorna400() throws Exception {
        mockMvc.perform(get(URL + "/abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void actualizarEvaluado() throws Exception {
        when(evaluadoService.actualizar(eq(1L), any())).thenReturn(evaluadoGuardado(1L));

        mockMvc.perform(put(URL + "/1").contentType(MediaType.APPLICATION_JSON).content(EVALUADO_VALIDO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void actualizarConDatosInvalidosRetorna400() throws Exception {
        String invalido = EVALUADO_VALIDO.replace("camila.rojas@correo.cl", "correo-malo");

        mockMvc.perform(put(URL + "/1").contentType(MediaType.APPLICATION_JSON).content(invalido))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.email").exists());

        verifyNoInteractions(evaluadoService);
    }

    @Test
    void eliminarEvaluado() throws Exception {
        mockMvc.perform(delete(URL + "/1")).andExpect(status().isNoContent());
        verify(evaluadoService).eliminar(1L);
    }

    @Test
    void eliminarInexistenteRetorna404() throws Exception {
        doThrow(new ResourceNotFoundException("Evaluado", 99L)).when(evaluadoService).eliminar(99L);

        mockMvc.perform(delete(URL + "/99")).andExpect(status().isNotFound());
    }
}
