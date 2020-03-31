package com.dancing_koala.covid_19data.persistence

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Cache(
    @PrimaryKey val key: String,
    val response: String
)