package com.psicometria.api.services;

import com.psicometria.api.dto.TestDTO;
import com.psicometria.api.exceptions.DuplicateResourceException;
import com.psicometria.api.exceptions.ResourceNotFoundException;
import com.psicometria.api.models.Test;
import com.psicometria.api.repositories.TestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Logica de negocio de la entidad Test y conversion entidad <-> DTO.
 *
 * Usa las excepciones compartidas del paquete "exceptions":
 *  - ResourceNotFoundException  -> 404 Not Found
 *  - DuplicateResourceException -> 409 Conflict
 * Ambas las traduce a JSON el GlobalExceptionHandler.
 */
@Service
public class TestService {

    private static final String RECURSO = "Test";

    private final TestRepository testRepository;

    public TestService(TestRepository testRepository) {
        this.testRepository = testRepository;
    }

    @Transactional(readOnly = true)
    public List<TestDTO> listar() {
        return testRepository.findAll()
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public TestDTO buscarPorId(Long id) {
        return convertirADTO(obtenerOLanzarError(id));
    }

    @Transactional
    public TestDTO crear(TestDTO datos) {
        // La columna titulo es UNIQUE: se verifica antes de guardar para
        // devolver un 409 legible en vez de una violacion de integridad.
        if (testRepository.existsByTituloIgnoreCase(datos.getTitulo())) {
            throw new DuplicateResourceException(
                    "Ya existe un test con el titulo '" + datos.getTitulo() + "'");
        }
        Test nuevo = new Test();
        copiarDatos(datos, nuevo);
        return convertirADTO(testRepository.save(nuevo));
    }

    @Transactional
    public TestDTO actualizar(Long id, TestDTO datos) {
        Test existente = obtenerOLanzarError(id);
        if (testRepository.existsByTituloIgnoreCaseAndIdNot(datos.getTitulo(), id)) {
            throw new DuplicateResourceException(
                    "Ya existe otro test con el titulo '" + datos.getTitulo() + "'");
        }
        copiarDatos(datos, existente);
        return convertirADTO(testRepository.save(existente));
    }

    @Transactional
    public void eliminar(Long id) {
        testRepository.delete(obtenerOLanzarError(id));
    }

    /** Punto unico donde se lanza el 404. */
    private Test obtenerOLanzarError(Long id) {
        return testRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(RECURSO, id));
    }

    /** Vuelca los datos del DTO sobre la entidad. El id nunca se toma del cuerpo. */
    private void copiarDatos(TestDTO origen, Test destino) {
        destino.setPsicologoId(origen.getPsicologoId());
        destino.setTitulo(origen.getTitulo().trim());
        destino.setDescripcion(origen.getDescripcion());
        destino.setCategoria(origen.getCategoria());
        destino.setDuracionMinutos(origen.getDuracionMinutos());
        destino.setPuntajeAprobacion(origen.getPuntajeAprobacion());
        destino.setVisibilidad(origen.getVisibilidad());
    }

    private TestDTO convertirADTO(Test test) {
        TestDTO dto = new TestDTO();
        dto.setId(test.getId());
        dto.setPsicologoId(test.getPsicologoId());
        dto.setTitulo(test.getTitulo());
        dto.setDescripcion(test.getDescripcion());
        dto.setCategoria(test.getCategoria());
        dto.setDuracionMinutos(test.getDuracionMinutos());
        dto.setPuntajeAprobacion(test.getPuntajeAprobacion());
        dto.setVisibilidad(test.getVisibilidad());
        dto.setCreadoAt(test.getCreadoAt());
        dto.setActualizadoAt(test.getActualizadoAt());
        return dto;
    }
}