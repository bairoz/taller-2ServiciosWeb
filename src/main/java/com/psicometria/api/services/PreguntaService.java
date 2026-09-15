package com.psicometria.api.services;

import com.psicometria.api.dto.PreguntaDTO;
import com.psicometria.api.exceptions.DuplicateResourceException;
import com.psicometria.api.exceptions.ResourceNotFoundException;
import com.psicometria.api.models.Pregunta;
import com.psicometria.api.repositories.PreguntaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PreguntaService {

    private final PreguntaRepository preguntaRepository;

    public PreguntaService(PreguntaRepository preguntaRepository) {
        this.preguntaRepository = preguntaRepository;
    }

    public List<PreguntaDTO> listar() {
        return preguntaRepository.findAll().stream().map(this::toDTO).toList();
    }

    public PreguntaDTO buscarPorId(Long id) {
        return toDTO(obtenerOFallar(id));
    }

    public PreguntaDTO registrar(PreguntaDTO dto) {
        validarTestExiste(dto.testId());
        validarOrdenDisponible(dto.testId(), dto.orden(), null);

        Pregunta pregunta = new Pregunta();
        aplicarDatos(pregunta, dto);
        return toDTO(preguntaRepository.save(pregunta));
    }

    public PreguntaDTO actualizar(Long id, PreguntaDTO dto) {
        Pregunta pregunta = obtenerOFallar(id);
        validarTestExiste(dto.testId());
        validarOrdenDisponible(dto.testId(), dto.orden(), id);

        aplicarDatos(pregunta, dto);
        return toDTO(preguntaRepository.save(pregunta));
    }

    public void eliminar(Long id) {
        preguntaRepository.delete(obtenerOFallar(id));
    }

    private Pregunta obtenerOFallar(Long id) {
        return preguntaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pregunta", id));
    }

    private void validarTestExiste(Long testId) {
        if (!preguntaRepository.existsTestById(testId)) {
            throw new ResourceNotFoundException("Test", testId);
        }
    }

    private void validarOrdenDisponible(Long testId, Integer orden, Long idActual) {
        boolean duplicado = (idActual == null)
                ? preguntaRepository.existsByTestIdAndOrden(testId, orden)
                : preguntaRepository.existsByTestIdAndOrdenAndIdNot(testId, orden, idActual);

        if (duplicado) {
            throw new DuplicateResourceException(
                    "Ya existe una pregunta con el orden " + orden + " para el test " + testId);
        }
    }

    private void aplicarDatos(Pregunta pregunta, PreguntaDTO dto) {
        pregunta.setTestId(dto.testId());
        pregunta.setEnunciado(dto.enunciado());
        pregunta.setTipo(dto.tipo());
        pregunta.setOpciones(dto.opciones());
        pregunta.setRespuestaCorrecta(dto.respuestaCorrecta());
        pregunta.setPuntaje(dto.puntaje());
        pregunta.setOrden(dto.orden());
        pregunta.setObligatoria(dto.obligatoria() != null ? dto.obligatoria() : Boolean.TRUE);
    }

    private PreguntaDTO toDTO(Pregunta p) {
        return new PreguntaDTO(p.getId(), p.getTestId(), p.getEnunciado(), p.getTipo(),
                p.getOpciones(), p.getRespuestaCorrecta(), p.getPuntaje(), p.getOrden(), p.getObligatoria());
    }
}