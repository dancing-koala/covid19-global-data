package com.dancing_koala.covid_19data.network.jhu

import com.dancing_koala.covid_19data.network.BaseService
import okhttp3.OkHttpClient
import java.util.*

class JHUGithubService : BaseService() {
    override val okHttpClient = OkHttpClient()

    private val baseUrl = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data"
    private val timeSeriesPath = "csse_covid_19_time_series"
    private val dailyReportsPath = "csse_covid_19_daily_reports"

    private val confirmedTimeSeriesURL = "$baseUrl/$timeSeriesPath/time_series_19-covid-Confirmed.csv"
    private val deathsTimeSeriesURL = "$baseUrl/$timeSeriesPath/time_series_19-covid-Deaths.csv"
    private val recoveredTimeSeriesURL = "$baseUrl/$timeSeriesPath/time_series_19-covid-Recovered.csv"

    suspend fun fetchConfirmedTimeSeriesData(): Result = fetchContent(confirmedTimeSeriesURL)
    suspend fun fetchDeathsTimeSeriesData(): Result = fetchContent(deathsTimeSeriesURL)
    suspend fun fetchRecoveredTimeSeriesData(): Result = fetchContent(recoveredTimeSeriesURL)

    suspend fun fetchDailyReportData(year: Int, month: Int, day: Int): Result {
        val fileName = String.format(Locale.US, "%02d-%02d-%04d.csv", month, day, year)
        return fetchContent("$baseUrl/$dailyReportsPath/$fileName")
    }
}