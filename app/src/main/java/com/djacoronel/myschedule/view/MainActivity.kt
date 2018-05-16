package com.djacoronel.myschedule.view

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.arch.persistence.room.Room
import android.content.Context
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
import com.djacoronel.myschedule.*
import com.djacoronel.myschedule.data.AppDatabase
import com.djacoronel.myschedule.data.Course
import com.djacoronel.myschedule.util.AlarmReceiver
import com.djacoronel.myschedule.util.MyUsteScheduleFetcherUtil
import com.google.android.gms.ads.AdRequest
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.toast
import java.util.*


class MainActivity : AppCompatActivity() {
    private lateinit var viewPagerAdapter: ViewPagerAdapter
    private var disposables = CompositeDisposable()
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = Room.databaseBuilder(applicationContext,
                AppDatabase::class.java, "myschedule-database").build()

        setSupportActionBar(toolbar)
        viewPagerAdapter = ViewPagerAdapter()

        container.adapter = viewPagerAdapter
        container.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabs))
        tabs.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(container))

        setupAds()
        showSchedule()
        setNotifications()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.action_fetch_schedule) {
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
                disposables.add(
                        Observable.just(data)
                                .map { intent ->
                                    val studNo = intent.getStringExtra("studNo")
                                    val password = intent.getStringExtra("password")

                                    MyUsteScheduleFetcherUtil().getCourses(studNo, password)
                                }
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .doOnSubscribe { loading.visibility = View.VISIBLE }
                                .subscribe(
                                        { courses ->
                                            storeSchedule(courses)
                                            loading.visibility = View.GONE
                                        },
                                        { throwable ->
                                            throwable.printStackTrace()
                                            loading.visibility = View.GONE
                                            toast("Failed to fetch schedule :/")
                                        }
                                )
                )
            }
        }
    }

    private fun storeSchedule(courses: List<Course>) {
        disposables.add(
                Observable.just(courses)
                        .flatMapIterable { course -> course }
                        .map { course -> db.CourseDao().insertCourse(course) }
                        .doOnSubscribe { db.CourseDao().deleteAllCourses() }
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({}, {}, {
                            showSchedule()
                            setNotifications()
                        })
        )
    }

    private fun showSchedule() {
        disposables.add(
                Observable.just("M", "T", "W", "Th", "F", "S", "Su")
                        .map { day -> db.CourseDao().getCourses(day) }
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnSubscribe { viewPagerAdapter.clearData() }
                        .doFinally { setCurrentDayOfWeek() }
                        .subscribe { courses ->
                            val recycler = createRecycler()
                            val adapter = (recycler.adapter as RecyclerAdapter)
                            adapter.replaceData(courses.sortedBy { it.getStartTime() })
                            viewPagerAdapter.addRecycler(recycler)
                        }
        )
    }

    private fun setNotifications() {
        disposables.add(
                Observable.just(db)
                        .map { db -> db.CourseDao().getCourses() }
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .flatMapIterable { course -> course }
                        .subscribe { course ->
                            val alarmIntent = Intent(this, AlarmReceiver::class.java)
                            alarmIntent.putExtra("courseCode", course.code)
                            alarmIntent.putExtra("location", course.location)
                            alarmIntent.putExtra("schedule", course.schedule)

                            val requestCode = course.hashCode()
                            val pendingIntent = PendingIntent.getBroadcast(this, requestCode, alarmIntent, PendingIntent.FLAG_ONE_SHOT)
                            val manager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

                            manager.set(AlarmManager.RTC_WAKEUP, course.getReminderTime(), pendingIntent)
                            manager.setRepeating(AlarmManager.RTC_WAKEUP, course.getReminderTime(), AlarmManager.INTERVAL_DAY * 7, pendingIntent)
                        }
                )
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

    private fun setupAds() {
        val adRequest = AdRequest.Builder()
                .addTestDevice("CEA54CA528FB019B75536189748EAF7E")
                .addTestDevice("2F42DCE5AF01E77FB3B1748FFD2BFB08")
                .addTestDevice("4CCC112819318A806ADC4807B6A0C444")
                .build()

        adView.loadAd(adRequest)
    }

    public override fun onPause() {
        if (adView != null) {
            adView.pause()
        }
        super.onPause()
    }

    public override fun onResume() {
        super.onResume()
        if (adView != null) {
            adView.resume()
        }
    }

    public override fun onDestroy() {
        disposables.clear()
        if (adView != null) {
            adView.destroy()
        }
        super.onDestroy()
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

        override fun destroyItem(container: ViewGroup, position: Int, view: Any) {
            container.removeView(view as View)
        }

        override fun getItemPosition(`object`: Any): Int {
            return if (!mRecyclerList.contains(`object` as RecyclerView)) {
                POSITION_NONE
            } else {
                mRecyclerList.indexOf(`object`)
            }
        }

        fun clearData() {
            mRecyclerList = mutableListOf()
            notifyDataSetChanged()
        }

        fun addRecycler(recyclerView: RecyclerView) {
            mRecyclerList.add(recyclerView)
            notifyDataSetChanged()
        }
    }
}


