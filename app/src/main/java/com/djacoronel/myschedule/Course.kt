package com.djacoronel.myschedule

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import java.text.SimpleDateFormat
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

    fun getStartTime(): Long {
        if (schedule != "") {
            val split = schedule.split(" ")
            val startTimeString = split[0]
            val date = SimpleDateFormat("hh:mmaa", Locale.US).parse(startTimeString)
            return date.time
        }
        return 0
    }

    fun getReminderTime(): Long {
        if (schedule != "") {
            val startTime = schedule.split(" ")[0].substring(0, 5)
            val hour = startTime.split(":")[0].toInt()
            val minute = startTime.split(":")[1].toInt()
            val ampm = if (schedule.split(" ")[0].substring(6, 7) == "am") Calendar.AM else Calendar.PM

            val calendar = Calendar.getInstance()
            calendar.set(Calendar.DAY_OF_WEEK, getDayOfWeek())
            calendar.set(Calendar.HOUR, hour)
            calendar.set(Calendar.MINUTE, minute)
            calendar.set(Calendar.AM_PM, ampm)

            return calendar.timeInMillis - 15 * 60 * 1000
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