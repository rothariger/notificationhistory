package com.truchisoft.notificationhistory.data.database

import android.content.Context
import com.truchisoft.notificationhistory.data.database.entities.AppEntity
import com.truchisoft.notificationhistory.data.database.entities.IgnoredMessageEntity
import com.truchisoft.notificationhistory.data.database.entities.MyObjectBox
import com.truchisoft.notificationhistory.data.database.entities.NotificationEntity
import io.objectbox.Box
import io.objectbox.BoxStore

object ObjectBox {
    private lateinit var boxStore: BoxStore

    fun init(context: Context) {
        boxStore = MyObjectBox.builder()
            .androidContext(context.applicationContext)
            .build()
    }

    fun isInitialized(): Boolean = ::boxStore.isInitialized

    fun getBoxStore(): BoxStore = boxStore

    fun getAppBox(): Box<AppEntity> = boxStore.boxFor(AppEntity::class.java)

    fun getNotificationBox(): Box<NotificationEntity> = boxStore.boxFor(NotificationEntity::class.java)

    fun getIgnoredMessageBox(): Box<IgnoredMessageEntity> = boxStore.boxFor(IgnoredMessageEntity::class.java)
}
