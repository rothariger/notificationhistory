package com.truchisoft.notificationhistory.data.database.entities

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Unique
import io.objectbox.relation.ToMany

@Entity
data class AppEntity(
    @Id var id: Long = 0,
    @Unique var packageName: String,
    var appName: String,
    var isTracked: Boolean = false
) {
    lateinit var notifications: ToMany<NotificationEntity>
}
