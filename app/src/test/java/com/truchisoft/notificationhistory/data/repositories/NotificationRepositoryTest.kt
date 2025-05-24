package com.truchisoft.notificationhistory.data.repositories

import com.truchisoft.notificationhistory.data.database.MyObjectBox
import com.truchisoft.notificationhistory.data.database.ObjectBox
import com.truchisoft.notificationhistory.data.database.entities.NotificationEntity
import io.objectbox.Box
import io.objectbox.BoxStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.lang.reflect.Field
import java.lang.reflect.Method

@ExperimentalCoroutinesApi
class NotificationRepositoryTest {

    @TempDir
    lateinit var tempDir: File // JUnit 5 temporary directory

    private lateinit var store: BoxStore
    private lateinit var notificationBox: Box<NotificationEntity>
    private lateinit var repository: NotificationRepository
    private val testDispatcher = StandardTestDispatcher()


    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher) // Set main dispatcher for coroutines

        // Initialize ObjectBox store for testing in a temporary directory
        store = MyObjectBox.builder()
            .directory(tempDir)
            .build()
        ObjectBox.setStoreForTesting(store) // Crucial: Set this store for the ObjectBox singleton

        notificationBox = store.boxFor(NotificationEntity::class.java)
        repository = NotificationRepository() // Repository will use the store set in ObjectBox singleton
    }

    @AfterEach
    fun tearDown() {
        if (this::store.isInitialized && !store.isClosed) {
            store.close() // Close the store
            // store.deleteAllFiles() // This might be needed if tempDir is not cleaned automatically or for specific cases
        }
        ObjectBox.clearStoreForTesting() // Clear the testing store
        Dispatchers.resetMain() // Reset main dispatcher
        tempDir.deleteRecursively() // Clean up temp directory
    }

    @Test
    fun `generateFallbackUniqueKey should generate unique keys for the same notification due to timestamp`() {
        val notification = NotificationEntity(
            id = 0,
            appName = "TestApp",
            appPackageName = "com.testapp",
            title = "Test Title",
            content = "Test Content",
            timestamp = System.currentTimeMillis(),
            isRead = false,
            fullContent = "Full Test Content",
            extraData = "{}",
            uniqueKey = ""
        )

        val method: Method = NotificationRepository::class.java.getDeclaredMethod(
            "generateFallbackUniqueKey",
            NotificationEntity::class.java
        )
        method.isAccessible = true

        val key1 = method.invoke(repository, notification) as String
        Thread.sleep(5) // Ensure timestamp changes
        val key2 = method.invoke(repository, notification) as String
        Thread.sleep(5)
        val key3 = method.invoke(repository, notification) as String

        assertNotNull(key1)
        assertNotNull(key2)
        assertNotNull(key3)
        assertTrue(key1.startsWith("com.testapp_Test Title_Test Content_"))
        assertTrue(key2.startsWith("com.testapp_Test Title_Test Content_"))
        assertTrue(key3.startsWith("com.testapp_Test Title_Test Content_"))
        assertNotEquals(key1, key2, "Keys should be different due to timestamp (key1: $key1, key2: $key2).")
        assertNotEquals(key2, key3, "Keys should be different due to timestamp (key2: $key2, key3: $key3).")
        assertNotEquals(key1, key3, "Keys should be different due to timestamp (key1: $key1, key3: $key3).")
    }

    @Test
    fun `deleteNotifications should remove specified notifications and leave others`(): Unit = runBlocking {
        // Arrange: Insert some notifications
        val notification1 = NotificationEntity(appName = "App1", appPackageName = "pkg1", title = "Title1", content = "Content1", timestamp = 1000L, uniqueKey = "key1")
        val notification2 = NotificationEntity(appName = "App2", appPackageName = "pkg2", title = "Title2", content = "Content2", timestamp = 2000L, uniqueKey = "key2")
        val notification3 = NotificationEntity(appName = "App3", appPackageName = "pkg3", title = "Title3", content = "Content3", timestamp = 3000L, uniqueKey = "key3")
        val notification4 = NotificationEntity(appName = "App4", appPackageName = "pkg4", title = "Title4", content = "Content4", timestamp = 4000L, uniqueKey = "key4")

        // ObjectBox assigns IDs upon put
        val id1 = notificationBox.put(notification1)
        val id2 = notificationBox.put(notification2)
        val id3 = notificationBox.put(notification3)
        val id4 = notificationBox.put(notification4)

        assertEquals(4, notificationBox.count(), "Box should contain 4 notifications before deletion.")

        // Act: Delete notifications 1 and 3
        val idsToDelete = listOf(id1, id3)
        repository.deleteNotifications(idsToDelete)
        testDispatcher.scheduler.advanceUntilIdle() // Ensure coroutine launched by deleteNotifications completes

        // Assert
        assertEquals(2, notificationBox.count(), "Box should contain 2 notifications after deletion.")
        assertNull(notificationBox.get(id1), "Notification 1 should be deleted.")
        assertNotNull(notificationBox.get(id2), "Notification 2 should still exist.")
        assertNull(notificationBox.get(id3), "Notification 3 should be deleted.")
        assertNotNull(notificationBox.get(id4), "Notification 4 should still exist.")

        val remainingNotifications = notificationBox.all
        assertTrue(remainingNotifications.any { it.id == id2 })
        assertTrue(remainingNotifications.any { it.id == id4 })
        assertFalse(remainingNotifications.any { it.id == id1 })
        assertFalse(remainingNotifications.any { it.id == id3 })
    }
}
