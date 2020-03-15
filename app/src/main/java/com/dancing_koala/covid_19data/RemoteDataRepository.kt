package com.dancing_koala.covid_19data

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

    suspend fun getLastDailyReport(): List<DailyReport> = withContext(Dispatchers.IO) {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DATE, -1)

        val result = service.fetchDailyReportData(
            cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DATE)
        )

        if (result is JHUGithubService.Result.Success) {
            return@withContext csvDataParser.parseDailyReportCsv(result.data)
        }

        return@withContext listOf<DailyReport>()
    }
}