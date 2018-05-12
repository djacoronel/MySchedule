package com.djacoronel.myschedule.view

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.djacoronel.myschedule.data.Course
import com.djacoronel.myschedule.R
import kotlinx.android.synthetic.main.course_layout.view.*

/**
 * Created by djacoronel on 5/6/18.
 */

class RecyclerAdapter : RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {
    var courses = listOf<Course>()

    //extension function to simplify view inflation in an adapter
    private fun ViewGroup.inflate(layoutRes: Int): View {
        return LayoutInflater.from(context).inflate(layoutRes, this, false)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ViewHolder(parent.inflate(R.layout.course_layout))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
            holder.bind(courses[position])

    override fun getItemCount() = courses.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(course: Course) = with(itemView) {

            val titleText = "${course.title} (${course.code})"
            course_title.text = titleText
            section.text = course.section
            schedule.text = course.schedule
            location.text = course.location
        }
    }

    fun replaceData(courses: List<Course>){
        this.courses = courses
    }
}
