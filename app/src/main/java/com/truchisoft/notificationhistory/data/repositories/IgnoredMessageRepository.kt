package com.truchisoft.notificationhistory.data.repositories

import com.truchisoft.notificationhistory.data.database.ObjectBox
import com.truchisoft.notificationhistory.data.database.entities.IgnoredMessageEntity
import com.truchisoft.notificationhistory.data.database.entities.IgnoredMessageEntity_
import io.objectbox.kotlin.flow
import io.objectbox.query.QueryBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IgnoredMessageRepository @Inject constructor() {
    private val ignoredMessageBox = ObjectBox.getIgnoredMessageBox()

    fun getAllIgnoredMessages(): Flow<List<IgnoredMessageEntity>> =
        ignoredMessageBox.query()
            .orderDesc(IgnoredMessageEntity_.dateAdded)
            .build()
            .flow()

    suspend fun addIgnoredMessage(pattern: String, isExactMatch: Boolean = false) = withContext(Dispatchers.IO) {
        // Verificar si ya existe
        val existingPattern = ignoredMessageBox.query()
            .equal(IgnoredMessageEntity_.pattern, pattern, QueryBuilder.StringOrder.CASE_SENSITIVE)
            .build()
            .findFirst()

        if (existingPattern == null) {
            val ignoredMessage = IgnoredMessageEntity(
                pattern = pattern,
                isExactMatch = isExactMatch
            )
            ignoredMessageBox.put(ignoredMessage)
        }
    }

    suspend fun removeIgnoredMessage(id: Long) = withContext(Dispatchers.IO) {
        ignoredMessageBox.remove(id)
    }

    suspend fun shouldIgnoreMessage(title: String?, content: String?): Boolean = withContext(Dispatchers.IO) {
        if (title == null && content == null) return@withContext false

        val ignoredMessages = ignoredMessageBox.all

        for (ignoredMessage in ignoredMessages) {
            if (ignoredMessage.isExactMatch) {
                // Coincidencia exacta
                if (title == ignoredMessage.pattern || content == ignoredMessage.pattern) {
                    return@withContext true
                }
            } else {
                // Coincidencia parcial
                if ((title?.contains(ignoredMessage.pattern, ignoreCase = true) == true) ||
                    (content?.contains(ignoredMessage.pattern, ignoreCase = true) == true)) {
                    return@withContext true
                }
            }
        }

        return@withContext false
    }
}
