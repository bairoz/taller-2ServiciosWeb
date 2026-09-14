package com.psicometria.api.services;

import com.psicometria.api.dto.EvaluadoDTO;
import com.psicometria.api.exceptions.DuplicateResourceException;
import com.psicometria.api.exceptions.ResourceNotFoundException;
import com.psicometria.api.models.Evaluado;
import com.psicometria.api.models.Genero;
import com.psicometria.api.models.NivelEducativo;
import com.psicometria.api.repositories.EvaluadoRepository;
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
class EvaluadoServiceTest {

    @Mock
    private EvaluadoRepository evaluadoRepository;

    @InjectMocks
    private EvaluadoService evaluadoService;

    private static EvaluadoDTO dto(String email) {
        return new EvaluadoDTO(null, "  Camila ", "Rojas", email, LocalDate.of(2005, 3, 21),
                Genero.FEMENINO, NivelEducativo.UNIVERSITARIO, true, null, null);
    }

    @Test
    void crearNormalizaDatosYGuarda() {
        when(evaluadoRepository.existsByEmailIgnoreCase("camila.rojas@correo.cl")).thenReturn(false);
        when(evaluadoRepository.saveAndFlush(any(Evaluado.class))).thenAnswer(inv -> inv.getArgument(0));

        EvaluadoDTO creado = evaluadoService.crear(dto(" Camila.Rojas@Correo.cl "));

        ArgumentCaptor<Evaluado> captor = ArgumentCaptor.forClass(Evaluado.class);
        verify(evaluadoRepository).saveAndFlush(captor.capture());
        assertThat(captor.getValue().getNombre()).isEqualTo("Camila");
        assertThat(captor.getValue().getEmail()).isEqualTo("camila.rojas@correo.cl");
        assertThat(creado.genero()).isEqualTo(Genero.FEMENINO);
    }

    @Test
    void crearConEmailDuplicadoLanzaExcepcion() {
        when(evaluadoRepository.existsByEmailIgnoreCase("camila.rojas@correo.cl")).thenReturn(true);

        assertThatThrownBy(() -> evaluadoService.crear(dto("camila.rojas@correo.cl")))
                .isInstanceOf(DuplicateResourceException.class);
        verify(evaluadoRepository, never()).saveAndFlush(any());
    }

    @Test
    void obtenerInexistenteLanzaExcepcion() {
        when(evaluadoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> evaluadoService.obtenerPorId(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Evaluado con id 99 no encontrado");
    }

    @Test
    void actualizarConEmailDeOtroEvaluadoLanzaExcepcion() {
        when(evaluadoRepository.findById(1L)).thenReturn(Optional.of(new Evaluado()));
        when(evaluadoRepository.existsByEmailIgnoreCaseAndIdNot("otro@correo.cl", 1L)).thenReturn(true);

        assertThatThrownBy(() -> evaluadoService.actualizar(1L, dto("otro@correo.cl")))
                .isInstanceOf(DuplicateResourceException.class);
        verify(evaluadoRepository, never()).saveAndFlush(any());
    }

    @Test
    void actualizarModificaLaEntidadExistente() {
        Evaluado existente = new Evaluado();
        existente.setId(1L);
        existente.setNivelEducativo(NivelEducativo.MEDIA);
        when(evaluadoRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(evaluadoRepository.existsByEmailIgnoreCaseAndIdNot("camila.rojas@correo.cl", 1L)).thenReturn(false);
        when(evaluadoRepository.saveAndFlush(existente)).thenReturn(existente);

        EvaluadoDTO actualizado = evaluadoService.actualizar(1L, dto("camila.rojas@correo.cl"));

        assertThat(actualizado.id()).isEqualTo(1L);
        assertThat(actualizado.nivelEducativo()).isEqualTo(NivelEducativo.UNIVERSITARIO);
    }

    @Test
    void eliminarInexistenteLanzaExcepcion() {
        when(evaluadoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> evaluadoService.eliminar(99L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(evaluadoRepository, never()).delete(any());
    }
}
