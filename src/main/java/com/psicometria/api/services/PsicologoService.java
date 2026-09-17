package com.psicometria.api.services;

import com.psicometria.api.dto.PsicologoDTO;
import com.psicometria.api.exceptions.DuplicateResourceException;
import com.psicometria.api.exceptions.ResourceNotFoundException;
import com.psicometria.api.models.Psicologo;
import com.psicometria.api.repositories.PsicologoRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

/**
 * Lógica de negocio de psicologos: reglas de negocio y conversión entre entidad y DTO.
 */
@Service
@Transactional(readOnly = true)
public class PsicologoService {

    private static final String RECURSO = "Psicologo";

    private final PsicologoRepository psicologoRepository;

    public PsicologoService(PsicologoRepository psicologoRepository) {
        this.psicologoRepository = psicologoRepository;
    }

    public List<PsicologoDTO> listar() {
        return psicologoRepository.findAll(Sort.by("id")).stream()
                .map(this::toDTO)
                .toList();
    }

    public PsicologoDTO obtenerPorId(Long id) {
        return toDTO(buscar(id));
    }

    @Transactional
    public PsicologoDTO crear(PsicologoDTO dto) {
        String email = normalizarEmail(dto.email());
        if (psicologoRepository.existsByEmailIgnoreCase(email)) {
            throw new DuplicateResourceException("Ya existe un psicologo con el email " + email);
        }
        if (psicologoRepository.existsByNumeroRegistroIgnoreCase(normalizarRegistro(dto.numeroRegistro()))) {
            throw new DuplicateResourceException("Ya existe un psicologo con ese numero de registro");
        }
        Psicologo psicologo = new Psicologo();
        copiarDatos(dto, psicologo);
        return toDTO(psicologoRepository.saveAndFlush(psicologo));
    }

    @Transactional
    public PsicologoDTO actualizar(Long id, PsicologoDTO dto) {
        Psicologo psicologo = buscar(id);
        String email = normalizarEmail(dto.email());
        if (psicologoRepository.existsByEmailIgnoreCaseAndIdNot(email, id)) {
            throw new DuplicateResourceException("Ya existe un psicologo con el email " + email);
        }
        if (psicologoRepository.existsByNumeroRegistroIgnoreCaseAndIdNot(normalizarRegistro(dto.numeroRegistro()), id)) {
            throw new DuplicateResourceException("Ya existe un psicologo con ese numero de registro");
        }
        copiarDatos(dto, psicologo);
        return toDTO(psicologoRepository.saveAndFlush(psicologo));
    }

    @Transactional
    public void eliminar(Long id) {
        psicologoRepository.delete(buscar(id));
    }

    private Psicologo buscar(Long id) {
        return psicologoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(RECURSO, id));
    }

    private void copiarDatos(PsicologoDTO dto, Psicologo psicologo) {
        psicologo.setNombre(dto.nombre().trim());
        psicologo.setApellido(dto.apellido().trim());
        psicologo.setEmail(normalizarEmail(dto.email()));
        psicologo.setNumeroRegistro(normalizarRegistro(dto.numeroRegistro()));
        psicologo.setFechaTitulacion(dto.fechaTitulacion());
        psicologo.setEspecialidad(dto.especialidad());
        psicologo.setAniosExperiencia(dto.aniosExperiencia());
        psicologo.setDisponible(dto.disponible());
    }

    private String normalizarEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizarRegistro(String registro) {
        return registro.trim().toUpperCase(Locale.ROOT);
    }

    private PsicologoDTO toDTO(Psicologo psicologo) {
        return new PsicologoDTO(
                psicologo.getId(),
                psicologo.getNombre(),
                psicologo.getApellido(),
                psicologo.getEmail(),
                psicologo.getNumeroRegistro(),
                psicologo.getEspecialidad(),
                psicologo.getAniosExperiencia(),
                psicologo.getFechaTitulacion(),
                psicologo.getDisponible(),
                psicologo.getCreadoAt(),
                psicologo.getActualizadoAt()
        );
    }
}
