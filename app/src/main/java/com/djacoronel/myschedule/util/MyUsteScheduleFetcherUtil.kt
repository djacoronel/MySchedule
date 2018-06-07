package com.djacoronel.myschedule.util

import com.djacoronel.myschedule.data.Course
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

/**
 * Created by djacoronel on 5/13/18.
 */
class MyUsteScheduleFetcherUtil {
    private val userAgent = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/43.0.2357.130 Safari/537.36"

    fun getCourses(cookie:String): List<Course>{
        val cookieName = cookie.split("=")[0]
        val cookieValue = cookie.split("=")[1]
        val courses = arrayListOf<Course>()

        HttpsTrustManager.allowAllSSL()
        val rows = getScheduleTableRows(cookieName, cookieValue)
        for (i in 1..rows.lastIndex) {
            addCourse(rows[i], courses)
        }

        return courses
    }

    private fun getScheduleTableRows(cookieName: String, cookieValue: String): Elements {
        val doc = Jsoup.connect("https://myuste.ust.edu.ph/student/mySchedule.jsp")
                .cookie(cookieName, cookieValue)
                .userAgent(userAgent)
                .get()

        return doc.select("table")[0].select("tr")
    }

    private fun addCourse(row: Element, courses: MutableList<Course>) {
        val schedule = row.select("td")[5].html()
        val scheduleSplit = schedule.split("<br>")

        for (split in scheduleSplit) {
            val split2 = split.trim().split(" ")

            var i = 0
            var day: String
            while (i < split2[0].length){
                val course = Course()
                course.code = row.select("td")[0].text()
                course.title = row.select("td")[1].text()
                course.section = row.select("td")[4].text()
                course.schedule = "${split2[1]} - ${split2[3]}"

                val charArray = split2[0].toCharArray()
                if (i+1 < split2[0].length &&
                        charArray[i].isUpperCase() &&
                        charArray[i+1].isLowerCase()){
                    day = "${charArray[i]}${charArray[i+1]}"
                    i+=2
                } else {
                    day = "${charArray[i]}"
                    i+=1
                }
                course.day = day

                var location = ""
                for (j in 4..split2.lastIndex) {
                    location += split2[j]
                    if (j != split2.lastIndex)
                        location += " "
                }
                course.location = location

                courses.add(course)
            }
        }
    }
}