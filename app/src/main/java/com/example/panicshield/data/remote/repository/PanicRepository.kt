
package com.example.panicshield.data.remote.repository

import android.util.Log
import com.example.panicshield.data.sms.SmsHelper
import com.example.panicshield.domain.model.Contact
import com.example.panicshield.domain.model.PanicAlert
import io.github.jan.supabase.SupabaseClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import javax.inject.Inject

class PanicRepository @Inject constructor(
    private val supabaseClient: SupabaseClient,
    private val smsManager: SmsHelper // ✅ CAMBIO: SmsManager en lugar de MqttManager
) {

    companion object {
        private const val TAG = "PanicRepository"
    }

    suspend fun sendPanicAlert(
        emergencyType: String,
        latitude: Double,
        longitude: Double,
        address: String?,
        message: String,
        priority: String = "HIGH",
        deviceInfo: Map<String, Any>? = null
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🚨 Iniciando envío de alerta de pánico...")

            val currentUser = supabaseClient.auth.currentUserOrNull()
            if (currentUser == null) {
                Log.e(TAG, "❌ Usuario no autenticado")
                return@withContext Result.failure(Exception("Usuario no autenticado"))
            }

            Log.d(TAG, "👤 Usuario autenticado: ${currentUser.id}")

            // 1. Insertar en tu tabla emergencies
            val emergencyRecord = mapOf(
                "user_id" to currentUser.id,
                "emergency_type" to emergencyType,
                "status" to "ACTIVE",
                "latitude" to latitude,
                "longitude" to longitude,
                "address" to address,
                "message" to message,
                "priority" to priority,
                "device_info" to deviceInfo,
                "created_at" to System.currentTimeMillis()
            )

            Log.d(TAG, "💾 Insertando emergencia en BD...")
            supabaseClient.from("emergencies").insert(emergencyRecord)
            Log.d(TAG, "✅ Emergencia insertada exitosamente")

            // 2. Obtener contactos del usuario
            Log.d(TAG, "📞 Obteniendo contactos...")
            val contacts = supabaseClient.from("contacts")
                .select() {
                    filter {
                        eq("user_id", currentUser.id)
                    }
                }
                .decodeList<Contact>()

            Log.d(TAG, "📞 Contactos obtenidos: ${contacts.size}")

            if (contacts.isEmpty()) {
                Log.w(TAG, "⚠️ No hay contactos para enviar alerta")
                return@withContext Result.success(false)
            }

            // 3. Obtener info del usuario actual
            val userName = currentUser.userMetadata?.get("full_name")?.toString()
                ?: currentUser.userMetadata?.get("name")?.toString()
                ?: "Usuario desconocido"
            val userPhone = currentUser.phone ?: ""
            val userId = currentUser.id

            Log.d(TAG, "👤 Info usuario: $userName, $userPhone")

            // 4. Crear alerta usando los datos de la emergencia
            val panicAlert = PanicAlert(
                userName = userName,
                userPhone = userPhone,
                userId = userId,
                emergencyType = emergencyType,
                latitude = latitude,
                longitude = longitude,
                address = address ?: "Ubicación no disponible",
                message = message,
                priority = priority,
                timestamp = System.currentTimeMillis(),
                deviceInfo = deviceInfo
            )

            Log.d(TAG, "📝 Alerta SMS creada: ${panicAlert.toJson()}")

            // 5. ✅ VERIFICAR PERMISOS SMS
            if (!smsManager.hasSmsPermission()) {
                Log.e(TAG, "❌ Sin permisos para enviar SMS")
                return@withContext Result.failure(Exception("Sin permisos SMS"))
            }

            // 6. ✅ CONECTAR SMS MANAGER (verificar permisos)
            if (!smsManager.isConnected()) {
                Log.d(TAG, "📱 Verificando permisos SMS...")
                if (!smsManager.connect()) {
                    Log.e(TAG, "❌ Error verificando permisos SMS")
                    return@withContext Result.failure(Exception("Sin permisos SMS"))
                }
                Log.d(TAG, "✅ SMS Manager listo")
            }

            // 7. ✅ ENVIAR SMS A CADA CONTACTO
            var successCount = 0
            var errorCount = 0

            Log.d(TAG, "📤 Enviando SMS a ${contacts.size} contactos...")

            contacts.forEach { contact ->
                try {
                    Log.d(TAG, "📤 Enviando SMS a: ${contact.name} - ${contact.phone}")

                    val success = smsManager.publishPanicAlert(contact.phone, panicAlert)

                    if (success) {
                        successCount++
                        Log.d(TAG, "✅ SMS enviado exitosamente a: ${contact.name}")
                    } else {
                        errorCount++
                        Log.e(TAG, "❌ Falló envío SMS a: ${contact.name}")
                    }
                } catch (e: Exception) {
                    errorCount++
                    Log.e(TAG, "❌ Error enviando SMS a ${contact.name}: ${e.message}", e)
                }
            }

            Log.d(TAG, "📊 Resultado final: $successCount éxitos, $errorCount errores")

            if (successCount > 0) {
                Log.i(TAG, "✅ SMS enviados exitosamente a $successCount contactos")
                Result.success(true)
            } else {
                Log.e(TAG, "❌ No se pudo enviar SMS a ningún contacto")
                Result.failure(Exception("No se pudo enviar SMS a ningún contacto"))
            }

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error general enviando alerta: ${e.message}", e)
            Result.failure(e)
        }
    }

    // ✅ FUNCIÓN ADICIONAL: Enviar SMS simple a un contacto
    suspend fun sendSimpleSms(contactPhone: String, message: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "📱 Enviando SMS simple a: $contactPhone")

            if (!smsManager.hasSmsPermission()) {
                return@withContext Result.failure(Exception("Sin permisos SMS"))
            }

            val success = smsManager.sendSimpleSms(contactPhone, message)

            if (success) {
                Log.d(TAG, "✅ SMS simple enviado exitosamente")
                Result.success(true)
            } else {
                Log.e(TAG, "❌ Error enviando SMS simple")
                Result.failure(Exception("Error enviando SMS"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error enviando SMS simple: ${e.message}", e)
            Result.failure(e)
        }
    }

    // ✅ FUNCIÓN ADICIONAL: Verificar permisos SMS
    fun hasSmsPermission(): Boolean {
        return smsManager.hasSmsPermission()
    }
}