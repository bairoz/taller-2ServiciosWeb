# Propuesta de arquitectura — API de tests psicométricos

**Taller #1 · Primer corte evaluativo · 25 puntos**

Propuesta alineada con el diagrama editado `arquitectura.drawio`. Retoma el sistema de tests del semestre anterior y plantea una interfaz web para estudiantes, psicólogos y administrador, con Postman como cliente de pruebas. La interfaz anterior quedó incompleta; esta propuesta describe su integración con la API.

## Proyecto y problema

El proyecto actual administra tests psicométricos para estudiantes de una institución: evaluados, profesionales psicólogos, tests, preguntas y administradores. La propuesta organiza estas funcionalidades mediante una API REST, para que un cliente pueda consultar y administrar los datos sin acceder directamente a PostgreSQL.

El cliente representado en el diagrama es una interfaz web dirigida a estudiantes, psicólogos y administrador. Se propone retomar las pantallas del semestre anterior y conectarlas a la API. Postman es un cliente adicional para probar las peticiones y respuestas. Los roles describen los usuarios previstos de la interfaz; no implican que el backend actual ya implemente permisos por rol. La tecnología de la interfaz no se especifica en el diagrama.

## Arquitectura y responsabilidades

Se propone una aplicación Spring Boot organizada en capas, desplegada como una única API. Los módulos de las entidades comparten la base de datos y el manejo de errores; no son microservicios independientes.

| Bloque del diagrama | Responsabilidad | Correspondencia en el proyecto |
|---|---|---|
| Interfaz web | Capturar datos, enviar solicitudes y mostrar resultados a estudiantes, psicólogos y administrador | Cliente de la solución; Postman como cliente de pruebas |
| Presentación / Controllers + DTO | Definir rutas HTTP, recibir datos, activar validaciones y devolver el contrato JSON con su estado HTTP | `controllers`, `dto`, `@Valid`, `ResponseEntity` |
| Aplicación / Services | Coordinar CRUD y transacciones y convertir entre DTO y entidad; integra la lógica de negocio para duplicados y otras reglas | `services`, `@Service`, `@Transactional` |
| Acceso a datos / Repositories + Models | Buscar, guardar y eliminar mediante repositorios; representar los datos mediante entidades JPA y enumeraciones | `repositories`, `JpaRepository`, `models`, `@Entity` |
| Infraestructura de persistencia | Traducir las operaciones de persistencia, comunicarse con PostgreSQL y administrar el pool de conexiones | Hibernate/JPA, driver JDBC PostgreSQL, HikariCP |
| Soporte transversal | Atender HTTP, configurar conexión y credenciales locales y convertir excepciones en errores JSON 400, 404 y 409 | Tomcat integrado, configuración local, `exceptions`, `@RestControllerAdvice` |
| Base de datos / PostgreSQL | Almacenar evaluados, psicólogos, tests, preguntas y admins; garantizar claves, relaciones y restricciones | PostgreSQL y scripts en `database/` |

El bloque de presentación agrupa controladores y DTOs; el de acceso a datos muestra repositorios y modelos. Estas agrupaciones explican su colaboración, aunque el código los organiza en paquetes diferentes. La lógica de negocio está integrada en los servicios, tal como indica el diagrama.

La infraestructura de persistencia aparece en el recorrido hacia PostgreSQL. El soporte transversal se representa a un lado porque da servicio al conjunto de la API; no es un paso adicional que deba recorrer cada petición después de acceder a la base.

## Diagrama

Abrir [arquitectura.drawio](arquitectura.drawio) en Draw.io mediante la opción de abrir un archivo existente. Es editable y contiene el cliente, el límite de la API, sus responsabilidades internas, infraestructura, PostgreSQL y flujos de solicitud y respuesta. Exportar desde la herramienta a PDF o PNG para la entrega si la plataforma lo requiere.

Las flechas azules descendentes representan la solicitud y las verdes discontinuas ascendentes representan el retorno de resultados. El cliente no se conecta directamente a la base de datos.

**Solicitud:** Interfaz web / Postman → HTTP/HTTPS y JSON → Controllers + DTO → DTO validado → Services → operación de datos → Repositories → persistencia JPA → infraestructura de persistencia → SQL vía JDBC/TCP → PostgreSQL.

**Respuesta:** PostgreSQL → filas / resultado SQL → infraestructura de persistencia → objetos mapeados → Repositories → entidad / resultado → Services → DTO de respuesta → Controller → estado HTTP y JSON → cliente.

## Comunicación

- **Cliente → API:** HTTP en desarrollo (`localhost:8080`); HTTPS propuesto para despliegue. JSON en las peticiones que necesitan cuerpo, con `Content-Type: application/json`.
- **Dentro de la API:** llamadas a métodos Java y objetos DTO/entidades; no son peticiones HTTP entre capas.
- **API → PostgreSQL:** Hibernate utiliza JDBC y el driver PostgreSQL sobre una conexión TCP, normalmente al puerto 5432. Se ejecuta SQL; la base no recibe los cuerpos JSON de la API como si fueran endpoints.
- **API → cliente:** código HTTP y JSON cuando corresponde. El controlador devuelve DTOs o colecciones y Spring los serializa. Los errores de validación incluyen mensaje y detalle por campo.

HTTPS describe el despliegue propuesto; el arranque local revisado utiliza HTTP. La administración de usuarios por CRUD no equivale a autenticación o autorización: no se presenta un inicio de sesión como funcionalidad implementada.

## Servicios REST

Estas funcionalidades están sustentadas por el modelo y los controladores actuales. El equipo debe confirmar cuáles proceden del sistema anterior; el README indica que psicólogos fue agregado al modelo del taller.

| Recurso o funcionalidad | Método | Endpoint | Descripción |
|---|---|---|---|
| Consultar evaluados | GET | `/api/evaluados` | Listar estudiantes registrados |
| Registrar evaluado | POST | `/api/evaluados` | Guardar los datos de un estudiante |
| Consultar tests | GET | `/api/tests` | Listar instrumentos psicométricos |
| Crear test | POST | `/api/tests` | Registrar título, categoría y configuración del test |
| Consultar preguntas | GET | `/api/preguntas` | Listar preguntas registradas |
| Registrar pregunta | POST | `/api/preguntas` | Asociar una pregunta a un test existente |
| Actualizar pregunta | PUT | `/api/preguntas/{id}` | Modificar una pregunta existente |
| Consultar psicólogos | GET | `/api/psicologos` | Listar profesionales registrados |
| Registrar psicólogo | POST | `/api/psicologos` | Crear un profesional con email y registro únicos |
| Eliminar psicólogo | DELETE | `/api/psicologos/{id}` | Eliminar el profesional indicado |

Evaluados, tests y preguntas aportan tres funcionalidades distintas para la propuesta. Las tablas de tokens y aplicaciones de tests son soporte del modelo; no se afirma que exista un servicio para rendir o calificar tests en la implementación actual.

## Ejemplo de recorrido completo

1. El cliente envía `POST /api/psicologos` con datos JSON.
2. El controlador recibe el DTO y `@Valid` comprueba sus restricciones. Un error de formato o rango produce 400 sin invocar el servicio.
3. El servicio coordina la creación, normaliza datos y comprueba duplicados. Un duplicado produce 409.
4. El repositorio solicita guardar la entidad a la infraestructura de persistencia. Hibernate realiza el mapeo y genera SQL; el driver JDBC lo envía a PostgreSQL usando una conexión administrada por HikariCP.
5. PostgreSQL persiste el registro y genera su ID. La infraestructura procesa el resultado y lo mapea a objetos; el repositorio devuelve la entidad al servicio.
6. El servicio devuelve un DTO al controlador, que responde `201 Created` con JSON y la cabecera Location.

En una consulta de un ID inexistente, el servicio lanza la excepción correspondiente y el manejador global devuelve 404 con un mensaje JSON.

## Justificación

Separar responsabilidades permite modificar la presentación sin reescribir el acceso a datos, reutilizar reglas desde distintos controladores y probar servicios sin una base real mediante mocks. La API establece un contrato que puede consumir más de un cliente. Una única aplicación por capas resulta coherente con el alcance del taller y con la implementación existente, sin introducir despliegues independientes para cada entidad.

## Guion grupal sugerido — 6 a 8 minutos

Distribución sugerida para cinco integrantes; ajustar al tamaño real del equipo.

1. **Proyecto y cliente (1 minuto).** “Retomamos nuestro sistema de tests. El diagrama propone una interfaz web para estudiantes, psicólogos y administrador; la interfaz anterior quedó incompleta. La propuesta es organizar la gestión de estudiantes, tests y preguntas mediante una API REST que pueda consumir esa interfaz; para probar la API usamos Postman”. Señalar cliente y API en el diagrama.
2. **Controladores y contrato (1 minuto).** “El cliente envía HTTP con JSON. El controlador identifica la operación, recibe un DTO validado y devuelve el estado HTTP y la respuesta”. Mostrar un GET y un POST de la tabla.
3. **Servicios y reglas (1 a 2 minutos).** “El servicio coordina el caso de uso. La lógica de negocio comprueba reglas como no repetir el registro de un psicólogo. En este proyecto ambas responsabilidades están implementadas en services”. Explicar la diferencia entre validación de formato y comprobación de duplicados.
4. **Datos e infraestructura (1 a 2 minutos).** “El repositorio accede a datos con JPA. Hibernate, el driver y el pool de conexiones permiten ejecutar SQL en PostgreSQL. No enviamos HTTP desde Repository a la base”. Señalar las flechas de ida y regreso y el bloque lateral de soporte: Tomcat atiende HTTP, la configuración establece la conexión y el manejador global produce errores JSON.
5. **Flujo y justificación (1 a 2 minutos).** Narrar el ejemplo de creación completo, mostrar tres funcionalidades y explicar por qué se separan capas. Cerrar indicando que el taller es una propuesta de arquitectura y el CRUD actual ya materializa parte de ella.

### Tu intervención: lógica de negocio y psicólogos

“Mi módulo permite administrar psicólogos. Cuando llega una solicitud de creación, el DTO comprueba datos como email válido, experiencia entre cero y sesenta y una fecha de titulación no futura. Después el servicio comprueba que el email y el número de registro no estén repetidos. Si todo es válido, utiliza el repositorio para guardar el profesional y devuelve los datos al controlador. Esta organización permite mantener las reglas separadas de las rutas HTTP y probarlas de forma independiente”.

## Preguntas para la defensa

- **¿Quién consume la API?** La interfaz web para estudiantes, psicólogos y administrador; Postman actúa como cliente de pruebas.
- **¿Por qué DTO y entidad son distintos?** El DTO define el contrato de intercambio; la entidad mapea la persistencia.
- **¿Dónde está la lógica de negocio?** En las clases de servicio actuales; no se creó una capa física independiente.
- **¿La base devuelve JSON directamente al navegador?** No. El resultado se convierte en objetos y DTOs dentro de la API, y Spring genera la respuesta JSON.
- **¿Por qué una API por capas?** Para separar responsabilidades, facilitar mantenimiento y pruebas y permitir distintos clientes.
- **¿Qué pasa si el dato no es válido o no existe?** Se devuelve 400 o 404, respectivamente, con información de error; un duplicado devuelve 409.

## Antes de entregar

- Confirmar el nombre formal del proyecto y la tecnología de la interfaz anterior. En el bloque del cliente del diagrama, corregir el texto «Administrado» a «Administrador» para que el rótulo coincida con esta propuesta.
- Revisar el diagrama entre todos y exportarlo desde Draw.io si necesitan una imagen o PDF.
- Incluir la tabla de servicios con al menos tres funcionalidades del proyecto anterior confirmadas.
- Distribuir la exposición y practicar el flujo de solicitud y respuesta con todos los integrantes.
