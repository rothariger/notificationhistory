package com.truchisoft.notificationhistory.data.repositories

import com.truchisoft.notificationhistory.data.database.ObjectBox
import com.truchisoft.notificationhistory.data.database.entities.NotificationEntity
import com.truchisoft.notificationhistory.data.database.entities.NotificationEntity_
import io.objectbox.kotlin.flow
import io.objectbox.query.QueryBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepository @Inject constructor() {
    private val notificationBox = ObjectBox.getNotificationBox()

    fun getAllNotifications(): Flow<List<NotificationEntity>> =
        notificationBox.query()
            .orderDesc(NotificationEntity_.timestamp)
            .build()
            .flow()

    fun getNotificationsByApp(packageName: String): Flow<List<NotificationEntity>> =
        notificationBox.query()
            .equal(NotificationEntity_.appPackageName, packageName, QueryBuilder.StringOrder.CASE_SENSITIVE)
            .orderDesc(NotificationEntity_.timestamp)
            .build()
            .flow()

    fun getUnreadNotificationCountByApp(packageName: String): Flow<Int> =
        notificationBox.query()
            .equal(NotificationEntity_.appPackageName, packageName, QueryBuilder.StringOrder.CASE_SENSITIVE)
            .equal(NotificationEntity_.isRead, false)
            .build()
            .flow()
            .map { it.size }

    fun getTotalNotificationCountByApp(packageName: String): Flow<Int> =
        notificationBox.query()
            .equal(NotificationEntity_.appPackageName, packageName, QueryBuilder.StringOrder.CASE_SENSITIVE)
            .build()
            .flow()
            .map { it.size }

    suspend fun insertNotification(notification: NotificationEntity) = withContext(Dispatchers.IO) {
        // Asegurarse de que uniqueKey no sea nulo
        val notificationToInsert = if (notification.uniqueKey.isBlank()) {
            notification.copy(uniqueKey = generateFallbackUniqueKey(notification))
        } else {
            notification
        }

        // Verificar si ya existe una notificación con la misma clave única
        val existingNotification = notificationBox.query()
            .equal(NotificationEntity_.uniqueKey, notificationToInsert.uniqueKey, QueryBuilder.StringOrder.CASE_SENSITIVE)
            .build()
            .findFirst()

        if (existingNotification == null) {
            notificationBox.put(notificationToInsert)
        }
    }

    private fun generateFallbackUniqueKey(notification: NotificationEntity): String {
        return "${notification.appPackageName}_${notification.title}_${notification.content}"
    }

    suspend fun isDuplicate(packageName: String, title: String?, content: String?): Boolean = withContext(Dispatchers.IO) {
        // Buscar notificaciones con el mismo paquete, título y contenido
        val query = notificationBox.query()
            .equal(NotificationEntity_.appPackageName, packageName, QueryBuilder.StringOrder.CASE_SENSITIVE)

        if (!title.isNullOrEmpty()) {
            query.equal(NotificationEntity_.title, title, QueryBuilder.StringOrder.CASE_SENSITIVE)
        } else {
            query.isNull(NotificationEntity_.title)
        }

        if (!content.isNullOrEmpty()) {
            query.equal(NotificationEntity_.content, content, QueryBuilder.StringOrder.CASE_SENSITIVE)
        } else {
            query.isNull(NotificationEntity_.content)
        }

        val count = query.build().count()
        count > 0
    }

    suspend fun markAsRead(notificationId: Long) = withContext(Dispatchers.IO) {
        val notification = notificationBox.get(notificationId)
        notification?.let {
            it.isRead = true
            notificationBox.put(it)
        }
    }

    suspend fun markAllAsRead(packageName: String) = withContext(Dispatchers.IO) {
        val notifications = notificationBox.query()
            .equal(NotificationEntity_.appPackageName, packageName, QueryBuilder.StringOrder.CASE_SENSITIVE)
            .equal(NotificationEntity_.isRead, false)
            .build()
            .find()

        for (notification in notifications) {
            notification.isRead = true
        }

        notificationBox.put(notifications)
    }

    suspend fun deleteNotificationsByApp(packageName: String) = withContext(Dispatchers.IO) {
        val notificationsToDelete = notificationBox.query()
            .equal(NotificationEntity_.appPackageName, packageName, QueryBuilder.StringOrder.CASE_SENSITIVE)
            .build()
            .find()

        notificationBox.remove(notificationsToDelete)
    }

    suspend fun deleteNotification(notificationId: Long) = withContext(Dispatchers.IO) {
        notificationBox.remove(notificationId)
    }

    suspend fun notificationExists(uniqueKey: String): Boolean = withContext(Dispatchers.IO) {
        val count = notificationBox.query()
            .equal(NotificationEntity_.uniqueKey, uniqueKey, QueryBuilder.StringOrder.CASE_SENSITIVE)
            .build()
            .count()

        count > 0
    }
}
