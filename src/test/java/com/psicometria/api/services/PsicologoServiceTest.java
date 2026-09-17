package com.psicometria.api.services;

import com.psicometria.api.dto.PsicologoDTO;
import com.psicometria.api.exceptions.DuplicateResourceException;
import com.psicometria.api.exceptions.ResourceNotFoundException;
import com.psicometria.api.models.Psicologo;
import com.psicometria.api.models.EspecialidadPsicologo;

import com.psicometria.api.repositories.PsicologoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PsicologoServiceTest {

    @Mock
    private PsicologoRepository psicologoRepository;

    @InjectMocks
    private PsicologoService psicologoService;

    private static PsicologoDTO dto(String email) {
        return new PsicologoDTO(null, "  Camila ", "Rojas", email, " psi-test-01 ", EspecialidadPsicologo.CLINICA, 5, LocalDate.of(2005, 3, 21), true, null, null);
    }

    @Test
    void crearNormalizaDatosYGuarda() {
        when(psicologoRepository.existsByEmailIgnoreCase("camila.rojas@correo.cl")).thenReturn(false);
        when(psicologoRepository.saveAndFlush(any(Psicologo.class))).thenAnswer(inv -> inv.getArgument(0));

        PsicologoDTO creado = psicologoService.crear(dto(" Camila.Rojas@Correo.cl "));

        ArgumentCaptor<Psicologo> captor = ArgumentCaptor.forClass(Psicologo.class);
        verify(psicologoRepository).saveAndFlush(captor.capture());
        assertThat(captor.getValue().getNombre()).isEqualTo("Camila");
        assertThat(captor.getValue().getEmail()).isEqualTo("camila.rojas@correo.cl");
        assertThat(creado.especialidad()).isEqualTo(EspecialidadPsicologo.CLINICA);
        assertThat(creado.numeroRegistro()).isEqualTo("PSI-TEST-01");
    }

    @Test
    void crearConEmailDuplicadoLanzaExcepcion() {
        when(psicologoRepository.existsByEmailIgnoreCase("camila.rojas@correo.cl")).thenReturn(true);

        assertThatThrownBy(() -> psicologoService.crear(dto("camila.rojas@correo.cl")))
                .isInstanceOf(DuplicateResourceException.class);
        verify(psicologoRepository, never()).saveAndFlush(any());
    }

    @Test
    void obtenerInexistenteLanzaExcepcion() {
        when(psicologoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> psicologoService.obtenerPorId(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Psicologo con id 99 no encontrado");
    }

    @Test
    void actualizarConEmailDeOtroPsicologoLanzaExcepcion() {
        when(psicologoRepository.findById(1L)).thenReturn(Optional.of(new Psicologo()));
        when(psicologoRepository.existsByEmailIgnoreCaseAndIdNot("otro@correo.cl", 1L)).thenReturn(true);

        assertThatThrownBy(() -> psicologoService.actualizar(1L, dto("otro@correo.cl")))
                .isInstanceOf(DuplicateResourceException.class);
        verify(psicologoRepository, never()).saveAndFlush(any());
    }

    @Test
    void actualizarModificaLaEntidadExistente() {
        Psicologo existente = new Psicologo();
        existente.setId(1L);
        existente.setAniosExperiencia(0);
        when(psicologoRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(psicologoRepository.existsByEmailIgnoreCaseAndIdNot("camila.rojas@correo.cl", 1L)).thenReturn(false);
        when(psicologoRepository.saveAndFlush(existente)).thenReturn(existente);

        PsicologoDTO actualizado = psicologoService.actualizar(1L, dto("camila.rojas@correo.cl"));

        assertThat(actualizado.id()).isEqualTo(1L);
        assertThat(actualizado.aniosExperiencia()).isEqualTo(5);
    }

    @Test
    void eliminarInexistenteLanzaExcepcion() {
        when(psicologoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> psicologoService.eliminar(99L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(psicologoRepository, never()).delete(any());
    }

    @Test
    void crearConRegistroDuplicadoNoGuarda() {
        when(psicologoRepository.existsByNumeroRegistroIgnoreCase("PSI-TEST-01")).thenReturn(true);
        assertThatThrownBy(() -> psicologoService.crear(dto("nuevo@correo.cl")))
                .isInstanceOf(DuplicateResourceException.class);
        verify(psicologoRepository, never()).saveAndFlush(any());
    }

    @Test
    void actualizarConRegistroDeOtroPsicologoNoGuarda() {
        when(psicologoRepository.findById(1L)).thenReturn(Optional.of(new Psicologo()));
        when(psicologoRepository.existsByNumeroRegistroIgnoreCaseAndIdNot("PSI-TEST-01", 1L)).thenReturn(true);
        assertThatThrownBy(() -> psicologoService.actualizar(1L, dto("nuevo@correo.cl")))
                .isInstanceOf(DuplicateResourceException.class);
        verify(psicologoRepository, never()).saveAndFlush(any());
    }

    @Test
    void actualizarInexistenteNoGuarda() {
        assertThatThrownBy(() -> psicologoService.actualizar(99L, dto("nuevo@correo.cl")))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(psicologoRepository, never()).saveAndFlush(any());
    }

    @Test
    void eliminarExistente() {
        Psicologo existente = new Psicologo();
        existente.setId(1L);
        when(psicologoRepository.findById(1L)).thenReturn(Optional.of(existente));
        psicologoService.eliminar(1L);
        verify(psicologoRepository).delete(existente);
    }
}
