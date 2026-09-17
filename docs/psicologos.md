# API REST de Tests Psicométricos — Psicólogos

Módulo del Taller #2, rama `feature/psicologos`. Administra profesionales mediante `/api/psicologos`.

## Ejecución

Requisitos del proyecto: Java 21 o posterior, Maven Wrapper incluido y PostgreSQL con los scripts del repositorio ejecutados.

1. Conectado a la base `postgres`, ejecutar `database/01_crear_base_datos.sql` una sola vez.
2. Conectado a `psicometria_db`, ejecutar `database/02_esquema.sql`. Los datos de `03_datos_prueba.sql` son opcionales.
3. Copiar `application-local.properties.example` a `application-local.properties` y configurar las credenciales locales. Git ignora ese archivo.
4. En Windows ejecutar `.\mvnw.cmd spring-boot:run` desde la raíz. La API utiliza el puerto 8080.
5. Ejecutar `.\mvnw.cmd test` para las pruebas automatizadas, que no necesitan PostgreSQL.

### Arranque alternativo en Windows

Si Tomcat falla con `Unable to establish loopback connection` y `UnixDomainSockets.connect`, se verificó este arranque en el equipo local con Java 25 y PostgreSQL 16.4:

```powershell
New-Item -ItemType Directory -Force target/sockets | Out-Null
.\mvnw.cmd spring-boot:run '-Dspring-boot.run.jvmArguments=-Djdk.net.unixdomain.tmpdir=target/sockets'
```

La opción cambia la carpeta temporal de los sockets de Java para esa ejecución. No modifica las credenciales ni la configuración compartida. Referencia: [propiedades de red de Java](https://docs.oracle.com/en/java/javase/17/core/java-networking.html).

## Endpoints

| Método | Ruta | Éxito | Errores previstos |
|---|---|---|---|
| GET | `/api/psicologos` | 200, arreglo JSON | — |
| GET | `/api/psicologos/{id}` | 200, psicólogo | 400, 404 |
| POST | `/api/psicologos` | 201, psicólogo y cabecera Location | 400, 409 |
| PUT | `/api/psicologos/{id}` | 200, psicólogo actualizado | 400, 404, 409 |
| DELETE | `/api/psicologos/{id}` | 200, objeto con id y mensaje | 400, 404 |

PUT reemplaza todos los campos editables. El servidor asigna `id`, `creadoAt` y `actualizadoAt`; el DTO los trata como solo lectura.

## Campos y validaciones

| Campo | Tipo JSON | Validación |
|---|---|---|
| nombre | texto | Obligatorio, entre 2 y 100 caracteres |
| apellido | texto | Obligatorio, entre 2 y 100 caracteres |
| email | texto | Obligatorio, formato email, máximo 255, único |
| numeroRegistro | texto | Obligatorio, máximo 30, único |
| especialidad | texto/enum | EDUCACIONAL, CLINICA, ORGANIZACIONAL, NEUROPSICOLOGIA o VOCACIONAL |
| aniosExperiencia | entero | Obligatorio, entre 0 y 60 inclusive |
| fechaTitulacion | fecha ISO yyyy-MM-dd | Obligatoria, hoy o una fecha pasada |
| disponible | booleano | Obligatorio; admite true y false |

El servicio elimina espacios de los extremos de nombre, apellido, email y registro. Normaliza el email a minúsculas y el registro a mayúsculas. Comprueba duplicados sin distinguir mayúsculas; en PUT excluye el ID actual. Los duplicados producen 409, mientras que los datos que incumplen las validaciones producen 400.

El manejador global existente devuelve `timestamp`, `status`, `error`, `message`, `path` y, para errores de validación, `errores` con el detalle por campo.

## Ocho pruebas manuales y evidencias

Abrir `docs/psicologos.http` en el cliente HTTP de IntelliJ y ejecutar las peticiones en orden. El POST guarda automáticamente `psicologoId`. En otro cliente se debe copiar el ID de la respuesta del POST. Los casos solo actualizan y eliminan el registro creado para esta secuencia.

| Caso | Resultado esperado | Captura que se debe guardar en evidencias/psicologos/ |
|---|---|---|
| 01 Crear | 201 | 01-post-crear.png |
| 02 Listar | 200 | 02-get-listar.png |
| 03 Consultar ID | 200 | 03-get-por-id.png |
| 04 Actualizar | 200 | 04-put-actualizar.png |
| 05 POST inválido | 400 | 05-post-invalido.png |
| 06 PUT inválido | 400 | 06-put-invalido.png |
| 07 Eliminar | 200 | 07-delete-eliminar.png |
| 08 Consultar eliminado | 404 | 08-get-inexistente.png |

Cada captura debe mostrar método, URL, cuerpo enviado cuando corresponda, estado HTTP y respuesta JSON. Las capturas deben obtenerse de ejecuciones reales; el archivo HTTP por sí solo no es evidencia de ejecución.

Se ejecutaron las ocho peticiones contra la API real y PostgreSQL mediante PowerShell. El registro de método, URL, cuerpo, estado y respuesta está en `evidencias/psicologos/resultados-http.json`; los ocho estados coincidieron con los esperados. Este registro complementa las pruebas, pero aún deben obtenerse las capturas solicitadas desde Postman, Insomnia o IntelliJ.

Si se interrumpe la secuencia después de crear el registro, retomar con su ID o cambiar el email y número de registro antes de crear otro. Una ejecución completa elimina el registro y permite repetir la secuencia.

## Recorrido de una solicitud para la defensa

1. Spring dirige la URL y el método HTTP al método de `PsicologoController`.
2. En POST y PUT convierte el JSON a `PsicologoDTO` y ejecuta sus anotaciones mediante `@Valid`.
3. `PsicologoService` comprueba existencia y duplicados, normaliza los datos y convierte DTO y entidad.
4. `PsicologoRepository` utiliza JPA para consultar y guardar en PostgreSQL.
5. El controlador construye un `ResponseEntity` y Spring serializa el resultado a JSON. Si hay errores, `GlobalExceptionHandler` prepara su respuesta JSON.

El enum JPA utiliza `especialidad_psicologo` de PostgreSQL. El ID se genera en la base y las fechas de auditoría usan `OffsetDateTime`. La FK existente en `tests` usa `ON DELETE SET NULL`: eliminar un psicólogo conserva sus tests y deja su autor en NULL.

## Integración de la entrega

Por las reglas del repositorio, esta rama documenta el módulo aquí sin editar el README compartido. Al integrar en main, el equipo debe añadir al README un enlace a este documento y la tabla de endpoints para cumplir el formato de entrega del profesor. También deben incluirse las ocho capturas reales antes de considerar completa la entrega.
