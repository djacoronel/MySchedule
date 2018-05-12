package com.djacoronel.myschedule

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v4.app.NotificationCompat

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val courseCode = intent.getStringExtra("courseCode")
        val location = intent.getStringExtra("location")
        val schedule = intent.getStringExtra("schedule")

        val channelId = "channel_00"
        val name = "MySchedule Notifications"

        val notifIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 0, notifIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val notification = NotificationCompat.Builder(context, channelId)
                .setStyle(NotificationCompat.BigTextStyle())
                .setContentTitle(courseCode)
                .setContentText(location+ "\n"+ schedule)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setSmallIcon(R.drawable.ic_event_black_24dp)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, name, NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0, notification)
    }
}
