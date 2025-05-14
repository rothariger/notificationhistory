package com.truchisoft.notificationhistory

import android.app.Application
import com.truchisoft.notificationhistory.data.database.ObjectBox
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class NotificationHistoryApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Inicializar ObjectBox
        ObjectBox.init(this)
    }
}
