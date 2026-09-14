package com.psicometria.api.repositories;

import com.psicometria.api.models.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminRepository extends JpaRepository<Admin, Long> {

    boolean existsByUsuarioIgnoreCase(String usuario);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByUsuarioIgnoreCaseAndIdNot(String usuario, Long id);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
}
