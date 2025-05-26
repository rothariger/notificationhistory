package com.truchisoft.notificationhistory.data.database.entities

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Index
import io.objectbox.annotation.Unique
import java.util.Date

@Entity
data class IgnoredMessageEntity(
    @Id var id: Long = 0,
    @Unique @Index var pattern: String, // Patrón a ignorar (título o contenido)
    var isExactMatch: Boolean = false, // Si es true, debe coincidir exactamente; si es false, contiene
    var dateAdded: Date = Date()
)
