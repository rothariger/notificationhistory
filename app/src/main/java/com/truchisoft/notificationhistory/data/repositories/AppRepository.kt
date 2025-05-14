package com.truchisoft.notificationhistory.data.repositories

import com.truchisoft.notificationhistory.data.database.ObjectBox
import com.truchisoft.notificationhistory.data.database.entities.AppEntity
import com.truchisoft.notificationhistory.data.database.entities.AppEntity_
import io.objectbox.kotlin.flow
import io.objectbox.query.QueryBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppRepository @Inject constructor() {
    private val appBox = ObjectBox.getAppBox()

    fun getAllApps(): Flow<List<AppEntity>> =
        appBox.query().order(AppEntity_.appName).build().flow()

    fun getTrackedApps(): Flow<List<AppEntity>> =
        appBox.query()
            .equal(AppEntity_.isTracked, true)
            .order(AppEntity_.appName)
            .build()
            .flow()

    suspend fun insertApp(app: AppEntity) = withContext(Dispatchers.IO) {
        appBox.put(app)
    }

    suspend fun updateApp(app: AppEntity) = withContext(Dispatchers.IO) {
        appBox.put(app)
    }

    suspend fun getAppByPackageName(packageName: String): AppEntity? = withContext(Dispatchers.IO) {
        appBox.query()
            .equal(AppEntity_.packageName, packageName, QueryBuilder.StringOrder.CASE_SENSITIVE)
            .build()
            .findFirst()
    }

    suspend fun toggleAppTracking(packageName: String, isTracked: Boolean) = withContext(Dispatchers.IO) {
        val app = getAppByPackageName(packageName)
        app?.let {
            it.isTracked = isTracked
            appBox.put(it)
        }
    }
}
