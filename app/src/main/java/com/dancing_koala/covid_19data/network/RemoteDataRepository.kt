package com.dancing_koala.covid_19data.network

import com.dancing_koala.covid_19data.data.CsvDataParser
import com.dancing_koala.covid_19data.data.DailyReport
import com.dancing_koala.covid_19data.data.StateTimeSeries
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

class RemoteDataRepository() {
    private val service = JHUGithubService()
    private val csvDataParser = CsvDataParser()

    suspend fun getConfirmedTimeSeries(): List<StateTimeSeries> = withContext(Dispatchers.IO) {
        val result = service.fetchConfirmedTimeSeriesData()

        if (result is JHUGithubService.Result.Success) {
            return@withContext csvDataParser.parseTimeSeriesCsv(result.data)
        }

        return@withContext listOf<StateTimeSeries>()
    }

    suspend fun getDeathsTimeSeries(): List<StateTimeSeries> = withContext(Dispatchers.IO) {
        val result = service.fetchDeathsTimeSeriesData()

        if (result is JHUGithubService.Result.Success) {
            return@withContext csvDataParser.parseTimeSeriesCsv(result.data)
        }

        return@withContext listOf<StateTimeSeries>()
    }

    suspend fun getRecoveredTimeSeries(): List<StateTimeSeries> = withContext(Dispatchers.IO) {
        val result = service.fetchRecoveredTimeSeriesData()

        if (result is JHUGithubService.Result.Success) {
            return@withContext csvDataParser.parseTimeSeriesCsv(result.data)
        }

        return@withContext listOf<StateTimeSeries>()
    }

    suspend fun getLastDailyReports(): List<DailyReport> = withContext(Dispatchers.IO) {
        val cal = Calendar.getInstance()
        var result: JHUGithubService.Result? = null

        while (result == null || result.responseCode == 404) {
            cal.add(Calendar.DATE, -1)

            result = service.fetchDailyReportData(
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DATE)
            )
        }

        if (result is JHUGithubService.Result.Success) {
            return@withContext csvDataParser.parseDailyReportCsv(result.data)
        }

        return@withContext listOf<DailyReport>()
    }
}