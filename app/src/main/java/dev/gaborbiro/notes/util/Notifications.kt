package dev.gaborbiro.notes.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat
import dev.gaborbiro.notes.R

private const val CHANNEL_ID_ACTIONS = "actions"
private const val NOTIFICATION_ID_ACTIONS = 1001

fun Context.createNotificationChannels() {
    val importance = NotificationManager.IMPORTANCE_DEFAULT
    val undoChannel = NotificationChannel(
        CHANNEL_ID_ACTIONS,
        "Actions",
        importance
    )

    val notificationManager: NotificationManager =
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.createNotificationChannels(
        listOf(
            undoChannel,
        )
    )
}

fun Context.showActionNotification(
    title: String,
    action: String,
    @DrawableRes actionIcon: Int,
    actionIntent: Intent
) {
    val pendingIntent = PendingIntent.getActivity(
        applicationContext,
        1,
        actionIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    val builder = NotificationCompat.Builder(this, CHANNEL_ID_ACTIONS)
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setContentTitle(title)
        .addAction(actionIcon, action, pendingIntent)
    getSystemService(NotificationManager::class.java).notify(
        NOTIFICATION_ID_ACTIONS,
        builder.build()
    )

}

fun Context.hideActionNotification() {
    getSystemService(NotificationManager::class.java).cancel(NOTIFICATION_ID_ACTIONS)
}