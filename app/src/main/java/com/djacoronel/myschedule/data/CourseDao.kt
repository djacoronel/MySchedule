package com.djacoronel.myschedule.data

import android.arch.persistence.room.*

/**
 * Created by djacoronel on 5/8/18.
 */
@Dao
interface CourseDao{
    @Query("SELECT * FROM course")
    fun getCourses(): List<Course>

    @Query("SELECT * FROM course WHERE day = :day")
    fun getCourses(day: String): List<Course>

    @Query("SELECT * FROM course WHERE id = :id")
    fun getCourse(id: Long): Course

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCourse(course: Course)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateCourse(course: Course)

    @Delete
    fun deleteCourse(course: Course)

    @Query("DELETE FROM course")
    fun deleteAllCourses()
}