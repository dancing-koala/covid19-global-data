package com.dancing_koala.covid_19data.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.CacheControl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.TimeUnit

abstract class BaseService {

    protected abstract val okHttpClient: OkHttpClient

    protected suspend fun fetchContent(url: String): NetworkResult =
        withContext(Dispatchers.IO) {
            val request = Request.Builder()
                .url(url)
                .cacheControl(CacheControl.Builder().maxAge(1, TimeUnit.HOURS).build())
                .build()

            try {
                val response = okHttpClient.newCall(request).execute()
                val data = response.body?.string() ?: ""
                response.close()

                if (response.code >= 400) {
                    if (response.code >= 500) {
                        NetworkResult.Error.HttpServerError(responseCode = response.code, url = url)
                    } else {
                        NetworkResult.Error.HttpClientError(responseCode = response.code, url = url)
                    }
                }

                NetworkResult.Success(responseCode = response.code, data = data)
            } catch (e: IOException) {
                NetworkResult.Error.MaybeNoInternetError(url = url, throwable = e)
            } catch (e: Exception) {
                NetworkResult.Error.UnknownError(url = url, throwable = e)
            }
        }
}