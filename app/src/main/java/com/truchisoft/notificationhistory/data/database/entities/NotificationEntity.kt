package com.truchisoft.notificationhistory.data.database.entities

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Index
import io.objectbox.annotation.Unique
import io.objectbox.relation.ToOne
import java.util.Date

@Entity
data class NotificationEntity(
    @Id var id: Long = 0,
    var title: String? = null,
    var content: String? = null,
    var fullContent: String? = null, // Contenido completo de la notificación
    var extraData: String? = null, // Datos adicionales en formato JSON
    var timestamp: Date = Date(),
    @Index var appPackageName: String = "",
    @Index var isRead: Boolean = false, // Indica si la notificación ha sido leída
    @Unique var uniqueKey: String = "" // Clave única para evitar duplicados
) {
    // Relación con la app
    lateinit var app: ToOne<AppEntity>
}
