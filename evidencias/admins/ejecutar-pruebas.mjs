import { writeFile } from 'node:fs/promises';
import { fileURLToPath } from 'node:url';
import assert from 'node:assert/strict';

// Ejecutar contra una base de prueba vacía. Solo elimina el admin creado aquí.
const base = process.env.ADMINS_BASE_URL || 'http://127.0.0.1:18080';
const casos = [];
const datos = {
  nombre: 'Ana', apellido: 'Perez', usuario: 'admin.evidencia',
  email: 'admin.evidencia@example.com', password: 'DemoClave123',
  rol: 'ADMIN', activo: true, intentosFallidos: 0, ultimoAcceso: null
};
let id;
let eliminado = false;

async function peticion(archivo, caso, metodo, ruta, esperado, enviado) {
  const url = base + ruta;
  const fecha = new Date().toISOString();
  const solicitud = enviado === undefined ? null : JSON.stringify(enviado, null, 2);
  const res = await fetch(url, {
    method: metodo,
    headers: enviado === undefined ? {} : { 'Content-Type': 'application/json' },
    body: solicitud,
    signal: AbortSignal.timeout(15000)
  });
  const texto = await res.text();
  const respuesta = texto ? JSON.parse(texto) : null;
  casos.push({ archivo, caso, fecha, metodo, url, enviado: enviado ?? null,
    solicitud, esperado, status: res.status, estadoHttp: res.statusText,
    cabeceras: Object.fromEntries(res.headers), respuesta, respuestaTexto: texto });
  assert.equal(res.status, esperado, `${caso}: ${texto}`);
  console.log(`${archivo}: ${res.status} OK`);
  return respuesta;
}

try {
  const creado = await peticion('01-post-crear', 'Crear administrador', 'POST', '/api/admins', 201, datos);
  id = creado.id;
  assert.ok(Number.isSafeInteger(id));
  assert.equal(creado.usuario, datos.usuario);
  assert.ok(!('password' in creado));
  assert.ok(casos[0].cabeceras.location.endsWith(`/api/admins/${id}`));

  const lista = await peticion('02-get-listar', 'Listar administradores', 'GET', '/api/admins', 200);
  assert.ok(Array.isArray(lista));
  assert.ok(lista.some(a => a.id === id));
  assert.ok(lista.every(a => !('password' in a)));

  const obtenido = await peticion('03-get-por-id', 'Consultar administrador por ID', 'GET', `/api/admins/${id}`, 200);
  assert.equal(obtenido.email, datos.email);
  assert.ok(!('password' in obtenido));

  const cambios = { ...datos, rol: 'LECTOR', activo: false, intentosFallidos: 2,
    ultimoAcceso: '2025-01-15T10:30:00-06:00', password: 'NuevaDemo123' };
  const actualizado = await peticion('04-put-actualizar', 'Actualizar rol y estado', 'PUT', `/api/admins/${id}`, 200, cambios);
  assert.equal(actualizado.rol, 'LECTOR');
  assert.equal(actualizado.activo, false);
  assert.equal(actualizado.intentosFallidos, 2);
  assert.ok(!('password' in actualizado));
  const persistido = await fetch(`${base}/api/admins/${id}`).then(r => r.json());
  assert.equal(persistido.rol, 'LECTOR');
  assert.equal(persistido.activo, false);

  const invalido = await peticion('05-post-invalido', 'Rechazar datos inválidos', 'POST', '/api/admins', 400,
    { ...datos, nombre: '', email: 'correo-invalido', password: 'corta', intentosFallidos: -1 });
  for (const campo of ['nombre', 'email', 'password', 'intentosFallidos']) assert.ok(invalido.errores[campo]);

  const putInvalido = await peticion('06-put-invalido', 'Rechazar actualización con fecha futura', 'PUT', `/api/admins/${id}`, 400,
    { ...cambios, ultimoAcceso: '2999-01-01T00:00:00Z' });
  assert.ok(putInvalido.errores.ultimoAcceso);

  const usuario = await peticion('07-post-usuario-duplicado', 'Rechazar usuario duplicado', 'POST', '/api/admins', 409,
    { ...datos, usuario: 'ADMIN.EVIDENCIA', email: 'otro.admin@example.com' });
  assert.match(usuario.message, /usuario/);

  const email = await peticion('08-post-email-duplicado', 'Rechazar email duplicado', 'POST', '/api/admins', 409,
    { ...datos, usuario: 'otro.admin', email: 'ADMIN.EVIDENCIA@example.com' });
  assert.match(email.message, /email/);

  await peticion('09-delete-eliminar', 'Eliminar administrador de prueba', 'DELETE', `/api/admins/${id}`, 204);
  eliminado = true;
  await peticion('10-get-inexistente', 'Consultar el administrador eliminado', 'GET', `/api/admins/${id}`, 404);
} finally {
  await writeFile(fileURLToPath(new URL('resultados-http.json', import.meta.url)), JSON.stringify(casos, null, 2) + '\n');
  if (id && !eliminado) {
    await fetch(`${base}/api/admins/${id}`, { method: 'DELETE' });
  }
}

console.log('10 casos HTTP verificados; registro de prueba eliminado.');
