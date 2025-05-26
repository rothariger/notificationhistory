package com.truchisoft.notificationhistory.di

import android.content.Context
import android.content.pm.PackageManager
import com.truchisoft.notificationhistory.data.database.ObjectBox
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.objectbox.BoxStore
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideObjectBoxStore(@ApplicationContext context: Context): BoxStore {
        if (!ObjectBox.isInitialized()) {
            ObjectBox.init(context)
        }
        return ObjectBox.getBoxStore()
    }

    @Provides
    @Singleton
    fun providePackageManager(@ApplicationContext context: Context): PackageManager {
        return context.packageManager
    }
}
