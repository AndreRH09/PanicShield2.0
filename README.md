##"¿Cómo se podría sincronizar cuando un dato se modificó en el backend? Por ejemplo, tenemos una app para compra de pasajes de bus, el usuario selecciona un asiento y mientras llena algunos datos, otro usuario compro el asiento?"

# Sincronización de Datos Modificados en el Backend en Aplicaciones Multiusuario

## Contexto

En sistemas multiusuario, como aplicaciones de compra de pasajes, es fundamental sincronizar en tiempo real los cambios críticos que ocurren en el backend. Uno de los casos más comunes es la selección y reserva de asientos. El problema radica en que múltiples usuarios pueden interactuar simultáneamente sobre los mismos recursos, lo cual genera **condiciones de carrera** y **estados inconsistentes** si no se implementan mecanismos de control adecuados.

## Problema: Estado Compartido y Cambiante

Cuando un usuario selecciona un asiento, el sistema puede no bloquearlo automáticamente para los demás. Esto da lugar a situaciones como:

> "El asiento ya fue reservado" justo después de llenar los datos.

Este problema surge porque varios clientes leen un estado compartido que puede cambiar en cualquier momento entre la selección y la confirmación. La falta de sincronización reactiva y control de concurrencia agrava el riesgo de inconsistencia.

## Estrategias para la Sincronización de Datos Modificados

### 1. Verificación Justo Antes de Confirmar (Control Optimista)

Esta estrategia consiste en verificar la disponibilidad del recurso justo antes de ejecutar la acción definitiva:

```kotlin
val response = api.verifySeatAvailability(seatId)
if (response.isAvailable) {
    api.reserveSeat(seatId, userData)
} else {
    showError("El asiento ya fue tomado. Por favor elige otro.")
}
```

Es simple de implementar, pero no evita conflictos si dos usuarios confirman casi simultáneamente. El backend debe manejar transacciones atómicas para evitar dobles reservas [1].

---

### 2. Bloqueo Temporal del Asiento (Reserva Provisional)

El sistema puede bloquear provisionalmente el asiento durante un tiempo limitado cuando el usuario lo selecciona:

```http
POST /seats/{id}/hold
{
  "userId": "abc",
  "expiresIn": 180
}
```

El backend marca el asiento como "en espera". Si no se completa la operación dentro del tiempo especificado, el bloqueo expira automáticamente. La interfaz debe reflejar visualmente el estado de los asientos bloqueados.

**Consideraciones:**

- Es obligatorio un proceso de limpieza periódica para liberar los bloqueos expirados.
- Es eficaz para evitar reservas concurrentes, pero debe combinarse con control de concurrencia en el backend [2].

---

### 3. Sincronización en Tiempo Real (WebSockets / Firebase)

Para aplicaciones altamente concurrentes, la sincronización en tiempo real es esencial. Se puede utilizar Firebase Realtime Database o WebSockets para notificar cambios instantáneamente.

Ejemplo con Firebase:

```bash
/buses/{busId}/seats/{seatId} = {
  status: "available" | "reserved",
  userId: "..."
}
```

En el cliente, se suscribe a los eventos:

```kotlin
seatsRef.addValueEventListener(object : ValueEventListener {
    override fun onDataChange(snapshot: DataSnapshot) {
        // Actualizar UI en tiempo real
    }

    override fun onCancelled(error: DatabaseError) {
        // Manejo de errores
    }
})
```

Este enfoque reduce los errores por lectura de estados obsoletos y mejora la experiencia del usuario [3].

---

### 4. Control de Concurrencia en el Backend (Transacciones Atómicas)

A pesar de implementar lógica en el cliente, el backend debe garantizar **integridad de datos** mediante operaciones atómicas:

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

La transacción asegura que dos solicitudes simultáneas no puedan reservar el mismo asiento. Este enfoque es crucial en sistemas críticos [4].

---

## Sobre las Referencias

### [1] Martin Fowler – *Patterns of Enterprise Application Architecture*

> Fowler describe patrones como la validación previa a confirmaciones críticas. Este patrón es útil cuando múltiples usuarios acceden simultáneamente al mismo recurso.

---

### [2] Eric Evans – *Domain-Driven Design*

> Evans sugiere modelar reglas del negocio explícitamente. Un bloqueo de asiento no es solo una técnica, sino una expresión de la intención del usuario en el dominio.

---

### [3] B. Burns, J. Beda y K. Hightower – *Kubernetes: Up and Running*

> El libro explica cómo manejar eventos distribuidos y sincronización de estados, lo cual es directamente aplicable a sistemas multiusuario con cambios frecuentes.

---

### [4] C. J. Date – *Introducción a los Sistemas de Bases de Datos*

> Date explica cómo las transacciones aseguran la integridad y consistencia cuando múltiples usuarios acceden al mismo recurso concurrentemente.

---

## Referencias

[1] M. Fowler, *Patterns of Enterprise Application Architecture*, Addison-Wesley, 2003.

[2] E. Evans, *Domain-Driven Design: Tackling Complexity in the Heart of Software*, Addison-Wesley, 2004.

[3] B. Burns, J. Beda y K. Hightower, *Kubernetes: Up and Running*, O'Reilly Media, 2022.

[4] C. Date, *Introducción a los Sistemas de Bases de Datos*, Prentice Hall, 2001.
