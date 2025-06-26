# Configuración Inicial

## Actualizar el Build.Graddle.kts
Para evitar errores en la compilacion, se necesita actualizar  (dentro del archivo cambiar las versiones de 34) a:
- compileSdk = 35
- targetSdk = 35

## Version de gradle 
8.11.1

"¿Cómo se podría sincronizar cuando un dato se modificó en el backend? Por ejemplo, tenemos una app para compra de pasajes de bus, el usuario selecciona un asiento y mientras llena algunos datos, otro usuario compro el asiento?"

## ¿Cómo se podría sincronizar cuando un dato se modificó en el backend?

En sistemas multiusuario como una aplicación de compra de pasajes, es esencial reflejar de forma inmediata cualquier cambio crítico que ocurra en el backend, especialmente cuando varios usuarios interactúan sobre los mismos recursos (por ejemplo, asientos disponibles). La sincronización periódica no es suficiente en estos casos; se requiere gestión de concurrencia y mecanismos de actualización reactiva.

### Problema: estado compartido y cambiante

Cuando un usuario selecciona un asiento, ese estado no está bloqueado por defecto para otros usuarios. Si no se maneja adecuadamente, puede ocasionar inconsistencias como:

> “El asiento ya fue reservado” justo después de llenar los datos.

Esto ocurre porque múltiples clientes están leyendo un estado compartido que puede cambiar entre la selección y la confirmación.

### Estrategias para sincronizar datos modificados desde el backend

1. Verificación justo antes de confirmar la compra (control optimista)

Antes de realizar la reserva final, el cliente debe verificar que el asiento siga disponible:

```kotlin
val response = api.verifySeatAvailability(seatId)
if (response.isAvailable) {
    api.reserveSeat(seatId, userData)
} else {
    showError("El asiento ya fue tomado. Por favor elige otro.")
}
```

Esta estrategia es simple de implementar. Sin embargo, no evita que varios usuarios seleccionen el mismo asiento al mismo tiempo. Su efectividad depende del control transaccional del backend.

2. Bloqueo temporal del asiento (reserva provisional)

Una vez que el usuario selecciona un asiento, se puede solicitar un bloqueo temporal al backend:

```http
POST /seats/{id}/hold
{
  "userId": "abc",
  "expiresIn": 180
}
```

El servidor marca el asiento como "temporalmente reservado" para el usuario solicitante. Si la operación no se completa dentro del tiempo especificado, el asiento se libera automáticamente.

Consideraciones importantes:

- Se deben limpiar los bloqueos expirados para evitar inconsistencias.
- La interfaz debe mostrar el estado "en proceso de reserva" para asientos bloqueados por otros.

3. Sincronización en tiempo real (WebSockets, Firebase, etc.)

Cuando se requiere que todos los usuarios vean al instante los cambios de disponibilidad, es recomendable emplear comunicación en tiempo real.

Ejemplo con Firebase Realtime Database:

```bash
/buses/{busId}/seats/{seatId} = {
  status: "available" | "reserved",
  userId: "..."
}
```

En el cliente, se suscribe a los cambios de estado:

```kotlin
seatsRef.addValueEventListener(object : ValueEventListener {
    override fun onDataChange(snapshot: DataSnapshot) {
        // Actualizar interfaz en tiempo real
    }

    override fun onCancelled(error: DatabaseError) {
        // Manejo de errores
    }
})
```

Esto permite que la interfaz reaccione inmediatamente cuando otro usuario reserva o libera un asiento. Es la opción ideal para aplicaciones con múltiples usuarios concurrentes.

4. Control de concurrencia en el servidor

Incluso si la lógica del cliente es correcta, el backend debe garantizar la atomicidad de las operaciones. Esto evita condiciones de carrera cuando dos usuarios intentan reservar el mismo asiento al mismo tiempo.

Ejemplo en un servicio transaccional:

```kotlin
@Transactional
fun reserveSeat(userId: String, seatId: String) {
    val seat = seatRepository.findById(seatId)
    if (seat.status != AVAILABLE) throw ConflictException("Asiento ocupado")
    seat.status = RESERVED
    seat.userId = userId
    seatRepository.save(seat)
}
```

Este patrón asegura que solo un usuario pueda reservar el asiento, aunque múltiples solicitudes lleguen casi simultáneamente.

