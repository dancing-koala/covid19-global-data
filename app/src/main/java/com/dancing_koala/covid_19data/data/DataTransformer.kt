package com.dancing_koala.covid_19data.data

import java.util.*

class DataTransformer {

    private val dateStringFormat = "20%02d-%02d-%02d"

    fun transform(
        dailyReports: List<DailyReport>,
        confirmedTimeSeries: List<StateTimeSeries>,
        deathsTimeSeries: List<StateTimeSeries>,
        recoveredTimeSeries: List<StateTimeSeries>
    ): List<StateData> {
        val worldData = mutableListOf<StateData>()
        val sortedDailyReports = dailyReports.sortedBy { it.country.toLowerCase() + it.state }

        for ((i, dailyReport) in sortedDailyReports.withIndex()) {
            val localId = i + 1
            val remoteId = dailyReport.state + dailyReport.country

            val stateConfirmed = confirmedTimeSeries.firstOrNull { it.state + it.country == remoteId }
            val stateDeaths = deathsTimeSeries.firstOrNull { it.state + it.country == remoteId }
            val stateRecovered = recoveredTimeSeries.firstOrNull { it.state + it.country == remoteId }

            val stateData = StateData(
                localId = localId,
                state = dailyReport.state,
                country = dailyReport.country.capitalize(),
                lastUpdate = dailyReport.lastUpdate,
                confirmed = dailyReport.confirmed,
                deaths = dailyReport.deaths,
                recovered = dailyReport.recovered,
                latitude = dailyReport.latitude,
                longitude = dailyReport.longitude,
                confirmedPerDayData = transformTimeSeriesPerDayData(stateConfirmed?.perDayData),
                deathsPerDayData = transformTimeSeriesPerDayData(stateDeaths?.perDayData),
                recoveredPerDayData = transformTimeSeriesPerDayData(stateRecovered?.perDayData)
            )

            worldData.add(stateData)
        }

        var confirmed = 0
        var deaths = 0
        var recovered = 0
        var lastUpdate = Date(0)
        val confirmedPerDayData = hashMapOf<String, Int>()
        val deathsPerDayData = hashMapOf<String, Int>()
        val recoveredPerDayData = hashMapOf<String, Int>()

        worldData.forEach {
            confirmed += it.confirmed
            deaths += it.deaths
            recovered += it.recovered

            if (lastUpdate.time < it.lastUpdate.time) {
                lastUpdate = it.lastUpdate
            }

            confirmedPerDayData.aggregate(it.confirmedPerDayData)
            deathsPerDayData.aggregate(it.deathsPerDayData)
            recoveredPerDayData.aggregate(it.recoveredPerDayData)
        }

        val worldwide = StateData(
            0, "", "Worldwide", lastUpdate,
            confirmed, deaths, recovered, Double.NaN, Double.NaN,
            confirmedPerDayData, deathsPerDayData, recoveredPerDayData
        )

        worldData.add(0, worldwide)

        return worldData
    }

    private fun transformTimeSeriesPerDayData(perDayData: HashMap<String, Int>?): HashMap<String, Int> {
        val result = hashMapOf<String, Int>()

        perDayData ?: return result

        perDayData.keys.forEach { date ->
            result[formatDate(date)] = perDayData[date] ?: 0
        }

        return result
    }

    private fun formatDate(unformattedDate: String): String {
        val (month, day, year) = unformattedDate.split("/").map { it.toInt() }
        return String.format(Locale.US, dateStringFormat, year, month, day)
    }

    private fun HashMap<String, Int>.aggregate(other: HashMap<String, Int>) {
        other.keys.forEach { key ->
            this[key] = (this[key] ?: 0) + (other[key] ?: 0)
        }
    }
}