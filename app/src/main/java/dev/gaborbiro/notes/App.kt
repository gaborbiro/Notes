package dev.gaborbiro.notes

import android.app.Application
import dev.gaborbiro.notes.store.db.AppDatabase
import dev.gaborbiro.notes.util.createNotificationChannels

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        AppDatabase.init(this)
        createNotificationChannels()
    }
}