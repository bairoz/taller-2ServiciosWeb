package com.psicometria.api.repositories;

import com.psicometria.api.models.Test;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Acceso a datos de la tabla "tests".
 *
 * Los metodos derivados se usan para verificar la restriccion UNIQUE de
 * "titulo" antes de guardar, y asi devolver un error controlado en lugar
 * de dejar que explote la violacion de integridad de PostgreSQL.
 */
@Repository
public interface TestRepository extends JpaRepository<Test, Long> {

    boolean existsByTituloIgnoreCase(String titulo);

    boolean existsByTituloIgnoreCaseAndIdNot(String titulo, Long id);
}