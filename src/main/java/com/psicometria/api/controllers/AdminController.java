package com.psicometria.api.controllers;

import com.psicometria.api.dto.AdminDTO;
import com.psicometria.api.services.AdminService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/admins")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping
    public ResponseEntity<List<AdminDTO>> listar() {
        return ResponseEntity.ok(adminService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdminDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<AdminDTO> crear(@Valid @RequestBody AdminDTO dto) {
        AdminDTO creado = adminService.crear(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(creado.id())
                .toUri();
        return ResponseEntity.created(location).body(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AdminDTO> actualizar(@PathVariable Long id, @Valid @RequestBody AdminDTO dto) {
        return ResponseEntity.ok(adminService.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        adminService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
