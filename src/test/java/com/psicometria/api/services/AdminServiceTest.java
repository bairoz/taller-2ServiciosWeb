package com.psicometria.api.services;

import com.psicometria.api.dto.AdminDTO;
import com.psicometria.api.exceptions.DuplicateResourceException;
import com.psicometria.api.exceptions.ResourceNotFoundException;
import com.psicometria.api.models.Admin;
import com.psicometria.api.models.RolAdmin;
import com.psicometria.api.repositories.AdminRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private AdminRepository adminRepository;

    @InjectMocks
    private AdminService adminService;

    private static final OffsetDateTime ACCESO = OffsetDateTime.parse("2025-01-15T10:30:00-06:00");

    private static AdminDTO dto(String usuario, String email) {
        return new AdminDTO(999L, "  Ana ", " Pérez ", usuario, email, " Clave123 ",
                RolAdmin.LECTOR, false, 2, ACCESO, ACCESO, ACCESO);
    }

    private static Admin existente() {
        Admin admin = new Admin();
        admin.setId(1L);
        admin.setUsuario("admin");
        admin.setEmail("admin@example.com");
        admin.setPassword("ClaveAnterior");
        return admin;
    }

    @Test
    void crearNormalizaDatosSinAlterarPasswordNiAceptarIdOAuditoriaDelCliente() {
        when(adminRepository.saveAndFlush(any(Admin.class))).thenAnswer(inv -> inv.getArgument(0));

        AdminDTO creado = adminService.crear(dto(" ADMIN ", " ADMIN@Example.com "));

        ArgumentCaptor<Admin> captor = ArgumentCaptor.forClass(Admin.class);
        verify(adminRepository).saveAndFlush(captor.capture());
        Admin guardado = captor.getValue();
        assertThat(guardado.getId()).isNull();
        assertThat(guardado.getCreadoAt()).isNull();
        assertThat(guardado.getActualizadoAt()).isNull();
        assertThat(guardado.getNombre()).isEqualTo("Ana");
        assertThat(guardado.getApellido()).isEqualTo("Pérez");
        assertThat(guardado.getUsuario()).isEqualTo("admin");
        assertThat(guardado.getEmail()).isEqualTo("admin@example.com");
        assertThat(guardado.getPassword()).isEqualTo(" Clave123 ");
        assertThat(guardado.getRol()).isEqualTo(RolAdmin.LECTOR);
        assertThat(guardado.getActivo()).isFalse();
        assertThat(guardado.getIntentosFallidos()).isEqualTo(2);
        assertThat(guardado.getUltimoAcceso()).isEqualTo(ACCESO);
        assertThat(creado.password()).isNull();
        verify(adminRepository).existsByUsuarioIgnoreCase("admin");
        verify(adminRepository).existsByEmailIgnoreCase("admin@example.com");
    }

    @Test
    void normalizarIdentificadoresNoDependeDelIdiomaDelSistema() {
        Locale anterior = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            when(adminRepository.saveAndFlush(any(Admin.class))).thenAnswer(inv -> inv.getArgument(0));

            AdminDTO creado = adminService.crear(dto("ADMIN", "ADMIN@EXAMPLE.COM"));

            assertThat(creado.usuario()).isEqualTo("admin");
            assertThat(creado.email()).isEqualTo("admin@example.com");
        } finally {
            Locale.setDefault(anterior);
        }
    }

    @Test
    void crearConUsuarioDuplicadoNoGuarda() {
        when(adminRepository.existsByUsuarioIgnoreCase("admin")).thenReturn(true);

        assertThatThrownBy(() -> adminService.crear(dto(" ADMIN ", "nuevo@example.com")))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Ya existe un admin con el usuario admin");
        verify(adminRepository, never()).saveAndFlush(any());
    }

    @Test
    void crearConEmailDuplicadoNoGuarda() {
        when(adminRepository.existsByEmailIgnoreCase("admin@example.com")).thenReturn(true);

        assertThatThrownBy(() -> adminService.crear(dto("nuevo", "ADMIN@example.com")))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Ya existe un admin con el email admin@example.com");
        verify(adminRepository, never()).saveAndFlush(any());
    }

    @Test
    void listarOrdenaPorIdYNoExponePassword() {
        when(adminRepository.findAll(Sort.by("id"))).thenReturn(List.of(existente()));

        List<AdminDTO> resultado = adminService.listar();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.getFirst().id()).isEqualTo(1L);
        assertThat(resultado.getFirst().password()).isNull();
    }

    @Test
    void listarSinAdminsRetornaListaVacia() {
        when(adminRepository.findAll(Sort.by("id"))).thenReturn(List.of());
        assertThat(adminService.listar()).isEmpty();
    }

    @Test
    void obtenerPorIdNoExponePassword() {
        when(adminRepository.findById(1L)).thenReturn(Optional.of(existente()));
        AdminDTO resultado = adminService.obtenerPorId(1L);
        assertThat(resultado.id()).isEqualTo(1L);
        assertThat(resultado.usuario()).isEqualTo("admin");
        assertThat(resultado.password()).isNull();
    }

    @Test
    void obtenerInexistenteLanza404() {
        assertThatThrownBy(() -> adminService.obtenerPorId(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Admin con id 99 no encontrado");
    }

    @Test
    void actualizarPermiteConservarUsuarioYEmailPropiosYReemplazaDatos() {
        Admin admin = existente();
        when(adminRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(adminRepository.saveAndFlush(admin)).thenReturn(admin);

        AdminDTO actualizado = adminService.actualizar(1L, dto(" ADMIN ", "ADMIN@example.com"));

        assertThat(actualizado.id()).isEqualTo(1L);
        assertThat(actualizado.nombre()).isEqualTo("Ana");
        assertThat(actualizado.apellido()).isEqualTo("Pérez");
        assertThat(actualizado.usuario()).isEqualTo("admin");
        assertThat(actualizado.email()).isEqualTo("admin@example.com");
        assertThat(actualizado.rol()).isEqualTo(RolAdmin.LECTOR);
        assertThat(actualizado.activo()).isFalse();
        assertThat(actualizado.intentosFallidos()).isEqualTo(2);
        assertThat(actualizado.ultimoAcceso()).isEqualTo(ACCESO);
        assertThat(admin.getPassword()).isEqualTo(" Clave123 ");
        assertThat(actualizado.password()).isNull();
        verify(adminRepository).existsByUsuarioIgnoreCaseAndIdNot("admin", 1L);
        verify(adminRepository).existsByEmailIgnoreCaseAndIdNot("admin@example.com", 1L);
    }

    @Test
    void actualizarConUsuarioAjenoNoModificaNiGuarda() {
        Admin admin = existente();
        when(adminRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(adminRepository.existsByUsuarioIgnoreCaseAndIdNot("otro", 1L)).thenReturn(true);

        assertThatThrownBy(() -> adminService.actualizar(1L, dto("otro", "admin@example.com")))
                .isInstanceOf(DuplicateResourceException.class);
        assertThat(admin.getUsuario()).isEqualTo("admin");
        assertThat(admin.getPassword()).isEqualTo("ClaveAnterior");
        verify(adminRepository, never()).saveAndFlush(any());
    }

    @Test
    void actualizarConEmailAjenoNoModificaNiGuarda() {
        Admin admin = existente();
        when(adminRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(adminRepository.existsByEmailIgnoreCaseAndIdNot("otro@example.com", 1L)).thenReturn(true);

        assertThatThrownBy(() -> adminService.actualizar(1L, dto("admin", "otro@example.com")))
                .isInstanceOf(DuplicateResourceException.class);
        assertThat(admin.getEmail()).isEqualTo("admin@example.com");
        verify(adminRepository, never()).saveAndFlush(any());
    }

    @Test
    void actualizarInexistenteNoCreaUnAdmin() {
        assertThatThrownBy(() -> adminService.actualizar(99L, dto("admin", "admin@example.com")))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(adminRepository, never()).saveAndFlush(any());
    }

    @Test
    void eliminarBorraElAdminEncontrado() {
        Admin admin = existente();
        when(adminRepository.findById(1L)).thenReturn(Optional.of(admin));
        adminService.eliminar(1L);
        verify(adminRepository).delete(admin);
    }

    @Test
    void eliminarInexistenteNoBorra() {
        assertThatThrownBy(() -> adminService.eliminar(99L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(adminRepository, never()).delete(any());
    }
}
