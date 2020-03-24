package com.dancing_koala.covid_19data.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.CacheControl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

abstract class BaseService {

    protected abstract val okHttpClient: OkHttpClient

    protected suspend fun fetchContent(url: String): Result =
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

    sealed class Result(val responseCode: Int) {
        class Success(responseCode: Int, val data: String) : Result(responseCode)
        sealed class Error(responseCode: Int, val url: String) : Result(responseCode) {
            class HttpClientError(responseCode: Int, url: String) : Error(responseCode, url)
            class HttpServerError(responseCode: Int, url: String) : Error(responseCode, url)
            class UnknownError(url: String, val throwable: Throwable) : Error(-1, url)
        }
    }
}