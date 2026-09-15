package com.psicometria.api.controllers;

import com.psicometria.api.dto.PreguntaDTO;
import com.psicometria.api.services.PreguntaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/preguntas")
public class PreguntaController {

    private final PreguntaService preguntaService;

    public PreguntaController(PreguntaService preguntaService) {
        this.preguntaService = preguntaService;
    }

    @GetMapping
    public ResponseEntity<List<PreguntaDTO>> listar() {
        return ResponseEntity.ok(preguntaService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PreguntaDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(preguntaService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<PreguntaDTO> registrar(@Valid @RequestBody PreguntaDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(preguntaService.registrar(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PreguntaDTO> actualizar(@PathVariable Long id, @Valid @RequestBody PreguntaDTO dto) {
        return ResponseEntity.ok(preguntaService.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        preguntaService.eliminar(id);
        return ResponseEntity.ok().build();
    }
}