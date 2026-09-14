package com.psicometria.api.repositories;

import com.psicometria.api.models.Evaluado;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EvaluadoRepository extends JpaRepository<Evaluado, Long> {

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
}
