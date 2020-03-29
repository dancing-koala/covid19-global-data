package com.dancing_koala.covid_19data.network.lmaoninja

import com.dancing_koala.covid_19data.data.LmaoNinjaApiDataParser
import com.dancing_koala.covid_19data.data.ReportDataSet
import com.dancing_koala.covid_19data.data.TimeLineDataSet
import com.dancing_koala.covid_19data.network.BaseService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LmaoNinjaApiRemoteDataRepository {
    private val service = LmaoNinjaApiService()
    private val parser = LmaoNinjaApiDataParser()

    suspend fun getCountriesReports(): List<ReportDataSet> = withContext(Dispatchers.IO) {
        val result = service.fetchCountriesData()

        if (result is BaseService.Result.Success) {
            parser.parseCountriesData(result.data)
        } else {
            listOf()
        }
    }

    suspend fun getCountriesTimeLines(): List<TimeLineDataSet> = withContext(Dispatchers.IO) {
        val result = service.fetchHistoricalV2()

        if (result is BaseService.Result.Success) {
            parser.parseTimeLineDataSets(result.data)
        } else {
            listOf()
        }
    }
}