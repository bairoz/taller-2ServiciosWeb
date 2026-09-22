package com.psicometria.api.controllers;

import com.psicometria.api.dto.PsicologoDTO;
import com.psicometria.api.services.PsicologoService;
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
import java.util.Map;

@RestController
@RequestMapping("/api/psicologos")
public class PsicologoController {

    private final PsicologoService psicologoService;

    public PsicologoController(PsicologoService psicologoService) {
        this.psicologoService = psicologoService;
    }

    @GetMapping
    public ResponseEntity<List<PsicologoDTO>> listar() {
        return ResponseEntity.ok(psicologoService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PsicologoDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(psicologoService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<PsicologoDTO> crear(@Valid @RequestBody PsicologoDTO dto) {
        PsicologoDTO creado = psicologoService.crear(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(creado.id())
                .toUri();
        return ResponseEntity.created(location).body(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PsicologoDTO> actualizar(@PathVariable Long id, @Valid @RequestBody PsicologoDTO dto) {
        return ResponseEntity.ok(psicologoService.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> eliminar(@PathVariable Long id) {
        psicologoService.eliminar(id);
        return ResponseEntity.ok(Map.of("id", id, "mensaje", "Psicologo eliminado correctamente"));
    }
}
