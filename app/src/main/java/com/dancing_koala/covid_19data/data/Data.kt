package com.dancing_koala.covid_19data.data

import java.util.*

data class DailyReport(
    val state: String,
    val country: String,
    val lastUpdate: Date,
    val confirmed: Int,
    val deaths: Int,
    val recovered: Int,
    val active: Int,
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

data class AreaData(
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
    CASES("Cases"),
    RECOVERED("Recovered"),
    DEATHS("Deaths")
}

data class Country(
    val id: Int,
    val name: String,
    val iso2: String,
    val latitude: Double,
    val longitude: Double
)

data class DataSet(
    val id: Int,
    val country: Country,
    val cases: Int,
    val casesToday: Int,
    val deaths: Int,
    val deathsToday: Int,
    val recovered: Int,
    val recoveredToday: Int,
    val active: Int,
    val critical: Int,
    val casesPerOneMillion: Int,
    val casesTimeLine: HashMap<String, Int>,
    val deathsTimeLine: HashMap<String, Int>,
    val recoveredTimeLine: HashMap<String, Int>
)