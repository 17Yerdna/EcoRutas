# SC-C — Humedad estimada

Campo humedadEstimada añadido al DTO, conversión al dominio, payload persistido y columna explícita SQLite. Migración 1.sqm conserva instalaciones existentes. El formulario acepta 0..100% y el detalle lo muestra. La reconciliación de respuestas perdidas también compara humedad para no descartar una corrección.

Prueba DTO/dominio y suite completa: BUILD SUCCESSFUL (`sc-c-green.log`). APK debug generada con ambos formularios/detalle y persistencia SQLDelight.
