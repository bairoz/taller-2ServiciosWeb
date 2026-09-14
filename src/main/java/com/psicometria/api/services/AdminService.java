package com.psicometria.api.services;

import com.psicometria.api.dto.AdminDTO;
import com.psicometria.api.exceptions.DuplicateResourceException;
import com.psicometria.api.exceptions.ResourceNotFoundException;
import com.psicometria.api.models.Admin;
import com.psicometria.api.repositories.AdminRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class AdminService {

    private static final String RECURSO = "Admin";

    private final AdminRepository adminRepository;

    public AdminService(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    public List<AdminDTO> listar() {
        return adminRepository.findAll(Sort.by("id")).stream()
                .map(this::toDTO)
                .toList();
    }

    public AdminDTO obtenerPorId(Long id) {
        return toDTO(buscar(id));
    }

    @Transactional
    public AdminDTO crear(AdminDTO dto) {
        String usuario = normalizarIdentificador(dto.usuario());
        String email = normalizarIdentificador(dto.email());
        if (adminRepository.existsByUsuarioIgnoreCase(usuario)) {
            throw usuarioDuplicado(usuario);
        }
        if (adminRepository.existsByEmailIgnoreCase(email)) {
            throw emailDuplicado(email);
        }
        Admin admin = new Admin();
        copiarDatos(dto, admin);
        return toDTO(adminRepository.saveAndFlush(admin));
    }

    @Transactional
    public AdminDTO actualizar(Long id, AdminDTO dto) {
        Admin admin = buscar(id);
        String usuario = normalizarIdentificador(dto.usuario());
        String email = normalizarIdentificador(dto.email());
        if (adminRepository.existsByUsuarioIgnoreCaseAndIdNot(usuario, id)) {
            throw usuarioDuplicado(usuario);
        }
        if (adminRepository.existsByEmailIgnoreCaseAndIdNot(email, id)) {
            throw emailDuplicado(email);
        }
        copiarDatos(dto, admin);
        return toDTO(adminRepository.saveAndFlush(admin));
    }

    @Transactional
    public void eliminar(Long id) {
        adminRepository.delete(buscar(id));
    }

    private Admin buscar(Long id) {
        return adminRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(RECURSO, id));
    }

    private void copiarDatos(AdminDTO dto, Admin admin) {
        admin.setNombre(dto.nombre().trim());
        admin.setApellido(dto.apellido().trim());
        admin.setUsuario(normalizarIdentificador(dto.usuario()));
        admin.setEmail(normalizarIdentificador(dto.email()));
        admin.setPassword(dto.password());
        admin.setRol(dto.rol());
        admin.setActivo(dto.activo());
        admin.setIntentosFallidos(dto.intentosFallidos());
        admin.setUltimoAcceso(dto.ultimoAcceso());
    }

    private String normalizarIdentificador(String valor) {
        return valor.trim().toLowerCase(Locale.ROOT);
    }

    private DuplicateResourceException usuarioDuplicado(String usuario) {
        return new DuplicateResourceException("Ya existe un admin con el usuario " + usuario);
    }

    private DuplicateResourceException emailDuplicado(String email) {
        return new DuplicateResourceException("Ya existe un admin con el email " + email);
    }

    private AdminDTO toDTO(Admin admin) {
        return new AdminDTO(
                admin.getId(),
                admin.getNombre(),
                admin.getApellido(),
                admin.getUsuario(),
                admin.getEmail(),
                null,
                admin.getRol(),
                admin.getActivo(),
                admin.getIntentosFallidos(),
                admin.getUltimoAcceso(),
                admin.getCreadoAt(),
                admin.getActualizadoAt()
        );
    }
}
