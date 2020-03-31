package com.dancing_koala.covid_19data.data

import java.util.*

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
)

enum class DataCategory(val label: String) {
    CASES("Cases"), DEATHS("Deaths")
}

data class Country(
    val id: Int,
    val name: String,
    val iso2: String,
    val latitude: Double,
    val longitude: Double
)

data class ReportDataSet(
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
    val deathsPerOneMillion: Int
)

class TimeLine : HashMap<String, Int>()

data class TimeLineDataSet(
    val id: Int,
    val provinceName: String,
    val countryName: String,
    val casesTimeLine: TimeLine,
    val deathsTimeLine: TimeLine
) {
    val locationName = if (provinceName.isEmpty() || provinceName == countryName) {
        countryName.capitalize()
    } else {
        "${countryName.capitalize()}, ${provinceName.capitalize()}"
    }
}