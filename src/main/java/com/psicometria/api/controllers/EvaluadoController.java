package com.psicometria.api.controllers;

import com.psicometria.api.dto.EvaluadoDTO;
import com.psicometria.api.services.EvaluadoService;
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
@RequestMapping("/api/evaluados")
public class EvaluadoController {

    private final EvaluadoService evaluadoService;

    public EvaluadoController(EvaluadoService evaluadoService) {
        this.evaluadoService = evaluadoService;
    }

    @GetMapping
    public ResponseEntity<List<EvaluadoDTO>> listar() {
        return ResponseEntity.ok(evaluadoService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EvaluadoDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(evaluadoService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<EvaluadoDTO> crear(@Valid @RequestBody EvaluadoDTO dto) {
        EvaluadoDTO creado = evaluadoService.crear(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(creado.id())
                .toUri();
        return ResponseEntity.created(location).body(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EvaluadoDTO> actualizar(@PathVariable Long id, @Valid @RequestBody EvaluadoDTO dto) {
        return ResponseEntity.ok(evaluadoService.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        evaluadoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
