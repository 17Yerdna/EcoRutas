# Diseño de EcoRutas para el Examen de Unidad 2

## Intención

EcoRutas será la aplicación KMP/Compose Android del operario de EcoRecicla. Permitirá iniciar sesión, consultar la ruta del día desde una base local, registrar y corregir pesajes sin conexión, sincronizar al recuperar señal y aplicar la experiencia de usuario correspondiente a cada error del servicio.

## Objetivos

- Cumplir RF-A01 a RF-A07 y el modelo de dominio obligatorio sin eliminar ni cambiar tipos.
- Usar SQLDelight como única fuente de verdad observable para la interfaz.
- Mantener un único `HttpClient` Ktor inyectado con Koin y traducir todos los fallos a `ErrorApi`.
- Guardar el token mediante almacenamiento cifrado respaldado por Android Keystore.
- Soportar CRUD de pesajes, permisos, sincronización manual/automática y modo avión.

## No objetivos

- Ejecutar iOS, publicar en Play Store o implementar sincronización en segundo plano con WorkManager.
- Renovar tokens. Un 401 limpia sesión y vuelve al login.

## Enfoques considerados

1. **Base local como única fuente de verdad + cola de operación por fila (elegido).** Es el patrón exigido y permite distinguir crear, actualizar y anular sin perder el booleano `pendiente`.
2. Caché local solo para lectura. Se descarta porque registro y corrección deben funcionar offline.
3. Cola genérica JSON. Se descarta por añadir serialización y depuración innecesarias; las operaciones de pesaje son conocidas y caben en columnas tipadas.

## Tecnologías fijadas

- Kotlin 2.4.10, Compose Multiplatform 1.11.1, Android compile/target SDK 36 y min SDK 24.
- Ktor Client 3.1.3, Koin 4.0.2, SQLDelight 2.1.0 y kotlinx.serialization/coroutines.
- Una aplicación Android y módulo compartido KMP; el examen solo demuestra Android.

## Modelo común

Se implementan literalmente `PuntoAcopio`, `EstadoPesaje`, `Pesaje` y `Sesion` de la sección 4.5. `Pesaje` agrega `observacion: String` y `operacionPendiente: OperacionPendiente` sin modificar los campos obligatorios. `OperacionPendiente` es `NINGUNA`, `CREAR`, `ACTUALIZAR` o `ANULAR`.

La jerarquía `ErrorApi` distingue `Validacion`, `NoAutorizado`, `SinPrivilegio`, `NoEncontrado`, `Conflicto`, `Servidor`, `SinConexion` y `TiempoAgotado`.

## Esquema SQLDelight

- `ruta`: id, fecha, cerrada, ultimaSincronizacion.
- `punto_acopio`: campos exactos de `PuntoAcopio` más rutaId y motivoOmision opcional.
- `pesaje`: campos exactos de `Pesaje`, observacion, motivoAnulacion, pendiente y operacionPendiente.
- `sesion_metadata`: una fila para la última sincronización correcta.

Las consultas observables ordenan puntos por `secuencia` y pesajes por `fechaHora DESC`; buscan el siguiente ID provisional con `COALESCE(MIN(id), 0)` y Kotlin aplica `minOf(valor - 1, -1)`. Hay consultas para pendientes, reemplazo transaccional de ID negativo por remoto, conteo por punto, actualización y anulación lógica.

## Flujo offline-first

La UI solo observa `Flow` de SQLDelight. Al iniciar o sincronizar, la red escribe en la base y la UI cambia por emisión del flujo, nunca por asignar la respuesta HTTP al ViewModel.

- Crear offline: ID negativo, `pendiente = true`, operación `CREAR`.
- Corregir creación aún no enviada: actualiza la misma fila y conserva `CREAR`.
- Corregir fila remota: conserva ID positivo y marca `ACTUALIZAR`.
- Anular una creación con ID negativo cancela la creación local y elimina esa fila, porque aún no existe en el servidor. Anular una fila remota marca estado local y operación `ANULAR`.
- Sincronizar: con `Mutex`, sube `CREAR`, luego `ACTUALIZAR` y `ANULAR`; después descarga ruta, puntos y pesajes; finalmente registra la hora y el resumen.
- Un 400/409 deja la fila editable y pendiente con el mensaje; una caída de red conserva toda la cola.
- El monitor de conectividad Android solo dispara el intento automático y muestra estado; la petición real decide si el servicio está disponible.

## Cliente, sesión y seguridad

`ClienteHttp` instala `ContentNegotiation`, `DefaultRequest`, `Auth bearer`, `HttpTimeout` y `Logging`; sanitiza `Authorization` y apaga logging en release. La base URL es `http://10.0.2.2:8080/api/v1/` en emulador y configurable para dispositivo físico.

`AlmacenSeguroAndroid` guarda token y metadatos de expiración cifrados. Al abrir, la app restaura sesión si sigue vigente; si venció, limpia y muestra login. Tras login invalida el caché bearer. Toda fila local se asocia al ID del usuario para que otro turno no observe ni envíe pendientes ajenos. El logout sincroniza primero si hay red; si quedan pendientes, devuelve `PendientesSinSincronizar(cantidad)` y exige cancelar la salida o confirmar su descarte. Al cerrar, borra almacén, caché bearer y estado de sesión. Un 401 conserva pendientes identificados por usuario y exige reautenticación antes de enviarlos.

## Pantallas y estados

- Login: carga, credenciales incorrectas y sin conexión diferenciados.
- Ruta del día: puntos locales en secuencia, atendidos/pendientes, buscador, última sincronización, carga inicial, contenido, vacío y error.
- Detalle: punto, historial de pesajes y acciones según privilegio.
- Omisión: el operario puede registrar un motivo para un punto no pesado; el cierre de ruta usa pesaje u omisión según RN-04.
- Formulario: bruto, tara, material y observación; muestra errores del servidor bajo el campo.
- Perfil: usuario, rol, privilegios y cierre de sesión.
- Sincronización: progreso y resumen de enviados/recibidos; automática al recuperar conexión y manual por botón.

La respuesta 401 cierra sesión; 403 conserva sesión; 404 informa, refresca y no ofrece reintento; 409 muestra el motivo; 500 ofrece reintento. Acciones no permitidas no se dibujan, pero el servicio sigue siendo la barrera de seguridad.

## Pruebas obligatorias

`commonTest` usa `MockEngine` y `JdbcSqliteDriver.IN_MEMORY`, con base nueva por prueba. Como mínimo cubre:

1. DTO a dominio.
2. Traducción de error 400/401/403/404/409/500, conexión y timeout.
3. Registro sin red con ID negativo y pendiente.
4. Orden de sincronización: subir antes de bajar.
5. Sincronización con red y emisión del Flow.
6. Envío al reconectar.
7. Bajada que conserva pendientes.
8. IDs locales/remotos sin colisión.
9. Doble sincronización sin duplicados.
10. Corrección offline que se envía como PUT y no como POST.

## Trazabilidad

El repositorio usa `main`, `develop` y ramas `feature/base-kmp`, `feature/cliente-jwt`, `feature/sqldelight-offline`, `feature/ui-ruta-pesajes` y `feature/pruebas-entrega`. Cada rama tiene issue, commits atómicos, PR a `develop` y fusión `--no-ff`. Al cierre se fusiona `develop` a `main` y se etiqueta `v1.0-unidad2`.

