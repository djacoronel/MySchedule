package com.djacoronel.myschedule.util

import android.app.ProgressDialog
import android.os.AsyncTask
import com.djacoronel.myschedule.data.Course
import com.djacoronel.myschedule.view.MainActivity
import org.jetbrains.anko.toast
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import java.lang.ref.WeakReference

/**
 * Created by djacoronel on 5/7/18.
 */
class MyUsteScheduleFetcherTask(activity: MainActivity) : AsyncTask<String, Void, List<Course>>() {
    private var progressDialog = ProgressDialog(activity)
    private var weakActivity = WeakReference<MainActivity>(activity)
    private val userAgent = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/43.0.2357.130 Safari/537.36"


    override fun onPreExecute() {
        super.onPreExecute()
        progressDialog.setTitle("Fetching grades from MyUste")
        progressDialog.setMessage("Loading...")
        progressDialog.isIndeterminate = false
        progressDialog.show()
    }

    override fun doInBackground(vararg params: String): List<Course> {
        val studNo = params[0]
        val password = params[1]
        val courses = arrayListOf<Course>()

        try {
            HttpsTrustManager.allowAllSSL()
            val cookies = getCookies()
            val rows = loginAndGetScheduleTableRows(studNo, password, cookies)
            for (i in 1..rows.lastIndex) {
                addCourse(rows[i], courses)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return courses
    }

    private fun getCookies(): MutableMap<String, String> {
        val url = "https://myuste.ust.edu.ph/student"
        val response = Jsoup.connect(url).userAgent(userAgent)
                .method(Connection.Method.GET)
                .execute()

        return response.cookies()
    }

    private fun loginAndGetScheduleTableRows(studNo: String, password: String,
                                             cookies: MutableMap<String, String>): Elements {
        Jsoup.connect("https://myuste.ust.edu.ph/student/loginProcess")
                .cookies(cookies)
                .data("txtUsername", studNo)
                .data("txtPassword", password)
                .userAgent(userAgent)
                .method(Connection.Method.POST)
                .followRedirects(true)
                .execute()

        val doc = Jsoup.connect("https://myuste.ust.edu.ph/student/mySchedule.jsp")
                .cookies(cookies)
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

    override fun onPostExecute(courses: List<Course>) {
        super.onPostExecute(courses)

        if (courses.isEmpty())
            weakActivity.get()?.toast("Failed to fetch schedule. Check your internet connection.")
        else
            weakActivity.get()?.storeSchedule(courses)


        weakActivity.get()?.let {
            if (it.isFinishing || it.isDestroyed) {
                return
            }
            if (progressDialog.isShowing) {
                progressDialog.dismiss()
            }
        }
    }
}