package com.psicometria.api.services;

import com.psicometria.api.dto.EvaluadoDTO;
import com.psicometria.api.exceptions.DuplicateResourceException;
import com.psicometria.api.exceptions.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Lógica de negocio de evaluados. Por ahora los datos se guardan en memoria;
 * al conectar la base de datos solo cambia esta clase.
 */
@Service
public class EvaluadoService {

    private static final String RECURSO = "Evaluado";

    private final Map<Long, EvaluadoDTO> evaluados = new ConcurrentHashMap<>();
    private final AtomicLong secuencia = new AtomicLong();

    public List<EvaluadoDTO> listar() {
        return evaluados.values().stream()
                .sorted(Comparator.comparing(EvaluadoDTO::id))
                .toList();
    }

    public EvaluadoDTO obtenerPorId(Long id) {
        EvaluadoDTO evaluado = evaluados.get(id);
        if (evaluado == null) {
            throw new ResourceNotFoundException(RECURSO, id);
        }
        return evaluado;
    }

    public synchronized EvaluadoDTO crear(EvaluadoDTO dto) {
        validarEmailUnico(dto.email(), null);
        LocalDateTime ahora = LocalDateTime.now();
        EvaluadoDTO nuevo = dto.withAuditoria(secuencia.incrementAndGet(), ahora, ahora);
        evaluados.put(nuevo.id(), nuevo);
        return nuevo;
    }

    public synchronized EvaluadoDTO actualizar(Long id, EvaluadoDTO dto) {
        EvaluadoDTO actual = obtenerPorId(id);
        validarEmailUnico(dto.email(), id);
        EvaluadoDTO actualizado = dto.withAuditoria(id, actual.creadoAt(), LocalDateTime.now());
        evaluados.put(id, actualizado);
        return actualizado;
    }

    public synchronized void eliminar(Long id) {
        if (evaluados.remove(id) == null) {
            throw new ResourceNotFoundException(RECURSO, id);
        }
    }

    private void validarEmailUnico(String email, Long idExcluido) {
        String normalizado = email.trim().toLowerCase();
        boolean existe = evaluados.values().stream()
                .anyMatch(e -> e.email().equals(normalizado) && !e.id().equals(idExcluido));
        if (existe) {
            throw new DuplicateResourceException("Ya existe un evaluado con el email " + normalizado);
        }
    }
}
