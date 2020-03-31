package com.dancing_koala.covid_19data.network.lmaoninja

import com.dancing_koala.covid_19data.data.LmaoNinjaApiDataParser
import com.dancing_koala.covid_19data.data.ReportDataSet
import com.dancing_koala.covid_19data.data.TimeLineDataSet
import com.dancing_koala.covid_19data.network.BaseService
import com.dancing_koala.covid_19data.persistence.Cache
import com.dancing_koala.covid_19data.persistence.CacheDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.kodein.di.Kodein
import org.kodein.di.generic.instance
import java.util.*

class LmaoNinjaApiRemoteDataRepository(kodein: Kodein) {

    private val service: LmaoNinjaApiService by kodein.instance()
    private val cacheDao: CacheDao by kodein.instance()
    private val parser = LmaoNinjaApiDataParser()
    private val keyPrefix: String by lazy {
        val cal = Calendar.getInstance()
        String.format(
            Locale.ROOT,
            "%04d%02d%02d",
            cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DATE)
        )
    }

    private val reportsKey = "$keyPrefix-reports"
    private val timeLinesKey = "$keyPrefix-timeLines"

    suspend fun getCountriesReports(): List<ReportDataSet> = withContext(Dispatchers.IO) {
        val result = service.fetchCountriesData()

        if (result is BaseService.Result.Success) {
            parser.parseCountriesData(result.data)
        } else {
            listOf()
        }
    }

    suspend fun getCountriesTimeLines(): List<TimeLineDataSet> = withContext(Dispatchers.IO) {
        val cachedResponse = cacheDao.getByKey(timeLinesKey)

        val data = if (cachedResponse == null) {
            val result = service.fetchHistoricalV2()
            if (result is BaseService.Result.Success) {
                launch { cacheDao.insertEntry(Cache(timeLinesKey, result.data)) }
                result.data
            } else "[]"
        } else {
            cachedResponse.response
        }

        parser.parseTimeLineDataSets(data)
    }
}