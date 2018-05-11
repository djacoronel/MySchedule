package com.djacoronel.myschedule

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.view.PagerAdapter
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import android.arch.persistence.room.Room
import android.content.Context
import android.util.Log
import org.jetbrains.anko.toast


class MainActivity : AppCompatActivity() {
    lateinit var viewPagerAdapter: ViewPagerAdapter
    lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = Room.databaseBuilder(applicationContext,
                AppDatabase::class.java, "myschedule-database").allowMainThreadQueries().build()

        setSupportActionBar(toolbar)
        viewPagerAdapter = ViewPagerAdapter()

        container.adapter = viewPagerAdapter
        container.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabs))
        tabs.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(container))

        showSchedule()
        setNotifications()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.action_settings) {
            return true
        } else if (id == R.id.action_fetch_schedule) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivityForResult(intent, 2)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (requestCode == 1) {
        } else if (requestCode == 2) {
            if (resultCode == Activity.RESULT_OK) {
                val studNo = data.getStringExtra("studNo")
                val password = data.getStringExtra("password")

                MyUsteScheduleFetcherTask(this).execute(studNo, password)
            }
        }
    }

    fun storeSchedule(courses: List<Course>) {
        db.CourseDao().deleteAllCourses()
        for (course in courses) {
            db.CourseDao().insertCourse(course)
        }
        showSchedule()
        setNotifications()
    }

    private fun showSchedule() {
        val courses = db.CourseDao().getCourses()

        val recyclerViews = mutableListOf<RecyclerView>()
        val days = listOf("M", "T", "W", "Th", "F", "S", "Su")
        for (i in 0..6) {

            val recycler = createRecycler()
            val adapter = (recycler.adapter as RecyclerAdapter)

            val coursesForDay = courses.filter { it.day == days[i] }
            adapter.replaceData(coursesForDay.sortedBy { it.getStartTime() })
            adapter.notifyDataSetChanged()

            recyclerViews.add(recycler)
        }
        viewPagerAdapter.replaceData(recyclerViews)
        viewPagerAdapter.notifyDataSetChanged()

        setCurrentDayOfWeek()
    }

    private fun setNotifications() {
        val courses = db.CourseDao().getCourses()

        for (course in courses) {
            val alarmIntent = Intent(this, AlarmReceiver::class.java)
            alarmIntent.putExtra("courseCode", course.code)
            alarmIntent.putExtra("location", course.location)
            alarmIntent.putExtra("schedule", course.schedule)

            val requestCode = courses.indexOf(course)
            val pendingIntent = PendingIntent.getBroadcast(this, requestCode, alarmIntent, PendingIntent.FLAG_ONE_SHOT)
            val manager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

            manager.set(AlarmManager.RTC_WAKEUP, course.getReminderTime(), pendingIntent)
            manager.setRepeating(AlarmManager.RTC_WAKEUP, course.getReminderTime(), AlarmManager.INTERVAL_DAY * 7, pendingIntent)

            Log.i(course.code, course.getReminderTime().toString())
        }
    }

    private fun setCurrentDayOfWeek() {
        val calendar = Calendar.getInstance()
        val day = calendar.get(Calendar.DAY_OF_WEEK)

        when (day) {
            Calendar.MONDAY -> container.setCurrentItem(0, true)
            Calendar.TUESDAY -> container.setCurrentItem(1, true)
            Calendar.WEDNESDAY -> container.setCurrentItem(2, true)
            Calendar.THURSDAY -> container.setCurrentItem(3, true)
            Calendar.FRIDAY -> container.setCurrentItem(4, true)
            Calendar.SATURDAY -> container.setCurrentItem(5, true)
            Calendar.SUNDAY -> container.setCurrentItem(6, true)
        }
    }

    private fun createRecycler(): RecyclerView {
        val courseRecyclerView = RecyclerView(this)
        courseRecyclerView.layoutManager = LinearLayoutManager(this)
        courseRecyclerView.adapter = RecyclerAdapter()

        val mDividerItemDecoration = DividerItemDecoration(
                courseRecyclerView.context,
                (courseRecyclerView.layoutManager as LinearLayoutManager).orientation
        )
        courseRecyclerView.addItemDecoration(mDividerItemDecoration)

        return courseRecyclerView
    }


    inner class ViewPagerAdapter : PagerAdapter() {
        private var mRecyclerList = mutableListOf<RecyclerView>()

        override fun getCount(): Int {
            return mRecyclerList.size
        }

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view === `object`
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val recyclerView = mRecyclerList[position]
            container.addView(recyclerView)
            return recyclerView
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(mRecyclerList[position])
        }

        override fun getItemPosition(`object`: Any): Int {
            return if (!mRecyclerList.contains(`object` as RecyclerView)) {
                POSITION_NONE
            } else {
                mRecyclerList.indexOf(`object`)
            }
        }

        fun replaceData(recyclerViews: MutableList<RecyclerView>) {
            mRecyclerList = recyclerViews
            notifyDataSetChanged()
        }
    }
}


