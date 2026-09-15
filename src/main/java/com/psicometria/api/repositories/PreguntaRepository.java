package com.psicometria.api.repositories;

import com.psicometria.api.models.Pregunta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PreguntaRepository extends JpaRepository<Pregunta, Long> {

    List<Pregunta> findByTestId(Long testId);

    boolean existsByTestIdAndOrden(Long testId, Integer orden);

    boolean existsByTestIdAndOrdenAndIdNot(Long testId, Integer orden, Long id);

    @Query(value = "SELECT EXISTS(SELECT 1 FROM tests WHERE id = :testId)", nativeQuery = true)
    boolean existsTestById(@Param("testId") Long testId);
}