package com.dancing_koala.covid_19data.persistence

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [Cache::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cacheDao(): CacheDao
}