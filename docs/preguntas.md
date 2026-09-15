# Entidad: `preguntas`

Documentación de la parte del CRUD correspondiente a la entidad `preguntas`, desarrollada en la rama `feature/preguntas`.

> Este archivo vive en `docs/` en vez de en el `README.md` raíz porque, según las reglas del equipo, `README.md` es un archivo compartido y no debe modificarse desde una rama de entidad. Si el equipo decide consolidar toda la documentación en el README principal, este contenido puede copiarse/enlazarse desde `main`.

## Ruta base

`/api/preguntas`

## Atributos

| Campo | Tipo (BD) | Notas |
|---|---|---|
| `id` | `BIGINT` | Generado automáticamente (`GENERATED ALWAYS AS IDENTITY`) |
| `testId` | `BIGINT` | FK a `tests`, obligatorio |
| `enunciado` | `TEXT` | Obligatorio |
| `tipo` | `ENUM` (`tipo_pregunta`) | `OPCION_MULTIPLE`, `VERDADERO_FALSO`, `ESCALA_LIKERT`, `RESPUESTA_ABIERTA` |
| `opciones` | `JSONB` | Opcional; debe ser un arreglo JSON |
| `respuestaCorrecta` | `TEXT` | Opcional |
| `puntaje` | `NUMERIC(5,2)` | Obligatorio, ≥ 0 |
| `orden` | `INT` | Obligatorio, > 0, único por `testId` |
| `obligatoria` | `BOOLEAN` | Por defecto `true` |

## Endpoints

| Método | Ruta | Función | Respuesta esperada |
|---|---|---|---|
| `GET` | `/api/preguntas` | Listar todas las preguntas | `200 OK` + arreglo JSON |
| `GET` | `/api/preguntas/{id}` | Buscar pregunta por ID | `200 OK` o `404 Not Found` |
| `POST` | `/api/preguntas` | Registrar una nueva pregunta | `201 Created` o `400 Bad Request` |
| `PUT` | `/api/preguntas/{id}` | Actualizar una pregunta existente | `200 OK`, `400` o `404` |
| `DELETE` | `/api/preguntas/{id}` | Eliminar una pregunta | `200 OK` o `404 Not Found` |

## Validaciones (Bean Validation en `PreguntaDTO`)

- `testId`: obligatorio.
- `enunciado`: obligatorio (`@NotBlank`).
- `tipo`: obligatorio, debe ser uno de los valores del enum `TipoPregunta`.
- `puntaje`: obligatorio, no puede ser negativo.
- `orden`: obligatorio, debe ser mayor a 0. Además, `orden` debe ser único dentro del mismo `testId` (validado en el `service` antes de persistir, ya que la base de datos también lo exige vía `UNIQUE(test_id, orden)`).

## Ejemplo de error de validación (`400`)

```json
{
  "timestamp": "2026-09-14T19:03:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Error de validación",
  "path": "/api/preguntas",
  "errores": {
    "orden": "El orden debe ser mayor o igual a 1",
    "puntaje": "El puntaje no puede ser negativo",
    "enunciado": "El enunciado es obligatorio"
  }
}
```

## Ejemplo de conflicto por `orden` duplicado (`409`)

```json
{
  "timestamp": "2026-09-14T19:06:44",
  "status": 409,
  "error": "Conflict",
  "message": "Ya existe una pregunta con el orden 1 para el test 3",
  "path": "/api/preguntas"
}
```

## Ejemplo de recurso no encontrado (`404`)

```json
{
  "timestamp": "2026-09-14T19:07:48",
  "status": 404,
  "error": "Not Found",
  "message": "Pregunta con id 9999 no encontrado",
  "path": "/api/preguntas/9999"
}
```

## Pruebas realizadas

Se ejecutaron las 8 pruebas requeridas en Postman. Colección y environment disponibles en [`postman/`](../postman/), evidencias (capturas) en [`evidencias/`](../evidencias/).

| # | Prueba | Método | Resultado |
|---|--------|--------|-----------|
| 1 | Crear pregunta | `POST` | ✅ `201 Created` |
| 2 | Listar todas las preguntas | `GET` | ✅ `200 OK` |
| 3 | Buscar pregunta por ID | `GET` | ✅ `200 OK` |
| 4 | Actualizar pregunta | `PUT` | ✅ `200 OK` |
| 5 | Eliminar pregunta | `DELETE` | ✅ `200 OK` |
| 6 | Datos inválidos: campos obligatorios faltantes | `POST` | ✅ `400 Bad Request` |
| 7 | Datos inválidos: `orden` duplicado en el mismo test | `POST` | ✅ `409 Conflict` |
| 8 | ID inexistente | `GET` | ✅ `404 Not Found` |

### Cómo reproducir las pruebas

1. Importar en Postman `postman/preguntas.postman_collection.json` y `postman/preguntas-local.postman_environment.json`.
2. Seleccionar el environment **"Preguntas - Local"**.
3. Ejecutar las requests en orden (01 a 08). La request 01 guarda automáticamente el `id` creado en la variable `preguntaId`, reutilizada por las pruebas 03, 04 y 05.

## Recorrido de una solicitud

`PreguntaController` recibe la petición HTTP y valida el `PreguntaDTO` con `@Valid` → `PreguntaService` aplica las reglas de negocio (por ejemplo, verificar que no exista ya una pregunta con el mismo `orden` para el `testId` dado) y convierte entre `PreguntaDTO` y la entidad `Pregunta` → `PreguntaRepository` (interfaz `JpaRepository`) persiste o consulta contra PostgreSQL → la respuesta se serializa de vuelta a JSON con el código HTTP correspondiente, envuelta en `ResponseEntity`. Los errores (validación, no encontrado, conflicto) son capturados por `GlobalExceptionHandler` (`@RestControllerAdvice`), que los transforma en un `ErrorResponse` consistente con el resto de las entidades del proyecto.
