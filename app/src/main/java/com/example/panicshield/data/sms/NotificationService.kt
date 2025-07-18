package com.example.panicshield.data.mqtt
/**
import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.panicshield.R
import com.example.panicshield.domain.model.PanicAlert
import com.example.panicshield.MainActivity // Activity principal que contiene Compose Navigation
import com.example.panicshield.data.mqqt.UserHelper
import io.github.jan.supabase.SupabaseClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class NotificationService : Service() {
    private lateinit var mqttManager: MqttManager
    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private lateinit var alertReceiver: BroadcastReceiver
    private lateinit var userHelper: UserHelper
    private val supabaseClient: SupabaseClient = TODO() // Cambié el nombre a camelCase

    override fun onCreate() {
        super.onCreate()
        mqttManager = MqttManager(this)
        userHelper = UserHelper(this, supabaseClient)
        createNotificationChannel()
        setupAlertReceiver()

        // Suscribirse a alertas para el usuario actual
        serviceScope.launch {
            try {
                val isConnected = mqttManager.connect()
                if (isConnected) {
                    val currentUserPhone = userHelper.getUserPhone()
                    if (currentUserPhone.isNotEmpty()) {
                        mqttManager.subscribeToUserAlerts(currentUserPhone)
                        Log.i("NotificationService", "Suscrito a alertas para: $currentUserPhone")
                    }
                } else {
                    Log.e("NotificationService", "No se pudo conectar a MQTT")
                }
            } catch (e: Exception) {
                Log.e("NotificationService", "Error al conectar/suscribir MQTT", e)
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun setupAlertReceiver() {
        alertReceiver = object : BroadcastReceiver() {
            // Removí @Composable - no es necesario aquí
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == "com.tuapp.PANIC_ALERT_RECEIVED") {
                    val alertJson = intent.getStringExtra("panic_alert")
                    alertJson?.let {
                        val panicAlert = PanicAlert.fromJson(it)
                        showPanicNotification(panicAlert)
                    }
                }
            }
        }

        val filter = IntentFilter("com.tuapp.PANIC_ALERT_RECEIVED")
        registerReceiver(alertReceiver, filter)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(alertReceiver)
        mqttManager.disconnect()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "PANIC_CHANNEL",
                "Alertas de Pánico",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones de alertas de pánico"
                enableVibration(true)
                enableLights(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    // Removí @Composable - no es necesario aquí
    private fun showPanicNotification(panicAlert: PanicAlert) {
        // Intent hacia la MainActivity principal que maneja la navegación de Compose
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("panic_alert", panicAlert.toJson())
            // Agregar información para navegar al screen específico
            putExtra("navigate_to", "home_screen")
            putExtra("show_panic_alert", true)
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val priorityIcon = when (panicAlert.priority) {
            "HIGH" -> "🚨"
            "MEDIUM" -> "⚠️"
            else -> "ℹ️"
        }

        val notification = NotificationCompat.Builder(this, "PANIC_CHANNEL")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle("$priorityIcon ALERTA: ${panicAlert.emergencyType}")
            .setContentText("${panicAlert.userName} necesita ayuda urgente")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("${panicAlert.userName} ha enviado una alerta de ${panicAlert.emergencyType}.\n\n" +
                        "Mensaje: ${panicAlert.message}\n" +
                        "Ubicación: ${panicAlert.address ?: "Lat: ${panicAlert.latitude}, Lng: ${panicAlert.longitude}"}\n" +
                        "Teléfono: ${panicAlert.userPhone}"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.ic_launcher_foreground, "Llamar", createCallPendingIntent(panicAlert.userPhone))
            .addAction(R.drawable.ic_launcher_foreground, "Ver Ubicación", createMapPendingIntent(panicAlert.latitude, panicAlert.longitude))
            .build()

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(panicAlert.timestamp.toInt(), notification)
    }

    private fun createCallPendingIntent(phone: String): PendingIntent {
        val intent = Intent(Intent.ACTION_CALL).apply {
            data = Uri.parse("tel:$phone")
        }
        return PendingIntent.getActivity(
            this, 1, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createMapPendingIntent(lat: Double, lng: Double): PendingIntent {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("geo:$lat,$lng?q=$lat,$lng")
        }
        return PendingIntent.getActivity(
            this, 2, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    // Removí el método getCurrentUserPhone() ya que no se usaba
}*/