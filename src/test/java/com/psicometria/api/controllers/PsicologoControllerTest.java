package com.psicometria.api.controllers;

import com.psicometria.api.dto.PsicologoDTO;
import com.psicometria.api.exceptions.DuplicateResourceException;
import com.psicometria.api.exceptions.ResourceNotFoundException;
import com.psicometria.api.models.EspecialidadPsicologo;

import com.psicometria.api.services.PsicologoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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

@WebMvcTest(PsicologoController.class)
class PsicologoControllerTest {

    private static final String URL = "/api/psicologos";

    private static final String PSICOLOGO_VALIDO = """
            {
              "nombre": "Camila",
              "apellido": "Rojas",
              "email": "camila.rojas@correo.cl",
              "numeroRegistro": "PSI-TEST-01",
              "fechaTitulacion": "2005-03-21",
              "especialidad": "CLINICA",
              "aniosExperiencia": 5,
              "disponible": true
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PsicologoService psicologoService;

    private static PsicologoDTO psicologoGuardado(Long id) {
        OffsetDateTime ahora = OffsetDateTime.now();
        return new PsicologoDTO(id, "Camila", "Rojas", "camila.rojas@correo.cl", "PSI-TEST-01", EspecialidadPsicologo.CLINICA, 5, LocalDate.of(2005, 3, 21), true, ahora, ahora);
    }

    @Test
    void crearPsicologoValidoRetorna201() throws Exception {
        when(psicologoService.crear(any())).thenReturn(psicologoGuardado(1L));

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(PSICOLOGO_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/psicologos/1")))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("camila.rojas@correo.cl"))
                .andExpect(jsonPath("$.especialidad").value("CLINICA"))
                .andExpect(jsonPath("$.disponible").value(true))
                .andExpect(jsonPath("$.creadoAt").exists());
    }

    @Test
    void crearPsicologoInvalidoRetorna400ConErroresPorCampo() throws Exception {
        String invalido = """
                {
                  "nombre": "",
                  "apellido": "R",
                  "email": "no-es-email",
                  "fechaTitulacion": "2999-01-01",
                  "disponible": null
                }
                """;
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(invalido))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Error de validación"))
                .andExpect(jsonPath("$.errores.nombre").exists())
                .andExpect(jsonPath("$.errores.apellido").exists())
                .andExpect(jsonPath("$.errores.email").value("El email debe tener un formato válido"))
                .andExpect(jsonPath("$.errores.fechaTitulacion").value("La fecha de titulación no puede ser futura"))
                .andExpect(jsonPath("$.errores.especialidad").value("La especialidad es obligatoria"))
                .andExpect(jsonPath("$.errores.aniosExperiencia").value("Los años de experiencia son obligatorios"))
                .andExpect(jsonPath("$.errores.disponible").value("El campo disponible es obligatorio"));

        verifyNoInteractions(psicologoService);
    }

    @Test
    void crearConEnumInvalidoRetorna400() throws Exception {
        String json = PSICOLOGO_VALIDO.replace("CLINICA", "OTRO");
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void crearConEmailDuplicadoRetorna409() throws Exception {
        when(psicologoService.crear(any()))
                .thenThrow(new DuplicateResourceException("Ya existe un psicologo con el email camila.rojas@correo.cl"));

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(PSICOLOGO_VALIDO))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Ya existe un psicologo con el email camila.rojas@correo.cl"));
    }

    @Test
    void listarPsicologos() throws Exception {
        when(psicologoService.listar()).thenReturn(List.of(psicologoGuardado(1L), psicologoGuardado(2L)));

        mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void obtenerPorId() throws Exception {
        when(psicologoService.obtenerPorId(1L)).thenReturn(psicologoGuardado(1L));

        mockMvc.perform(get(URL + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Camila"));
    }

    @Test
    void obtenerInexistenteRetorna404() throws Exception {
        when(psicologoService.obtenerPorId(99L)).thenThrow(new ResourceNotFoundException("Psicologo", 99L));

        mockMvc.perform(get(URL + "/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Psicologo con id 99 no encontrado"));
    }

    @Test
    void obtenerConIdNoNumericoRetorna400() throws Exception {
        mockMvc.perform(get(URL + "/abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void actualizarPsicologo() throws Exception {
        when(psicologoService.actualizar(eq(1L), any())).thenReturn(psicologoGuardado(1L));

        mockMvc.perform(put(URL + "/1").contentType(MediaType.APPLICATION_JSON).content(PSICOLOGO_VALIDO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void actualizarConDatosInvalidosRetorna400() throws Exception {
        String invalido = PSICOLOGO_VALIDO.replace("camila.rojas@correo.cl", "correo-malo");

        mockMvc.perform(put(URL + "/1").contentType(MediaType.APPLICATION_JSON).content(invalido))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.email").exists());

        verifyNoInteractions(psicologoService);
    }

    @Test
    void eliminarPsicologo() throws Exception {
        mockMvc.perform(delete(URL + "/1")).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.mensaje").value("Psicologo eliminado correctamente"));
        verify(psicologoService).eliminar(1L);
    }

    @Test
    void eliminarInexistenteRetorna404() throws Exception {
        doThrow(new ResourceNotFoundException("Psicologo", 99L)).when(psicologoService).eliminar(99L);

        mockMvc.perform(delete(URL + "/99")).andExpect(status().isNotFound());
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 61})
    void experienciaFueraDeRangoRetorna400(int experiencia) throws Exception {
        String json = PSICOLOGO_VALIDO.replace("\"aniosExperiencia\": 5", "\"aniosExperiencia\": " + experiencia);
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.aniosExperiencia").exists());
        verifyNoInteractions(psicologoService);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 60})
    void aceptaLimitesDeExperienciaYFechaActual(int experiencia) throws Exception {
        when(psicologoService.crear(any())).thenReturn(psicologoGuardado(1L));
        String json = PSICOLOGO_VALIDO.replace("\"aniosExperiencia\": 5", "\"aniosExperiencia\": " + experiencia)
                .replace("2005-03-21", LocalDate.now().toString());
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated());
    }

    @Test
    void registroDemasiadoLargoRetorna400() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                        .content(PSICOLOGO_VALIDO.replace("PSI-TEST-01", "R".repeat(31))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.numeroRegistro").exists());
        verifyNoInteractions(psicologoService);
    }

    @Test
    void actualizarInexistenteRetorna404() throws Exception {
        when(psicologoService.actualizar(eq(99L), any())).thenThrow(new ResourceNotFoundException("Psicologo", 99L));
        mockMvc.perform(put(URL + "/99").contentType(MediaType.APPLICATION_JSON).content(PSICOLOGO_VALIDO))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Psicologo con id 99 no encontrado"));
    }
}
