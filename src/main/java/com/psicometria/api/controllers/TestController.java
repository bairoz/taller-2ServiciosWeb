package com.psicometria.api.controllers;


import com.psicometria.api.dto.TestDTO;
import com.psicometria.api.services.TestService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Endpoints REST de la entidad Test.
 * Ruta base: /api/tests
 */
@RestController
@RequestMapping("/api/tests")
public class TestController {

    private final TestService testService;

    public TestController(TestService testService) {
        this.testService = testService;
    }

    /** GET /api/tests -> 200 OK con el arreglo JSON de todos los tests. */
    @GetMapping
    public ResponseEntity<List<TestDTO>> listar() {
        return ResponseEntity.ok(testService.listar());
    }

    /** GET /api/tests/{id} -> 200 OK, o 404 si el id no existe. */
    @GetMapping("/{id}")
    public ResponseEntity<TestDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(testService.buscarPorId(id));
    }

    /** POST /api/tests -> 201 Created, o 400 si los datos no pasan las validaciones. */
    @PostMapping
    public ResponseEntity<TestDTO> crear(@Valid @RequestBody TestDTO testDTO,
                                         UriComponentsBuilder uriBuilder) {
        TestDTO creado = testService.crear(testDTO);
        URI ubicacion = uriBuilder.path("/api/tests/{id}")
                .buildAndExpand(creado.getId())
                .toUri();
        return ResponseEntity.created(ubicacion).body(creado);
    }

    /** PUT /api/tests/{id} -> 200 OK, 400 si los datos son invalidos, 404 si el id no existe. */
    @PutMapping("/{id}")
    public ResponseEntity<TestDTO> actualizar(@PathVariable Long id,
                                              @Valid @RequestBody TestDTO testDTO) {
        return ResponseEntity.ok(testService.actualizar(id, testDTO));
    }

    /** DELETE /api/tests/{id} -> 200 OK con mensaje JSON, o 404 si el id no existe. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> eliminar(@PathVariable Long id) {
        testService.eliminar(id);
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("estado", HttpStatus.OK.value());
        cuerpo.put("mensaje", "Test eliminado correctamente");
        cuerpo.put("id", id);
        return ResponseEntity.ok(cuerpo);
    }
}