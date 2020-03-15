package com.dancing_koala.covid_19data

import java.util.*

data class DailyReport(
    val state: String,
    val country: String,
    val lastUpdate: Date,
    val confirmed: Int,
    val deaths: Int,
    val recovered: Int,
    val latitude: Double,
    val longitude: Double
)

data class StateTimeSeries(
    val state: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    val perDayData: HashMap<String, Int>
)