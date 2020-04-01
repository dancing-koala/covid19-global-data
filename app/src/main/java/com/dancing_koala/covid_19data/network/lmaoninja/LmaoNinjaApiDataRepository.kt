package com.dancing_koala.covid_19data.network.lmaoninja

import com.dancing_koala.covid_19data.data.LmaoNinjaApiDataParser
import com.dancing_koala.covid_19data.network.NetworkResult
import com.dancing_koala.covid_19data.persistence.Cache
import com.dancing_koala.covid_19data.persistence.CacheDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.kodein.di.Kodein
import org.kodein.di.generic.instance
import java.util.*

class LmaoNinjaApiDataRepository(kodein: Kodein) {

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

    suspend fun getReportDataSets(): ApiResult = withContext(Dispatchers.IO) {
        fromCacheOrFetch(
            reportsKey,
            { service.fetchCountriesData() },
            { data -> parser.parseReportDataSets(data) }
        )
    }

    suspend fun getTimeLineDataSets(): ApiResult = withContext(Dispatchers.IO) {
        fromCacheOrFetch(
            timeLinesKey,
            { service.fetchHistoricalV2() },
            { data -> parser.parseTimeLineDataSets(data) }
        )
    }

    private suspend fun fromCacheOrFetch(
        cacheKey: String,
        fetchBlock: suspend () -> NetworkResult,
        parseBlock: suspend (data: String) -> Any
    ) = coroutineScope {
        val cachedResponse = cacheDao.getByKey(cacheKey)

        if (cachedResponse != null) {
            ApiResult.Success(parseBlock.invoke(cachedResponse.response))
        } else {
            val result = fetchBlock.invoke()
            if (result is NetworkResult.Success) {
                launch { cacheDao.insertEntry(Cache(cacheKey, result.data)) }
                ApiResult.Success(parseBlock.invoke(result.data))
            } else {
                when (result) {
                    is NetworkResult.Error.MaybeNoInternetError -> ApiResult.Failure.NetworkError
                    is NetworkResult.Error.HttpClientError      -> ApiResult.Failure.NetworkError
                    is NetworkResult.Error.HttpServerError      -> ApiResult.Failure.NetworkError
                    else                                        -> ApiResult.Failure.UnknownError
                }
            }
        }
    }

    sealed class ApiResult {
        class Success(val value: Any) : ApiResult()
        sealed class Failure : ApiResult() {
            object UnknownError : Failure()
            object NetworkError : Failure()
        }
    }
}