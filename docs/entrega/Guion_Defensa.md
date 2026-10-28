# Defensa EcoRecicla — Andrey Mestanza Bazán

Demostrar cada respuesta con el archivo abierto y el sistema ejecutándose. No basta leer una definición.

1. **¿Dónde se autoriza?** EcoAPI `servicio/EcoService.kt`, método `exigir`. Una petición con JWT válido recibe403 si falta el privilegio. Mostrar Postman operario intentando anular. El botón oculto no impide invocar HTTP directamente.
2. **¿Qué transporta el JWT?** `config/Seguridad.kt`, `JwtService`: subject=ID, usuario, rol y privilegios, issuedAt/expiration. HMAC valida firma y una hora de vigencia; un token alterado/vencido recibe401.
3. **¿Dónde se guarda el token?** Android `data/security/AlmacenSeguroAndroid.kt`: AES/GCM, IV nuevo y clave de Android Keystore. SharedPreferences contiene solo ciphertext e IV. Salir borra la sesión cifrada.
4. **¿Quién añade Authorization?** `data/remote/ApiClient.kt`, `call`: centraliza bearerAuth y nunca lo registra en logs. La sesión activa proporciona el token de cada turno.
5. **401 frente a403.** `ApiClient.call` y `EcoViewModel`:401 borra sesión activa y mantiene trabajo local separado por usuario para reautenticarse;403 muestra motivo conservando sesión. Mostrar captura03 y perfil.
6. **Recorrido de un pesaje.** Formulario→EcoViewModel→PesajeRepositoryImpl→SQLite pendiente→Ktor→EcoService→JPA/PostgreSQL→respuesta con neto/ID→reemplazo transaccional SQLite→Flow→UI.
7. **¿Por qué Flow?** `BaseLocal` usa queries.asFlow. La pantalla observa StateFlow del ViewModel. Una descarga actualiza SQLite, nunca una lista remota directa en el composable.
8. **¿Cómo nace el ID offline?** `Eco.sq`, `siguienteId`: MIN(COALESCE(MIN(id),0),0)-1, acotado por usuario. BaseLocal ejecuta esa consulta y el repositorio la serializa con Mutex. Mostrar prueba de tabla vacía y captura -1.
9. **¿En qué orden sincroniza?** `PesajeRepositoryImpl.sincronizar`: CREATE, UPDATE, ANULATE, descarga. UUID estable impide duplicados; una creación ya confirmada se reconcilia con el último deseo local antes de limpiar pendiente.
10. **¿Qué prueban los fakes?** `OfflineTest.Memoria/Remoto` simulan ausencia de red y respuesta perdida DESPUÉS de crear remotamente. Las aserciones revisan filas, campos, IDs y llamadas ordenadas, no solo que un mock fue llamado.
11. **¿Cómo llega el error al campo?** `ErrorResponse.validationErrors`→`ErrorDto`→`ErrorApi.campos`→`EcoUi.campos`→supportingText de bruto/tara/humedad. El servidor conserva autoridad aunque la app valide antes.
12. **¿Cómo agregar un rol?** Mostrar V2__rol_auditor.sql: rol, dos privilegios y usuario. No se modifica filtro/controlador. La app muestra acciones a partir de privilegios; soloLectura también funciona con otro nombre de rol.
13. **¿Cómo se versionó?** Abrir grafos y PR de ambos repositorios. main/develop/feature; cuatro ramas SC identificables, tres commits reales por cambio, merges con PR y tag v1.0-unidad2. El historial inició28-10-2026 y no se retrofechó.

## Cambios en vivo preparados

- SC-A: `Eco.sq.filtrarPuntos` combina búsqueda y pendientes sin red. Ver BaseLocalTest.
- SC-B: usuario auditor1/Eco2026*, dos privilegios de lectura y403 en escrituras.
- SC-C: humedadEstimada0..100 en PostgreSQL, DTO, dominio, SQLite/migración, formulario y detalle.
- SC-D: COUNT observable por usuario, distintivo de ruta sin incrementos manuales.

## Comandos de ensayo

```powershell
docker compose up --build
.\mvnw.cmd test
.\gradlew.bat :shared:testAndroidHostTest :composeApp:assembleDebug
npx newman run docs/api/EcoAPI.postman_collection.json -r cli
git log --graph --decorate --oneline --all
```

El primer comando se ejecuta en EcoAPI; Gradle en EcoRutas. Usar el emulador específico de verificación, no enviar entradas a un dispositivo que esté usando otro proyecto.
