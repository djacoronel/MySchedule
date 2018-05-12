package com.djacoronel.myschedule

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import java.util.*

/**
 * Created by djacoronel on 5/6/18.
 */
@Entity
class Course {
    @PrimaryKey(autoGenerate = true)
    var id = 0
    var code = ""
    var title = ""
    var section = ""
    var day = ""
    var schedule = ""
    var location = ""

    fun getReminderTime(): Long {
        if (schedule != "") {
            val startTime = schedule.split(" ")[0].substring(0, 5)
            val hour = startTime.split(":")[0].toInt()
            val minute = startTime.split(":")[1].toInt()
            val ampm = if (schedule.split(" ")[0].substring(5, 7) == "am") Calendar.AM else Calendar.PM

            val adjustedHour = if (hour == 12) 0 else hour

            val calendar = Calendar.getInstance()
            calendar.set(Calendar.DAY_OF_WEEK, getDayOfWeek())
            calendar.set(Calendar.HOUR, adjustedHour)
            calendar.set(Calendar.MINUTE, minute)
            calendar.set(Calendar.AM_PM, ampm)

            val reminderTime = calendar.timeInMillis
            val duration15mins = 15 * 60 * 1000
            val duration1week = 7 * 24 * 60 * 60 * 1000

            return if (reminderTime < System.currentTimeMillis())
                reminderTime + duration1week - duration15mins
            else
                reminderTime - duration15mins
        }
        return 0
    }

    private fun getDayOfWeek(): Int {
        return when (day) {
            "M" -> Calendar.MONDAY
            "T" -> Calendar.TUESDAY
            "W" -> Calendar.WEDNESDAY
            "Th" -> Calendar.THURSDAY
            "F" -> Calendar.FRIDAY
            "S" -> Calendar.SATURDAY
            "Su" -> Calendar.SUNDAY
            else -> 0
        }
    }
}