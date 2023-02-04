package dev.gaborbiro.notes

import android.app.Application
import android.content.Context
import dev.gaborbiro.notes.store.db.AppDatabase
import dev.gaborbiro.notes.util.createNotificationChannels

class App : Application() {

    companion object {
        lateinit var appContext: Context
    }

    override fun onCreate() {
        super.onCreate()
        appContext = this
        AppDatabase.init(this)
        createNotificationChannels()
    }
}