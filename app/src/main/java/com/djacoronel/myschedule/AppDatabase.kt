package com.djacoronel.myschedule

import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.Database



/**
 * Created by djacoronel on 5/8/18.
 */
@Database(entities = [(Course::class)], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun CourseDao(): CourseDao
}