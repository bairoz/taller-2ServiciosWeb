# Evidencias de admins — ElAiwan

Pruebas HTTP reales realizadas el **22 de septiembre de 2026** sobre la rama
`feature/admins`, código `c311f493d21d0ec212125f811ea3a741a59fea83`.

**Resultado: 10 de 10 casos HTTP correctos y 53 pruebas automáticas aprobadas.**

## Capturas

Las imágenes son capturas de un informe que muestra las peticiones y respuestas
reales guardadas en [resultados-http.json](resultados-http.json). No son capturas
de Postman. Los cuerpos originales y las cabeceras se conservan en el JSON;
el informe solo aplica formato para facilitar su lectura.

| # | Caso | Estado | Captura |
|---|---|---|---|
| 1 | Crear administrador | 201 | [POST crear](01-post-crear.png) |
| 2 | Listar administradores | 200 | [GET listar](02-get-listar.png) |
| 3 | Consultar por ID | 200 | [GET por ID](03-get-por-id.png) |
| 4 | Actualizar rol, estado e intentos | 200 | [PUT actualizar](04-put-actualizar.png) |
| 5 | Nombre vacío, email y contraseña inválidos, intentos negativos | 400 | [POST inválido](05-post-invalido.png) |
| 6 | Último acceso en el futuro | 400 | [PUT inválido](06-put-invalido.png) |
| 7 | Usuario duplicado, escrito en mayúsculas | 409 | [Usuario duplicado](07-post-usuario-duplicado.png) |
| 8 | Email duplicado, escrito en mayúsculas | 409 | [Email duplicado](08-post-email-duplicado.png) |
| 9 | Eliminar el administrador creado | 204 | [DELETE eliminar](09-delete-eliminar.png) |
| 10 | Consultar el ID después de eliminarlo | 404 | [GET inexistente](10-get-inexistente.png) |

También se verificaron la cabecera `Location`, la ausencia de `password` en las
respuestas, la lista como arreglo JSON y la persistencia de la actualización
mediante una consulta posterior. La base quedó con cero admins al finalizar.

## Archivos complementarios

- [Informe completo](informe.html): descargar y abrir en un navegador.
- [Resultados HTTP originales](resultados-http.json): incluye fecha UTC, URL,
  método, petición, respuesta, cabeceras y estado esperado/recibido.
- [Resultados de pruebas automáticas](resultados-tests.txt): informes de Maven
  Surefire; 39 pruebas de controller y 14 de service, sin fallos ni errores.
- [Script de peticiones y comprobaciones](ejecutar-pruebas.mjs).

## Entorno utilizado

- Spring Boot 4.1.1, JDK 24; compilación para Java 21.
- PostgreSQL **17.4**, instalado localmente; no se verificó PostgreSQL 18 en esta ejecución.
- Base aislada `admins_evidencias`, creada con `database/02_esquema.sql` de esta rama.
- API en `http://127.0.0.1:18080`; PostgreSQL en `127.0.0.1:55439`.
- Datos y contraseñas ficticios, exclusivos de esta prueba.
- Se probó la rama individual `feature/admins`, no el conjunto actual de `main`.

## Repetir las peticiones

Con Node.js 18 o posterior, la API de esta rama iniciada y una base de pruebas
vacía configurada según el README principal, ejecutar desde la raíz del proyecto:

```powershell
$env:ADMINS_BASE_URL = 'http://localhost:8080'
node evidencias/admins/ejecutar-pruebas.mjs
```

El script comprueba los resultados, actualiza `resultados-http.json` y elimina
únicamente el admin que él mismo creó. Los PNG y el HTML conservan la captura
del 22 de septiembre; el script no los regenera.

Para repetir las pruebas automáticas:

```powershell
.\mvnw.cmd test
```
