package com.dancing_koala.covid_19data.persistence

import androidx.room.*

@Dao
interface CacheDao {
    @Query("SELECT * FROM Cache")
    suspend fun getAll(): List<Cache>

    @Query("SELECT * FROM Cache WHERE Cache.`key` = :key")
    suspend fun getByKey(key: String): Cache?

    @Transaction
    @Insert
    fun insertEntry(cache: Cache)

    @Transaction
    @Update
    fun updateEntry(cache: Cache)

    @Transaction
    @Delete
    fun deleteEntry(cache: Cache)
}