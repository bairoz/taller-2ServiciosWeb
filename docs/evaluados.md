# Entidad: Evaluados

Ruta base: `/api/evaluados`

## DTO (`EvaluadoDTO`)

| Campo             | Tipo              | Validaciones                                              |
|-------------------|-------------------|-----------------------------------------------------------|
| `id`              | `Long`            | Solo lectura (lo asigna el servidor)                      |
| `nombre`          | `String`          | `@NotBlank`, `@Size(min = 2, max = 100)`, `@Pattern` (solo letras) |
| `apellido`        | `String`          | `@NotBlank`, `@Size(min = 2, max = 100)`, `@Pattern` (solo letras) |
| `email`           | `String`          | `@NotBlank`, `@Email`, `@Size(max = 255)`, único          |
| `fechaNacimiento` | `LocalDate`       | `@NotNull`, `@Past`                                       |
| `genero`          | `Genero` (enum)   | `@NotNull` — `MASCULINO`, `FEMENINO`, `NO_BINARIO`, `PREFIERO_NO_DECIR` |
| `nivelEducativo`  | `NivelEducativo` (enum) | `@NotNull` — `BASICA`, `MEDIA`, `TECNICO`, `UNIVERSITARIO`, `POSTGRADO` |
| `activo`          | `Boolean`         | `@NotNull`                                                |
| `creadoAt`        | `LocalDateTime`   | Solo lectura                                              |
| `actualizadoAt`   | `LocalDateTime`   | Solo lectura                                              |

## Endpoints

| Método   | Ruta                   | Descripción              | Respuesta exitosa |
|----------|------------------------|--------------------------|-------------------|
| `GET`    | `/api/evaluados`       | Lista todos los evaluados | `200 OK`          |
| `GET`    | `/api/evaluados/{id}`  | Obtiene un evaluado       | `200 OK`          |
| `POST`   | `/api/evaluados`       | Crea un evaluado (`@Valid`) | `201 Created` + header `Location` |
| `PUT`    | `/api/evaluados/{id}`  | Actualiza un evaluado (`@Valid`) | `200 OK`   |
| `DELETE` | `/api/evaluados/{id}`  | Elimina un evaluado       | `204 No Content`  |

Errores: `400` validación / JSON inválido, `404` no encontrado, `409` email duplicado.

## Ejemplos

Las mismas peticiones están en [`evaluados.http`](evaluados.http) (IntelliJ / VS Code REST Client).

### Crear

```bash
curl -i -X POST http://localhost:8080/api/evaluados \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Camila",
    "apellido": "Rojas",
    "email": "camila.rojas@correo.cl",
    "fechaNacimiento": "2005-03-21",
    "genero": "FEMENINO",
    "nivelEducativo": "UNIVERSITARIO",
    "activo": true
  }'
```

Respuesta `201 Created`:

```json
{
  "id": 1,
  "nombre": "Camila",
  "apellido": "Rojas",
  "email": "camila.rojas@correo.cl",
  "fechaNacimiento": "2005-03-21",
  "genero": "FEMENINO",
  "nivelEducativo": "UNIVERSITARIO",
  "activo": true,
  "creadoAt": "2026-09-14T10:15:30.123",
  "actualizadoAt": "2026-09-14T10:15:30.123"
}
```

### Crear con datos inválidos

```bash
curl -i -X POST http://localhost:8080/api/evaluados \
  -H "Content-Type: application/json" \
  -d '{"nombre": "", "email": "no-es-email", "fechaNacimiento": "2999-01-01"}'
```

Respuesta `400 Bad Request`:

```json
{
  "timestamp": "2026-09-14T10:16:02.456",
  "status": 400,
  "error": "Bad Request",
  "message": "Error de validación",
  "path": "/api/evaluados",
  "errores": {
    "nombre": "El nombre es obligatorio",
    "apellido": "El apellido es obligatorio",
    "email": "El email debe tener un formato válido",
    "fechaNacimiento": "La fecha de nacimiento debe ser una fecha pasada",
    "genero": "El género es obligatorio",
    "nivelEducativo": "El nivel educativo es obligatorio",
    "activo": "El campo activo es obligatorio"
  }
}
```

## Pruebas

`src/test/java/com/psicometria/api/controllers/EvaluadoControllerTest.java` cubre el CRUD completo, las validaciones, el `404` y el `409`.

```bash
./mvnw test
```

> Los datos se guardan en memoria dentro de `EvaluadoService`; se pierden al reiniciar la aplicación.
