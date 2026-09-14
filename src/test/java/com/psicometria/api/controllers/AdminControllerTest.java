package com.psicometria.api.controllers;

import com.psicometria.api.dto.AdminDTO;
import com.psicometria.api.exceptions.DuplicateResourceException;
import com.psicometria.api.exceptions.ResourceNotFoundException;
import com.psicometria.api.models.RolAdmin;
import com.psicometria.api.services.AdminService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
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

@WebMvcTest(AdminController.class)
class AdminControllerTest {

    private static final String URL = "/api/admins";
    private static final String ADMIN_VALIDO = """
            {
              "nombre": "Ana",
              "apellido": "Pérez",
              "usuario": "admin",
              "email": "admin@example.com",
              "password": "Clave123",
              "rol": "ADMIN",
              "activo": true,
              "intentosFallidos": 0,
              "ultimoAcceso": "2025-01-15T10:30:00-06:00"
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminService adminService;

    private static AdminDTO guardado(Long id) {
        OffsetDateTime fecha = OffsetDateTime.parse("2025-01-15T10:30:00-06:00");
        // Incluso si el service devolviera una contraseña, el JSON debe excluirla.
        return new AdminDTO(id, "Ana", "Pérez", "admin", "admin@example.com", "Clave123",
                RolAdmin.ADMIN, true, 0, fecha, fecha, fecha);
    }

    @Test
    void crearRetorna201LocationYDatosSinPassword() throws Exception {
        when(adminService.crear(any())).thenReturn(guardado(1L));

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(ADMIN_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/admins/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.usuario").value("admin"))
                .andExpect(jsonPath("$.rol").value("ADMIN"))
                .andExpect(jsonPath("$.intentosFallidos").value(0))
                .andExpect(jsonPath("$.creadoAt").exists())
                .andExpect(jsonPath("$.actualizadoAt").exists())
                .andExpect(jsonPath("$.password").doesNotExist());

        ArgumentCaptor<AdminDTO> captor = ArgumentCaptor.forClass(AdminDTO.class);
        verify(adminService).crear(captor.capture());
        assertThat(captor.getValue().password()).isEqualTo("Clave123");
        assertThat(captor.getValue().ultimoAcceso().toInstant())
                .isEqualTo(OffsetDateTime.parse("2025-01-15T10:30:00-06:00").toInstant());
    }

    @Test
    void ignoraIdYAuditoriaEnviadosPorElCliente() throws Exception {
        when(adminService.crear(any())).thenReturn(guardado(1L));
        String json = ADMIN_VALIDO.replace("{", """
                {"id": 999, "creadoAt": "2000-01-01T00:00:00Z", "actualizadoAt": "2000-01-01T00:00:00Z",
                """);

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.id").value(1));

        ArgumentCaptor<AdminDTO> captor = ArgumentCaptor.forClass(AdminDTO.class);
        verify(adminService).crear(captor.capture());
        assertThat(captor.getValue().id()).isNull();
        assertThat(captor.getValue().creadoAt()).isNull();
        assertThat(captor.getValue().actualizadoAt()).isNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {"POST", "PUT"})
    void rechazaCamposObligatoriosAusentes(String metodo) throws Exception {
        MockHttpServletRequestBuilder peticion = metodo.equals("POST") ? post(URL) : put(URL + "/1");
        mockMvc.perform(peticion.contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Error de validación"))
                .andExpect(jsonPath("$.errores.nombre").exists())
                .andExpect(jsonPath("$.errores.apellido").exists())
                .andExpect(jsonPath("$.errores.usuario").exists())
                .andExpect(jsonPath("$.errores.email").exists())
                .andExpect(jsonPath("$.errores.password").exists())
                .andExpect(jsonPath("$.errores.rol").exists())
                .andExpect(jsonPath("$.errores.activo").exists())
                .andExpect(jsonPath("$.errores.intentosFallidos").exists());
        verifyNoInteractions(adminService);
    }

    @ParameterizedTest
    @CsvSource({
            "nombre, Ana, A",
            "nombre, Ana, Ana123",
            "apellido, Pérez, P",
            "apellido, Pérez, Pérez123",
            "usuario, admin, '   '",
            "email, admin@example.com, correo-invalido",
            "password, Clave123, corta",
            "password, Clave123, '        '",
            "ultimoAcceso, 2025-01-15T10:30:00-06:00, 2999-01-01T00:00:00Z"
    })
    void rechazaDatosInvalidosConErrorDelCampo(String campo, String original, String invalido) throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                        .content(ADMIN_VALIDO.replace(original, invalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores." + campo).exists());
        verifyNoInteractions(adminService);
    }

    @ParameterizedTest
    @CsvSource({"nombre,Ana,100", "apellido,Pérez,100", "usuario,admin,50",
            "email,admin@example.com,255", "password,Clave123,100"})
    void rechazaLongitudesMayoresAlEsquema(String campo, String original, int maximo) throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                        .content(ADMIN_VALIDO.replace(original, "a".repeat(maximo + 1))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores." + campo).exists());
        verifyNoInteractions(adminService);
    }

    @Test
    void rechazaIntentosNegativos() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                        .content(ADMIN_VALIDO.replace("\"intentosFallidos\": 0", "\"intentosFallidos\": -1")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.intentosFallidos").exists());
        verifyNoInteractions(adminService);
    }

    @ParameterizedTest
    @ValueSource(strings = {"SUPER_ADMIN", "ADMIN", "LECTOR"})
    void aceptaTodosLosRoles(String rol) throws Exception {
        when(adminService.crear(any())).thenReturn(guardado(1L));
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                        .content(ADMIN_VALIDO.replace("\"ADMIN\"", "\"" + rol + "\"")))
                .andExpect(status().isCreated());
        ArgumentCaptor<AdminDTO> captor = ArgumentCaptor.forClass(AdminDTO.class);
        verify(adminService).crear(captor.capture());
        assertThat(captor.getValue().rol()).isEqualTo(RolAdmin.valueOf(rol));
    }

    @Test
    void aceptaUltimoAccesoNuloYActivoFalse() throws Exception {
        when(adminService.crear(any())).thenReturn(guardado(1L));
        String json = ADMIN_VALIDO.replace("\"2025-01-15T10:30:00-06:00\"", "null")
                .replace("\"activo\": true", "\"activo\": false");
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated());
        ArgumentCaptor<AdminDTO> captor = ArgumentCaptor.forClass(AdminDTO.class);
        verify(adminService).crear(captor.capture());
        assertThat(captor.getValue().ultimoAcceso()).isNull();
        assertThat(captor.getValue().activo()).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"{", "{\"rol\":\"OTRO\"}", "{\"ultimoAcceso\":\"fecha-invalida\"}"})
    void rechazaJsonEnumOFechaMalFormados(String json) throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(adminService);
    }

    @ParameterizedTest
    @ValueSource(strings = {"usuario admin", "email admin@example.com"})
    void crearDuplicadoRetorna409(String duplicado) throws Exception {
        when(adminService.crear(any())).thenThrow(new DuplicateResourceException("Ya existe un admin con el " + duplicado));
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(ADMIN_VALIDO))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Ya existe un admin con el " + duplicado));
    }

    @Test
    void restriccionDeBaseDeDatosRetorna409SinExponerDetalles() throws Exception {
        when(adminService.crear(any())).thenThrow(new DataIntegrityViolationException("detalle interno"));
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(ADMIN_VALIDO))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(
                        "La operación viola una restricción de la base de datos (dato duplicado o referencia inválida)"));
    }

    @Test
    void listarRetorna200SinPasswords() throws Exception {
        when(adminService.listar()).thenReturn(List.of(guardado(1L), guardado(2L)));
        mockMvc.perform(get(URL)).andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].password").doesNotExist())
                .andExpect(jsonPath("$[1].password").doesNotExist());
    }

    @Test
    void listarSinRegistrosRetornaListaVacia() throws Exception {
        when(adminService.listar()).thenReturn(List.of());
        mockMvc.perform(get(URL)).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void obtenerRetorna200SinPassword() throws Exception {
        when(adminService.obtenerPorId(1L)).thenReturn(guardado(1L));
        mockMvc.perform(get(URL + "/1")).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void obtenerInexistenteRetorna404() throws Exception {
        when(adminService.obtenerPorId(99L)).thenThrow(new ResourceNotFoundException("Admin", 99L));
        mockMvc.perform(get(URL + "/99")).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Admin con id 99 no encontrado"));
    }

    @Test
    void idNoNumericoRetorna400() throws Exception {
        mockMvc.perform(get(URL + "/abc")).andExpect(status().isBadRequest());
        verifyNoInteractions(adminService);
    }

    @Test
    void actualizarRetorna200SinPassword() throws Exception {
        when(adminService.actualizar(eq(1L), any())).thenReturn(guardado(1L));
        mockMvc.perform(put(URL + "/1").contentType(MediaType.APPLICATION_JSON).content(ADMIN_VALIDO))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void actualizarInexistenteRetorna404() throws Exception {
        when(adminService.actualizar(eq(99L), any())).thenThrow(new ResourceNotFoundException("Admin", 99L));
        mockMvc.perform(put(URL + "/99").contentType(MediaType.APPLICATION_JSON).content(ADMIN_VALIDO))
                .andExpect(status().isNotFound());
    }

    @Test
    void actualizarDuplicadoRetorna409() throws Exception {
        when(adminService.actualizar(eq(1L), any())).thenThrow(new DuplicateResourceException("Usuario duplicado"));
        mockMvc.perform(put(URL + "/1").contentType(MediaType.APPLICATION_JSON).content(ADMIN_VALIDO))
                .andExpect(status().isConflict());
    }

    @Test
    void eliminarRetorna204() throws Exception {
        mockMvc.perform(delete(URL + "/1")).andExpect(status().isNoContent());
        verify(adminService).eliminar(1L);
    }

    @Test
    void eliminarInexistenteRetorna404() throws Exception {
        doThrow(new ResourceNotFoundException("Admin", 99L)).when(adminService).eliminar(99L);
        mockMvc.perform(delete(URL + "/99")).andExpect(status().isNotFound());
    }
}
