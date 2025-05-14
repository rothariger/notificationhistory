package com.truchisoft.notificationhistory.data.database

import android.content.Context
import com.truchisoft.notificationhistory.data.database.entities.AppEntity
import com.truchisoft.notificationhistory.data.database.entities.IgnoredMessageEntity
import com.truchisoft.notificationhistory.data.database.entities.MyObjectBox
import com.truchisoft.notificationhistory.data.database.entities.NotificationEntity
import io.objectbox.Box
import io.objectbox.BoxStore

object ObjectBox {
    lateinit var boxStore: BoxStore
        private set

    fun init(context: Context) {
        boxStore = MyObjectBox.builder()
            .androidContext(context.applicationContext)
            .build()
    }

    fun getAppBox(): Box<AppEntity> = boxStore.boxFor(AppEntity::class.java)

    fun getNotificationBox(): Box<NotificationEntity> = boxStore.boxFor(NotificationEntity::class.java)

    fun getIgnoredMessageBox(): Box<IgnoredMessageEntity> = boxStore.boxFor(IgnoredMessageEntity::class.java)
}
