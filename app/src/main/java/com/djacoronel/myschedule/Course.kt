package com.djacoronel.myschedule

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by djacoronel on 5/6/18.
 */
@Entity
class Course{
    @PrimaryKey(autoGenerate = true)
    var id = 0
    var code = ""
    var title = ""
    var section = ""
    var day = ""
    var schedule = ""
    var location = ""

    fun getStartTime(): Long{
        if(schedule!=""){
            val split = schedule.split(" ")
            val startTimeString = split[0]

            val date = SimpleDateFormat("hh:mmaa",Locale.US).parse(startTimeString)
            return date.time
        }

        return 0
    }
}