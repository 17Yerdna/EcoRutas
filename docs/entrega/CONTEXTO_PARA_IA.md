# Continuación de EcoRecicla

Lee primero `tasks/handoff.md`, después `tasks/todo.md` y `tasks/plan.md`. El estado de esos archivos manda sobre cualquier resumen de chat.

## Encargo autorizado

El usuario autorizó implementar EcoAPI, EcoRutas y el entregable. Fecha límite indicada: 28-10-2026 antes de las 17:00 America/Lima. Continuar autónomamente y registrar avances por tarea. No prometer cumplimiento completo sin verificar.

## Fuentes

- Examen válido: `examen_parcial_ii_practico_versionb_nggszqgbe9.pdf` (EcoRecicla, Versión B). Ignorar AgroSierra/Versión A.
- Los tres diseños y planes están bajo `EcoAPI/docs/superpowers/` y `EcoRutas/docs/superpowers/`.
- Referencia de diseño del informe: `C:/Users/andre/OneDrive/Desktop/Archivos extra de PharmaMobil/Docs Sesion 11/S11_ActividadAutonoma_Mestanza_Bazan.html` y PDF homónimo.
- Sesiones 10 y 11 en la misma carpeta padre. No fabricar evidencia ni resultados.

## Contratos críticos

Spring Boot Kotlin + PostgreSQL Docker; KMP Android + Compose + Ktor + SQLDelight + Koin. UI solo observa base local. Privilegios en servicio, nunca autorización por nombre de rol en controlador. JWT firmado, BCrypt, almacenamiento Android seguro. RN01 ruta cerrada=409; RN02 propio/mismo día=403; RN03 neto calculado en servidor>0=400 por campo; RN04 no cerrar puntos sin pesaje/omisión=409 con cantidad. Errores: timestamp/status/error/message/path/validationErrors. Paginación: contenido/pagina/tamanio/totalElementos/totalPaginas/ultima.

Semilla: operario1/supervisor1/admin con Eco2026*, 12 puntos, 3 zonas, 2 rutas, 15 pesajes. Solo el supervisor anula/cierra; admin gestiona puntos y no registra pesajes. Cola offline con IDs negativos, CREATE→UPDATE→ANULATE→descarga, preservar pendientes, no duplicar.

## Git

Repositorios: https://github.com/17Yerdna/EcoAPI.git y https://github.com/17Yerdna/EcoRutas.git. Identidad: Andrey Fidel Mestanza Bazan, andreymestanzabazan@gmail.com. main/develop/feature/fix, al menos tres PR cerrados por repo, merge --no-ff, tag v1.0-unidad2. SC se asignará; no anticipar escenario. Historial verdadero, nunca retrofechar.

## Entorno y continuidad

Workspace: `C:/Users/andre/OneDrive/Desktop/Examen Unidad 2`. Maven instalado. Java de Oracle puede colgarse: usar `C:/Program Files/Android/Android Studio/jbr` (JDK25) y compilar target21. Docker Desktop instalado; comprobar motor antes de tests.

Actualizar `tasks/handoff.md` antes/después de cada tarea: rama, hashes reales, cambios sin commit, pruebas y resultado, bloqueo y siguiente acción exacta. No marcar builds/pruebas como verdes sin salida observada. Código parcial se conserva y se identifica, nunca se borra para reiniciar.

## Entrega implementada

La base completa y los cuatro SC fueron implementados. SC-A/D en EcoRutas; SC-B/C en ambos repositorios. Cada rama SC tiene tres commits reales (prueba, implementación, documentación) y PR a develop. API y app compilan; PostgreSQL está activo en Docker. Se observaron18 pruebas API,18 móviles y18 aserciones Newman sin fallos.

Evidencias reales: login, ruta,403 por pesaje ajeno con sesión conservada, registro offline ID-1, reconexión/IDpositivo, búsqueda+pendientes, contador1→0 y humedad12.5% persistida. El informe usa el estilo de sesión11; PDF/HTML, guion y logs están en EcoRutas/docs/entrega. APK en composeApp/build/outputs/apk/debug/composeApp-debug.apk.

## Decisiones y límites que debe conocer otra IA

- Un servicio transaccional API y DTO/entidades agrupados reducen archivos. Si crece el dominio, separar por caso de uso; hoy las reglas permanecen fuera del controlador.
- Pruebas de integración sobre PostgreSQL Docker local, no Testcontainers: iniciar db antes de Maven. No se afirma independencia de un servidor local.
- Ramas feature en clones nuevos, sin worktrees extra: no había trabajo paralelo de implementación que aislar. No reiniciar historia.
- Migración inicial agrupa esquema y semillas; V2 agrega AUDITOR y V3 humedad. SQLite1.sqm migra instalaciones existentes.
- Bearer se añade centralmente en ApiClient.call para usar siempre la sesión activa del turno. No depende de un token cacheado por un proveedor Auth.
- La APK entregada es debug para emulador y backend local10.0.2.2:8080. Un despliegue público/release requiere HTTPS y configurar URL.
- Historial iniciado28-10-2026; no se fabricaron fechas anteriores. El ensayo oral del estudiante sigue siendo una actividad humana.
- El usuario ya consumió su reinicio gratuito de cuota; no asumir que queda otro ni prometer activarlo automáticamente.

## Hallazgos importantes corregidos

1. Respuesta perdida de CREATE seguida de edición offline: reconciliar con el último valor local antes de limpiar pendiente.
2. Cancelación después de un resultado ambiguo: recuperar creación remota y anularla, no perderla eliminando solo la fila negativa.
3. Carrera alta/cierre: refrescar entidad Ruta bajo bloqueo pesimista antes de validar cerrada.
4. Formulario nuevo repetido: UUID estable por borrador y cierre después del guardado evitan duplicados.
5. Visita atendida: limitar aporte de pesajes locales al día actual; el supervisor corrige globalmente por privilegio.
6. Nuevo turno: restablecer filtros y limpiar datos locales solo tras sync o descarte confirmado.

Comprobar publicación final, hashes y siguiente acción en tasks/handoff.md. Desde un clon aislado sin carpeta tasks, consultar README, docs/cambios, logs de entrega y tag v1.0-unidad2. No volver a implementar lo ya existente.
