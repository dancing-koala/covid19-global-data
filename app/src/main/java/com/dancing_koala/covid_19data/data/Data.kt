package com.dancing_koala.covid_19data.data

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

data class StateData(
    val localId: Int,
    val state: String,
    val country: String,
    val lastUpdate: Date,
    val confirmed: Int,
    val deaths: Int,
    val recovered: Int,
    val latitude: Double,
    val longitude: Double,
    val confirmedPerDayData: HashMap<String, Int>,
    val deathsPerDayData: HashMap<String, Int>,
    val recoveredPerDayData: HashMap<String, Int>
) {
    val fullLabel: String = if (state.isEmpty() || country == state) country else "$state, $country"
}

enum class DataCategory(val label: String) {
    CONFIRMED("Confirmed"),
    RECOVERED("Recovered"),
    DEATHS("Deaths")
}