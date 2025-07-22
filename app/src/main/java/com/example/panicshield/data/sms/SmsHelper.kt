package com.example.panicshield.data.sms

import android.content.Context
import android.content.pm.PackageManager
import android.telephony.SmsManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.panicshield.domain.model.PanicAlert
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class SmsHelper(private val context: Context) {

    companion object {
        private const val TAG = "SmsManager"
        private const val PERMISSION_SEND_SMS = android.Manifest.permission.SEND_SMS
    }

    private val smsManager: SmsManager = SmsManager.getDefault()

    fun sendSms(phone: String, message: String) {
        smsManager.sendTextMessage(phone, null, message, null, null)
    }

    /**
     * Verificar si tenemos permisos para enviar SMS
     */
    fun hasSmsPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            PERMISSION_SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Simular la conexión (para mantener compatibilidad con el código existente)
     */
    suspend fun connect(): Boolean = suspendCancellableCoroutine { continuation ->
        try {
            if (hasSmsPermission()) {
                Log.i(TAG, "✅ Permisos SMS disponibles")
                continuation.resume(true)
            } else {
                Log.e(TAG, "❌ Sin permisos para enviar SMS")
                continuation.resumeWithException(Exception("Sin permisos SMS"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error verificando permisos SMS", e)
            continuation.resumeWithException(e)
        }
    }

    /**
     * Enviar SMS de alerta de pánico
     */
    suspend fun publishPanicAlert(contactPhone: String, panicData: PanicAlert): Boolean =
        suspendCancellableCoroutine { continuation ->
            try {
                // ✅ VERIFICAR PERMISOS PRIMERO
                if (!hasSmsPermission()) {
                    Log.e(TAG, "❌ Sin permisos para enviar SMS")
                    continuation.resumeWithException(Exception("Sin permisos SMS"))
                    return@suspendCancellableCoroutine
                }

                // ✅ LIMPIAR NÚMERO DE TELÉFONO
                val cleanPhone = contactPhone.replace("+", "").replace(" ", "").replace("-", "")

                // ✅ CREAR MENSAJE SMS
                val smsMessage = createSmsMessage(panicData)

                Log.d(TAG, "📱 Enviando SMS de alerta:")
                Log.d(TAG, "   👤 Teléfono contacto: $contactPhone")
                Log.d(TAG, "   📞 Número limpio: $cleanPhone")
                Log.d(TAG, "   📝 Mensaje: $smsMessage")

                // ✅ VERIFICAR QUE EL MENSAJE NO ESTÉ VACÍO
                if (smsMessage.isBlank()) {
                    Log.e(TAG, "❌ Mensaje SMS vacío")
                    continuation.resumeWithException(Exception("Mensaje SMS vacío"))
                    return@suspendCancellableCoroutine
                }

                // ✅ ENVIAR SMS
                try {
                    val smsManager = SmsManager.getDefault()

                    // Si el mensaje es muy largo, dividirlo en partes
                    if (smsMessage.length > 160) {
                        val parts = smsManager.divideMessage(smsMessage)
                        smsManager.sendMultipartTextMessage(
                            cleanPhone,
                            null,
                            parts,
                            null,
                            null
                        )
                        Log.i(TAG, "✅ SMS multiparte enviado a: $cleanPhone")
                    } else {
                        smsManager.sendTextMessage(
                            cleanPhone,
                            null,
                            smsMessage,
                            null,
                            null
                        )
                        Log.i(TAG, "✅ SMS enviado a: $cleanPhone")
                    }

                    continuation.resume(true)

                } catch (smsException: Exception) {
                    Log.e(TAG, "❌ Error al enviar SMS: ${smsException.message}", smsException)
                    continuation.resumeWithException(smsException)
                }

            } catch (e: Exception) {
                Log.e(TAG, "❌ Error general al enviar SMS", e)
                continuation.resumeWithException(e)
            }
        }

    /**
     * Crear mensaje SMS formateado
     */
    private fun createSmsMessage(panicData: PanicAlert): String {
        return buildString {
            append("🚨 ALERTA DE PÁNICO 🚨\n")
            append("De: ${panicData.userName}\n")
            append("Teléfono: ${panicData.userPhone}\n")
            append("Tipo: ${panicData.emergencyType}\n")
            append("Mensaje: ${panicData.message}\n")
            append("Ubicación: ${panicData.address}\n")
            append("Coordenadas: ${panicData.latitude}, ${panicData.longitude}\n")
            append("Prioridad: ${panicData.priority}\n")
            append("Hora: ${java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(panicData.timestamp))}")
        }
    }

    /**
     * Suscribirse a alertas (no aplicable para SMS, pero mantenemos compatibilidad)
     */
    suspend fun subscribeToUserAlerts(userPhone: String): Boolean =
        suspendCancellableCoroutine { continuation ->
            try {
                Log.i(TAG, "📬 SMS no requiere suscripción (recepción automática)")
                continuation.resume(true)
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error en subscribeToUserAlerts", e)
                continuation.resumeWithException(e)
            }
        }

    /**
     * Desconectar (no aplicable para SMS)
     */
    fun disconnect() {
        Log.i(TAG, "📱 SMS Manager desconectado")
    }

    /**
     * Verificar si está "conectado" (si tiene permisos)
     */
    fun isConnected(): Boolean {
        return hasSmsPermission()
    }

    /**
     * Enviar SMS simple (función adicional)
     */
    fun sendSimpleSms(phoneNumber: String, message: String): Boolean {
        return try {
            if (!hasSmsPermission()) {
                Log.e(TAG, "❌ Sin permisos SMS")
                return false
            }

            val smsManager = SmsManager.getDefault()
            val cleanPhone = phoneNumber.replace("+", "").replace(" ", "").replace("-", "")

            smsManager.sendTextMessage(cleanPhone, null, message, null, null)
            Log.i(TAG, "✅ SMS simple enviado a: $cleanPhone")
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error enviando SMS simple", e)
            false
        }
    }
}