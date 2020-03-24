package com.dancing_koala.covid_19data.network.jhu

import com.dancing_koala.covid_19data.data.DailyReport
import com.dancing_koala.covid_19data.data.JHUDataParser
import com.dancing_koala.covid_19data.data.StateTimeSeries
import com.dancing_koala.covid_19data.network.BaseService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

class JHURemoteDataRepository {
    private val service = JHUGithubService()
    private val csvDataParser = JHUDataParser()

    suspend fun getConfirmedTimeSeries(): List<StateTimeSeries> = withContext(Dispatchers.IO) {
        val result = service.fetchConfirmedTimeSeriesData()

        if (result is BaseService.Result.Success) {
            return@withContext csvDataParser.parseTimeSeriesCsv(result.data)
        }

        return@withContext listOf<StateTimeSeries>()
    }

    suspend fun getDeathsTimeSeries(): List<StateTimeSeries> = withContext(Dispatchers.IO) {
        val result = service.fetchDeathsTimeSeriesData()

        if (result is BaseService.Result.Success) {
            return@withContext csvDataParser.parseTimeSeriesCsv(result.data)
        }

        return@withContext listOf<StateTimeSeries>()
    }

    suspend fun getRecoveredTimeSeries(): List<StateTimeSeries> = withContext(Dispatchers.IO) {
        val result = service.fetchRecoveredTimeSeriesData()

        if (result is BaseService.Result.Success) {
            return@withContext csvDataParser.parseTimeSeriesCsv(result.data)
        }

        return@withContext listOf<StateTimeSeries>()
    }

    suspend fun getLastDailyReports(): List<DailyReport> = withContext(Dispatchers.IO) {
        val cal = Calendar.getInstance()
        var result: BaseService.Result? = null

        while (result == null || result.responseCode == 404) {
            cal.add(Calendar.DATE, -1)

            result = service.fetchDailyReportData(
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DATE)
            )
        }

        if (result is BaseService.Result.Success) {
            return@withContext csvDataParser.parseDailyReportCsv(result.data)
        }

        return@withContext listOf<DailyReport>()
    }
}