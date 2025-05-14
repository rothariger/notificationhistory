package com.truchisoft.notificationhistory.services

import android.app.Notification
import android.content.pm.PackageManager
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.truchisoft.notificationhistory.data.database.entities.AppEntity
import com.truchisoft.notificationhistory.data.database.entities.NotificationEntity
import com.truchisoft.notificationhistory.data.repositories.AppRepository
import com.truchisoft.notificationhistory.data.repositories.IgnoredMessageRepository
import com.truchisoft.notificationhistory.data.repositories.NotificationRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.security.MessageDigest
import javax.inject.Inject

@AndroidEntryPoint
class NotificationListenerService : NotificationListenerService() {

    @Inject
    lateinit var appRepository: AppRepository

    @Inject
    lateinit var notificationRepository: NotificationRepository

    @Inject
    lateinit var ignoredMessageRepository: IgnoredMessageRepository

    private val serviceScope = CoroutineScope(Dispatchers.IO)

    // Lista de patrones a ignorar en las notificaciones
    private val ignorePatterns = listOf(
        "is running in the background",
        "is doing work in the background",
        "is running",
        "running in the background",
        "using battery"
    )

    // Tiempo mínimo entre notificaciones idénticas (en milisegundos)
    private val DUPLICATE_TIME_WINDOW = 5 * 60 * 1000 // 5 minutos

    // Caché de notificaciones recientes para evitar duplicados
    private val recentNotifications = mutableMapOf<String, Long>()

    override fun onCreate() {
        super.onCreate()
        Log.d("NotificationService", "Servicio de notificaciones iniciado")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val packageName = sbn.packageName
        val notification = sbn.notification

        // Ignorar notificaciones de nuestra propia app
        if (packageName == applicationContext.packageName) {
            return
        }

        Log.d("NotificationService", "Notificación recibida de: $packageName")

        serviceScope.launch {
            val app = appRepository.getAppByPackageName(packageName)

            if (app == null) {
                // Primera vez que vemos esta app, la añadimos a la base de datos
                try {
                    val appInfo = packageManager.getApplicationInfo(packageName, 0)
                    val appName = packageManager.getApplicationLabel(appInfo).toString()
                    Log.d("NotificationService", "Añadiendo nueva app: $appName")
                    appRepository.insertApp(
                        AppEntity(
                            packageName = packageName,
                            appName = appName,
                            isTracked = false
                        )
                    )
                } catch (e: PackageManager.NameNotFoundException) {
                    // App no encontrada, usamos el nombre del paquete como fallback
                    Log.d("NotificationService", "App no encontrada, usando packageName: $packageName")
                    appRepository.insertApp(
                        AppEntity(
                            packageName = packageName,
                            appName = packageName,
                            isTracked = false
                        )
                    )
                }
            }

            // Comprobamos si esta app está siendo rastreada
            val trackedApp = app ?: appRepository.getAppByPackageName(packageName)
            if (trackedApp?.isTracked == true) {
                // Guardamos la notificación
                val extras = notification.extras
                val title = extras.getString(Notification.EXTRA_TITLE)
                val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
                val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()
                val fullContent = bigText ?: text

                // Verificar si debemos ignorar esta notificación
                if (shouldIgnoreNotification(title, text)) {
                    Log.d("NotificationService", "Ignorando notificación de trabajo en segundo plano")
                    return@launch
                }

                // Verificar si está en el diccionario de mensajes ignorados
                if (ignoredMessageRepository.shouldIgnoreMessage(title, text)) {
                    Log.d("NotificationService", "Ignorando notificación que está en el diccionario")
                    return@launch
                }

                // Crear una clave única basada solo en el contenido (sin timestamp)
                val contentKey = generateContentKey(packageName, title, text)

                // Verificar si es un duplicado reciente
                val currentTime = System.currentTimeMillis()
                val lastSeenTime = recentNotifications[contentKey]

                if (lastSeenTime != null && (currentTime - lastSeenTime) < DUPLICATE_TIME_WINDOW) {
                    Log.d("NotificationService", "Ignorando notificación duplicada reciente")
                    return@launch
                }

                // Actualizar la caché de notificaciones recientes
                recentNotifications[contentKey] = currentTime

                // Limpiar entradas antiguas de la caché
                cleanupRecentNotificationsCache(currentTime)

                // Verificar si ya existe en la base de datos
                if (notificationRepository.isDuplicate(packageName, title, text)) {
                    Log.d("NotificationService", "Ignorando notificación duplicada en la base de datos")
                    return@launch
                }

                Log.d("NotificationService", "Guardando notificación: $title")

                // Extraer todos los datos adicionales
                val extraData = extractExtraData(extras)

                val notificationEntity = NotificationEntity(
                    appPackageName = packageName,
                    title = title,
                    content = text,
                    fullContent = fullContent,
                    extraData = extraData,
                    isRead = false,
                    uniqueKey = contentKey
                )

                notificationRepository.insertNotification(notificationEntity)
            } else {
                Log.d("NotificationService", "App no rastreada, ignorando notificación")
            }
        }
    }

    private fun shouldIgnoreNotification(title: String?, content: String?): Boolean {
        // Si el título o contenido es nulo, no ignoramos
        if (title == null && content == null) return false

        // Verificar si alguno de los patrones a ignorar está en el título o contenido
        return ignorePatterns.any { pattern ->
            (title?.contains(pattern, ignoreCase = true) == true) ||
                    (content?.contains(pattern, ignoreCase = true) == true)
        }
    }

    private fun extractExtraData(extras: Bundle): String {
        val jsonObject = JSONObject()

        for (key in extras.keySet()) {
            val value = extras.get(key)
            if (value != null) {
                try {
                    jsonObject.put(key, value.toString())
                } catch (e: Exception) {
                    // Ignorar errores al convertir a JSON
                }
            }
        }

        return jsonObject.toString()
    }

    private fun generateContentKey(packageName: String, title: String?, content: String?): String {
        // Generar una clave única basada solo en el contenido, sin incluir el timestamp
        val input = "$packageName|$title|$content"
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(input.toByteArray())
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }

    private fun cleanupRecentNotificationsCache(currentTime: Long) {
        // Eliminar entradas antiguas de la caché
        val keysToRemove = mutableListOf<String>()

        for ((key, timestamp) in recentNotifications) {
            if (currentTime - timestamp > DUPLICATE_TIME_WINDOW) {
                keysToRemove.add(key)
            }
        }

        for (key in keysToRemove) {
            recentNotifications.remove(key)
        }
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d("NotificationService", "Listener conectado")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.d("NotificationService", "Listener desconectado")
    }
}
