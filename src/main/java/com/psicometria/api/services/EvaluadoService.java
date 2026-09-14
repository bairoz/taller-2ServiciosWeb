package com.psicometria.api.services;

import com.psicometria.api.dto.EvaluadoDTO;
import com.psicometria.api.exceptions.DuplicateResourceException;
import com.psicometria.api.exceptions.ResourceNotFoundException;
import com.psicometria.api.models.Evaluado;
import com.psicometria.api.repositories.EvaluadoRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Lógica de negocio de evaluados: reglas de negocio y conversión entre entidad y DTO.
 */
@Service
@Transactional(readOnly = true)
public class EvaluadoService {

    private static final String RECURSO = "Evaluado";

    private final EvaluadoRepository evaluadoRepository;

    public EvaluadoService(EvaluadoRepository evaluadoRepository) {
        this.evaluadoRepository = evaluadoRepository;
    }

    public List<EvaluadoDTO> listar() {
        return evaluadoRepository.findAll(Sort.by("id")).stream()
                .map(this::toDTO)
                .toList();
    }

    public EvaluadoDTO obtenerPorId(Long id) {
        return toDTO(buscar(id));
    }

    @Transactional
    public EvaluadoDTO crear(EvaluadoDTO dto) {
        String email = normalizarEmail(dto.email());
        if (evaluadoRepository.existsByEmailIgnoreCase(email)) {
            throw new DuplicateResourceException("Ya existe un evaluado con el email " + email);
        }
        Evaluado evaluado = new Evaluado();
        copiarDatos(dto, evaluado);
        return toDTO(evaluadoRepository.saveAndFlush(evaluado));
    }

    @Transactional
    public EvaluadoDTO actualizar(Long id, EvaluadoDTO dto) {
        Evaluado evaluado = buscar(id);
        String email = normalizarEmail(dto.email());
        if (evaluadoRepository.existsByEmailIgnoreCaseAndIdNot(email, id)) {
            throw new DuplicateResourceException("Ya existe un evaluado con el email " + email);
        }
        copiarDatos(dto, evaluado);
        return toDTO(evaluadoRepository.saveAndFlush(evaluado));
    }

    @Transactional
    public void eliminar(Long id) {
        evaluadoRepository.delete(buscar(id));
    }

    private Evaluado buscar(Long id) {
        return evaluadoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(RECURSO, id));
    }

    private void copiarDatos(EvaluadoDTO dto, Evaluado evaluado) {
        evaluado.setNombre(dto.nombre().trim());
        evaluado.setApellido(dto.apellido().trim());
        evaluado.setEmail(normalizarEmail(dto.email()));
        evaluado.setFechaNacimiento(dto.fechaNacimiento());
        evaluado.setGenero(dto.genero());
        evaluado.setNivelEducativo(dto.nivelEducativo());
        evaluado.setActivo(dto.activo());
    }

    private String normalizarEmail(String email) {
        return email.trim().toLowerCase();
    }

    private EvaluadoDTO toDTO(Evaluado evaluado) {
        return new EvaluadoDTO(
                evaluado.getId(),
                evaluado.getNombre(),
                evaluado.getApellido(),
                evaluado.getEmail(),
                evaluado.getFechaNacimiento(),
                evaluado.getGenero(),
                evaluado.getNivelEducativo(),
                evaluado.getActivo(),
                evaluado.getCreadoAt(),
                evaluado.getActualizadoAt()
        );
    }
}
