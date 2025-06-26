# Configuración Inicial

## Actualizar el Build.Graddle.kts
Para evitar errores en la compilacion, se necesita actualizar  (dentro del archivo cambiar las versiones de 34) a:
- compileSdk = 35
- targetSdk = 35

## Version de gradle 
8.11.1

"¿Cómo se podría sincronizar cuando un dato se modificó en el backend? Por ejemplo, tenemos una app para compra de pasajes de bus, el usuario selecciona un asiento y mientras llena algunos datos, otro usuario compro el asiento?"

## ¿Cómo se podría sincronizar cuando un dato se modificó en el backend?

Aquí estamos hablando de sincronización en tiempo real o casi en tiempo real, para reflejar cambios críticos (como disponibilidad de asientos) que pueden modificarse desde otros dispositivos o usuarios. En este caso, la sincronización periódica no basta: necesitas gestión de concurrencia y mecanismos de actualización reactiva.

---

### El problema: estado compartido y cambiante

En una app de compra de pasajes, cuando un usuario selecciona un asiento, ese estado no está bloqueado por defecto para otros usuarios. Si no se maneja adecuadamente, puede causar errores como:

> “El asiento ya fue reservado” justo después de llenar los datos.

---
