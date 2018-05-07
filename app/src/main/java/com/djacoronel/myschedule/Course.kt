package com.djacoronel.myschedule

import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by djacoronel on 5/6/18.
 */
class Course(){
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