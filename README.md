# PanicShield 2.0

## Integrantes:
- Vilca Quispe, Franks
- Titto Campos, Rutbel
- Añazco Huamanquispe, André
- Garay Bedregal, César


**PanicShield 2.0** es una aplicación móvil de emergencia que permite al usuario generar alertas de pánico mediante SMS, BLE y almacenamiento remoto/local. Ha sido desarrollada con **Jetpack Compose** y está orientada a combatir la inseguridad ciudadana, permitiendo una reacción rápida y configurable.

---

## Características principales

* Envío de alertas vía SMS con:

  * Ubicación GPS
  * Hora y fecha del evento
  * Nivel de prioridad
  * Teléfono del emisor
  * Transmisión Bluetooth BLE
  * Escaneo continuo para rastreo de usuarios
  * Configuración dinámica de
    - Campos a enviar por SMS
    - Activación de alarma con múltiples toques
  * Gestor de contactos de emergencia local (Room + Flow)
  * Interfaz responsiva con Jetpack Compose Material3
  * Autenticación y almacenamiento de eventos en Supabase

---

## Arquitectura del Proyecto

**MVVM + Clean Architecture**

* `ViewModel`: Expone estado de UI y lógica de negocio.
* `UseCases`: Lógica de dominio (ej. enviar SMS, activar BLE, guardar en Room).
* `Repository`: Orquesta Room, Supabase, y BLE.
* `DataSources`: Acceso a datos externos (BLE, Supabase, Room).
* `UI`: Jetpack Compose con estado reactivo.

---

Arquitectura del Proyecto
PanicShield 2.0 sigue una arquitectura moderna basada en principios de limpieza, modularidad y escalabilidad, implementada con las siguientes capas y tecnologías:

1. Presentation Layer (UI)
- Jetpack Compose: Utilizado para construir una UI declarativa, responsiva y modular.
- State Hoisting + ViewModel: El estado de cada pantalla es manejado por su respectivo ViewModel, facilitando separación de responsabilidades.
- Navigation Component: Maneja la navegación entre pantallas mediante rutas definidas en una estructura centralizada.

2. Domain Layer (Opcional/Futura)
Actualmente acoplado a la capa de presentación, pero pensado para evolucionar hacia una capa de dominio limpia que contenga lógica empresarial desacoplada.

3. Data Layer
Room + DAO: Base de datos local para gestionar contactos de emergencia y configuraciones persistentes.

DataStore: Utilizado para almacenar preferencias y configuraciones como envío de SMS, ubicación, y prioridades.

BLE (Bluetooth Low Energy): Módulo específico para escaneo y transmisión de señales tipo iBeacon codificando datos críticos como temperatura y humedad.

4. ViewModel Layer
AndroidViewModel + StateFlow: Cada ViewModel encapsula y expone los datos con StateFlow, brindando un flujo reactivo hacia la UI.

Contiene lógica de presentación y coordina acciones entre la UI, repositorios y almacenamiento local.

5. Dependency Injection
Hilt: Utilizado para la inyección de dependencias en ViewModels, Repositorios, DAOs y otras clases. Permite mantener el código desacoplado y facilita pruebas unitarias.

6. Sensor y Comunicación
BLE Transmitter/Scanner: Integración de sensores simulados de temperatura y humedad mediante BluetoothLeAdvertiser y BluetoothLeScanner.

iBeacon protocol: Los datos se codifican en los campos Major y Minor de un paquete iBeacon para garantizar compatibilidad y simplicidad en el escaneo.

Permisos Dinámicos: Gestión segura de permisos de Bluetooth, ubicación y SMS.

7. SMS y Alertas
Módulo de Configuración Dinámica: Permite al usuario activar/desactivar opciones de envío como: ubicación, teléfono, prioridad, etc.

Sistema de Toques: El botón de pánico se activa con múltiples toques en pantalla, configurable en la sección de ajustes.


---

## Tecnologías usadas

* **Jetpack Compose**: UI declarativa
* **Room**: Base de datos local
* **BLE**: Comunicación inalámbrica entre dispositivos
* **Supabase**: Backend como servicio (autenticación, storage y base de datos)
* **Kotlin Coroutines + Flow**: Programación reactiva
* **Permission APIs**: Acceso a GPS, SMS y Bluetooth

---

## Requisitos de Ejecución

1. **Android Studio Flamingo+**

2. **Mínimo SDK 26 (Android 8.0)**

3. **Permisos activados**:

   * `ACCESS_FINE_LOCATION`
   * `BLUETOOTH`, `BLUETOOTH_ADMIN`, `BLUETOOTH_CONNECT`
   * `SEND_SMS`, `RECEIVE_SMS`

---

## Ejecución del Proyecto

1. Clonar repositorio:

```bash
git clone https://github.com/tuusuario/panicshield-2.0.git
cd panicshield-2.0
```

2. Configurar credenciales en archivo `local.properties` o `BuildConfig` (SOLO EN CASO DE MODIFICAR LA LOGICA ACTUAL DEL PROYECTO, caso contrario ignorar esta parte):

```properties
SUPABASE_URL="https://xxx.supabase.co"
SUPABASE_KEY="your-key"
```

3. Ejecutar la app en un emulador con soporte BLE o dispositivo real.

4. Acceder con:

```bash
Usuario: rttitoca@unsa.edu.pe
Contraseña: 123456
```

---

## Escenarios de uso

* Activar alerta presionando rápidamente el botón de pánico (toques múltiples).
* Seleccionar qué información se enviará por SMS.
* Administrar contactos
* Ver historial de alertas enviadas (local o remoto).
* Configurar las alertas y la app
