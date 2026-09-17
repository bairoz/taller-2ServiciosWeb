package com.psicometria.api.repositories;

import com.psicometria.api.models.Psicologo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PsicologoRepository extends JpaRepository<Psicologo, Long> {
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
    boolean existsByNumeroRegistroIgnoreCase(String numeroRegistro);
    boolean existsByNumeroRegistroIgnoreCaseAndIdNot(String numeroRegistro, Long id);
}
