# EcoRutas — EcoRecicla

Aplicación KMP Android para registrar pesajes incluso sin conexión. La UI observa SQLDelight; Ktor sincroniza con EcoAPI. Compose Multiplatform, Kotlin2.4.10, SQLDelight2.1.0, Ktor3.1.3 y Koin4.0.2.

## Compilar y ejecutar

Requisitos: Android Studio/SDK36, JDK25 para el daemon Gradle y EcoAPI activo en localhost:8080. En este equipo se usa el JBR25 de Android Studio; gradle-daemon-jvm.properties también permite aprovisionarlo automáticamente.

```powershell
.\gradlew.bat :shared:testAndroidHostTest :composeApp:assembleDebug
```

APK: `composeApp/build/outputs/apk/debug/composeApp-debug.apk`. En emulador usa `http://10.0.2.2:8080/api/v1`. HTTP está habilitado únicamente en variante debug para el servidor local; release necesita HTTPS y una URL apropiada.

Usuarios: `operario1`, `supervisor1`, `admin` y `auditor1`, contraseña de demostración `Eco2026*`. AUDITOR dispone solo de lectura.

## Flujo

1. Ingresar con red para descargar la ruta del día y pesajes.
2. Abrir un punto y registrar un pesaje. Sin red se guarda con ID negativo y marca pendiente.
3. Sincronizar manualmente o recuperar la conexión: altas → correcciones → anulaciones → descarga.
4. La respuesta sustituye el ID negativo por el ID servidor dentro de una transacción local.
5. Perfil muestra rol/privilegios. Cerrar sesión sincroniza primero; si hay trabajo pendiente ofrece conservarlo o descartarlo explícitamente.

El token y la sesión se cifran con AES/GCM y Android Keystore. Las tablas locales están separadas por usuario. Un 401 limpia la sesión activa y conserva pendientes para el mismo usuario; un 403 mantiene sesión. Los campos 400 se muestran bajo el formulario y 409 conserva el motivo del servidor.

## Pruebas y arquitectura

`commonTest` prueba conversiones DTO, errores HTTP, registro offline, IDs negativos, reconexión, compactación de corrección, orden de sincronización y ausencia de duplicados. `androidHostTest` usa SQLite real en memoria para aislamiento de usuarios e IDs con tabla vacía.

`domain/model` conserva los contratos exigidos. `data/remote` centraliza bearer y errores. `data/local` guarda SQLDelight/Flow. `data/repository` serializa la cola con Mutex. `presentation/EcoViewModel` coordina estado; las pantallas de `App.kt` solo consumen ese estado local. Koin ensambla las dependencias Android.

Para evidenciar offline: iniciar sesión y descargar datos, activar modo avión, registrar, observar pendiente/ID negativo, recuperar red y comprobar resumen e ID positivo. El neto definitivo siempre lo calcula EcoAPI.

## Continuidad

Planes en `docs/superpowers`. En la carpeta padre: `CONTEXTO_PARA_IA.md`, `tasks/plan.md`, `tasks/todo.md`, `tasks/handoff.md`. Leer el relevo antes de continuar; verificar Git y pruebas en vez de repetir tareas.

El informe HTML/PDF y sus evidencias reales están en `docs/entrega`. Los cuatro cambios SC están implementados en ramas `sc-a-mestanza` a `sc-d-mestanza`: búsqueda/pendientes offline, AUDITOR, humedad estimada y contador observable. Cada rama conserva tres commits y su PR. La entrega se identifica mediante `v1.0-unidad2`.

[Informe PDF](docs/entrega/Examen_Unidad2_Mestanza_Bazan.pdf) · [HTML editable](docs/entrega/Examen_Unidad2_Mestanza_Bazan.html) · [Logs y grafos](docs/entrega/logs)
