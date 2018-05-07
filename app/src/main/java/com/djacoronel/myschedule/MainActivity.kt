package com.djacoronel.myschedule

import android.app.Activity
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

class MainActivity : AppCompatActivity() {
    lateinit var viewPagerAdapter: ViewPagerAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar)
        viewPagerAdapter = ViewPagerAdapter()
        
        container.adapter = viewPagerAdapter
        container.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabs))
        tabs.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(container))
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

    fun showSchedule(courses: List<Course>) {
        val days = listOf("M", "T", "W", "Th", "F", "S", "Su")
        for (i in 0..6) {

            val recycler = createRecycler()
            val adapter = (recycler.adapter as RecyclerAdapter)

            val coursesForDay = courses.filter { it.day == days[i] }
            adapter.replaceData(coursesForDay.sortedBy { it.getStartTime() })
            adapter.notifyDataSetChanged()

            viewPagerAdapter.addRecycler(recycler)
            viewPagerAdapter.notifyDataSetChanged()
        }
    }
    
    private fun createRecycler(): RecyclerView{
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



    inner class ViewPagerAdapter: PagerAdapter(){
        private val mRecyclerList = mutableListOf<RecyclerView>()

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
        
        fun addRecycler(recyclerView: RecyclerView){
            mRecyclerList.add(recyclerView)
            notifyDataSetChanged()
        }
    }
}


