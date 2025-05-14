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
    var fullContent: String? = null,
    var extraData: String? = null,
    var timestamp: Date = Date(),
    @Index var appPackageName: String = "",
    @Index var isRead: Boolean = false,
    @Unique var uniqueKey: String = ""
) {
    lateinit var app: ToOne<AppEntity>
}
