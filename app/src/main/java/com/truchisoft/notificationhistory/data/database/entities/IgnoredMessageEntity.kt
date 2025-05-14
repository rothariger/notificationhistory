package com.truchisoft.notificationhistory.data.database.entities

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Index
import io.objectbox.annotation.Unique
import java.util.Date

@Entity
data class IgnoredMessageEntity(
    @Id var id: Long = 0,
    @Unique @Index var pattern: String,
    var isExactMatch: Boolean = false,
    var dateAdded: Date = Date()
)
