package com.dancing_koala.covid_19data.network.lmaoninja

import com.dancing_koala.covid_19data.data.LmaoNinjaApiDataParser
import com.dancing_koala.covid_19data.data.ReportDataSet
import com.dancing_koala.covid_19data.data.TimeLineDataSet
import com.dancing_koala.covid_19data.network.BaseService
import com.dancing_koala.covid_19data.persistence.Cache
import com.dancing_koala.covid_19data.persistence.CacheDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
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

    suspend fun getReportDataSets(): List<ReportDataSet> = withContext(Dispatchers.IO) {
        val data = fromCacheOrFetch(reportsKey, "[]") { service.fetchCountriesData() }
        parser.parseReportDataSets(data)
    }

    suspend fun getTimeLineDataSets(): List<TimeLineDataSet> = withContext(Dispatchers.IO) {
        val data = fromCacheOrFetch(timeLinesKey, "[]") { service.fetchHistoricalV2() }
        parser.parseTimeLineDataSets(data)
    }

    private suspend fun fromCacheOrFetch(cacheKey: String, defaultValue: String, fetchBlock: suspend () -> BaseService.Result): String = coroutineScope {
        val cachedResponse = cacheDao.getByKey(cacheKey)

        if (cachedResponse != null) {
            cachedResponse.response
        } else {
            val result = fetchBlock.invoke()
            if (result is BaseService.Result.Success) {
                launch { cacheDao.insertEntry(Cache(cacheKey, result.data)) }
                result.data
            } else {
                defaultValue
            }
        }
    }
}