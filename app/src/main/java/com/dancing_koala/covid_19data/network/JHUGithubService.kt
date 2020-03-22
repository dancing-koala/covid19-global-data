package com.dancing_koala.covid_19data.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.CacheControl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.*
import java.util.concurrent.TimeUnit

class JHUGithubService {

    private val okHttpClient = OkHttpClient()

    private val baseUrl = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data"
    private val timeSeriesPath = "csse_covid_19_time_series"
    private val dailyReportsPath = "csse_covid_19_daily_reports"

    private val confirmedTimeSeriesURL = "$baseUrl/$timeSeriesPath/time_series_19-covid-Confirmed.csv"
    private val deathsTimeSeriesURL = "$baseUrl/$timeSeriesPath/time_series_19-covid-Deaths.csv"
    private val recoveredTimeSeriesURL = "$baseUrl/$timeSeriesPath/time_series_19-covid-Recovered.csv"

    private suspend fun fetchContent(url: String): Result =
        withContext(Dispatchers.IO) {
            val request = Request.Builder()
                .url(url)
                .cacheControl(
                    CacheControl.Builder()
                        .maxAge(1, TimeUnit.HOURS)
                        .build()
                )
                .build()

            try {
                val response = okHttpClient.newCall(request).execute()
                val data = response.body?.string() ?: ""
                response.close()

                if (response.code >= 400) {
                    return@withContext if (response.code >= 500) {
                        Result.Error.HttpServerError(responseCode = response.code, url = url)
                    } else {
                        Result.Error.HttpClientError(responseCode = response.code, url = url)
                    }
                }

                return@withContext Result.Success(responseCode = response.code, data = data)
            } catch (e: Exception) {
                return@withContext Result.Error.UnknownError(url = url, throwable = e)
            }
        }

    suspend fun fetchConfirmedTimeSeriesData(): Result = fetchContent(confirmedTimeSeriesURL)
    suspend fun fetchDeathsTimeSeriesData(): Result = fetchContent(deathsTimeSeriesURL)
    suspend fun fetchRecoveredTimeSeriesData(): Result = fetchContent(recoveredTimeSeriesURL)

    suspend fun fetchDailyReportData(year: Int, month: Int, day: Int): Result {
        val fileName = String.format(Locale.US, "%02d-%02d-%04d.csv", month, day, year)
        return fetchContent("$baseUrl/$dailyReportsPath/$fileName")
    }

    sealed class Result(val responseCode: Int) {
        class Success(responseCode: Int, val data: String) : Result(responseCode)
        sealed class Error(responseCode: Int, val url: String) : Result(responseCode) {
            class HttpClientError(responseCode: Int, url: String) : Error(responseCode, url)
            class HttpServerError(responseCode: Int, url: String) : Error(responseCode, url)
            class UnknownError(url: String, val throwable: Throwable) : Error(-1, url)
        }
    }
}