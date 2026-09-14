# Entidad: Admins

Ruta base: `/api/admins` — Tabla: `admins` — Rama: `feature/admins`.

## Archivos

| Capa | Archivo | Responsabilidad |
|---|---|---|
| Model | `models/Admin.java` | Entidad JPA que coincide con el esquema SQL |
| Model | `models/RolAdmin.java` | Roles `SUPER_ADMIN`, `ADMIN` y `LECTOR` |
| Repository | `repositories/AdminRepository.java` | Persistencia y consultas de duplicados |
| DTO | `dto/AdminDTO.java` | Validación de entrada y campos de respuesta |
| Service | `services/AdminService.java` | CRUD, normalización y unicidad |
| Controller | `controllers/AdminController.java` | Endpoints REST y validación con `@Valid` |

Los archivos Java están dentro de `src/main/java/com/psicometria/api/`.

## Campos de entrada

POST y PUT reciben el objeto completo. `ultimoAcceso` puede omitirse o ser `null`; los demás campos de entrada son obligatorios.

| Campo | Tipo | Validación |
|---|---|---|
| `nombre` | String | 2–100 caracteres, no vacío; letras, espacios, apóstrofes o guiones |
| `apellido` | String | 2–100 caracteres, no vacío; letras, espacios, apóstrofes o guiones |
| `usuario` | String | No vacío, máximo 50 caracteres, único |
| `email` | String | No vacío, email válido, máximo 255 caracteres, único |
| `password` | String | No vacío, 8–100 caracteres, solo entrada |
| `rol` | RolAdmin | `SUPER_ADMIN`, `ADMIN` o `LECTOR` |
| `activo` | Boolean | `true` o `false` |
| `intentosFallidos` | Integer | Mayor o igual a cero |
| `ultimoAcceso` | OffsetDateTime | Opcional; fecha ISO 8601 con zona, pasada o presente |

- `usuario` y `email` se guardan en minúsculas, sin espacios al inicio o al final; se comprueban duplicados sin distinguir mayúsculas.
- `nombre` y `apellido` se guardan sin espacios exteriores.
- `password` se conserva exactamente como se recibe, incluido cualquier espacio. Se almacena en texto plano porque así lo exige el esquema del taller, y se excluye de todas las respuestas.
- En PUT se permite conservar el usuario y email del mismo admin; se rechazan los de otro registro. La contraseña también es obligatoria en PUT.
- `id`, `creadoAt` y `actualizadoAt` son de solo lectura: el servidor los asigna e ignora valores enviados por el cliente.
- El rol se mapea al ENUM PostgreSQL `rol_admin`. Las fechas se mapean a `TIMESTAMPTZ`.
- Este módulo implementa el CRUD del taller; no incluye autenticación ni lógica automática de intentos de login.

## Endpoints

| Método | Ruta | Respuesta exitosa |
|---|---|---|
| GET | `/api/admins` | `200 OK`, lista ordenada por ID; `[]` si no hay registros |
| GET | `/api/admins/{id}` | `200 OK` |
| POST | `/api/admins` | `201 Created` y cabecera `Location` |
| PUT | `/api/admins/{id}` | `200 OK` |
| DELETE | `/api/admins/{id}` | `204 No Content` |

Errores del manejador compartido:

- `400`: validación, JSON incorrecto, enum o fecha inválidos, ID no numérico.
- `404`: admin inexistente al consultar, actualizar o eliminar.
- `409`: usuario/email duplicados o violación de una restricción de la base de datos.

## Ejemplo de creación

`POST http://localhost:8080/api/admins`

```json
{
  "nombre": "Ana",
  "apellido": "Pérez",
  "usuario": "admin.taller",
  "email": "admin.taller@example.com",
  "password": "Clave123",
  "rol": "ADMIN",
  "activo": true,
  "intentosFallidos": 0,
  "ultimoAcceso": null
}
```

Respuesta de ejemplo, `201 Created`, con `Location: http://localhost:8080/api/admins/1`:

```json
{
  "id": 1,
  "nombre": "Ana",
  "apellido": "Pérez",
  "usuario": "admin.taller",
  "email": "admin.taller@example.com",
  "rol": "ADMIN",
  "activo": true,
  "intentosFallidos": 0,
  "ultimoAcceso": null,
  "creadoAt": "2026-09-14T10:30:00-06:00",
  "actualizadoAt": "2026-09-14T10:30:00-06:00"
}
```

El ID y las fechas reales dependen de la base de datos. Las peticiones de creación, consulta, actualización, validación, conflicto y eliminación están en [admins.http](admins.http).

## Ejecutar y probar

Para usar la API con datos reales, crear la base y configurar la conexión siguiendo el README del proyecto. Se requiere Java 21 o posterior.

En Windows:

```powershell
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run
```

En Linux/macOS:

```bash
./mvnw test
./mvnw spring-boot:run
```

Las pruebas no necesitan PostgreSQL:

- `AdminControllerTest`: endpoints, validaciones, códigos HTTP, protección del ID y fechas de auditoría, recepción de contraseña y exclusión de la contraseña en las respuestas.
- `AdminServiceTest`: normalización, persistencia, duplicados en creación/actualización, conservación del ID, campos de admins, consultas y eliminación.

Las pruebas usan `@WebMvcTest` y Mockito, como la rama de referencia `feature/evaluados`. No sustituyen una prueba de integración contra PostgreSQL.
