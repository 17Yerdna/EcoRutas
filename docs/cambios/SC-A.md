# SC-A: filtro de ruta sin conexión

La consulta SQLDelight `filtrarPuntos` combina texto (razón social, dirección o zona) con solo pendientes de pesar. Respeta usuario, orden de visita y pesajes locales del día. `Flow` actualiza el ViewModel; el composable consume el resultado sin recorrer listas para filtrar.

Verificación: `BaseLocalTest.filtroCombinaBusquedaYPendientesLocalmente`, más suite completa y APK. Resultado observado en `sc-a-green.log`: BUILD SUCCESSFUL. No requiere conectividad ni cambios de API.
