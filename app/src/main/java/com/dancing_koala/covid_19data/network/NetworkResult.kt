package com.dancing_koala.covid_19data.network

import java.io.IOException

sealed class NetworkResult(val responseCode: Int) {
    class Success(responseCode: Int, val data: String) : NetworkResult(responseCode)
    sealed class Error(responseCode: Int, val url: String) : NetworkResult(responseCode) {
        class HttpClientError(responseCode: Int, url: String) : Error(responseCode, url)
        class HttpServerError(responseCode: Int, url: String) : Error(responseCode, url)
        class UnknownError(url: String, val throwable: Throwable) : Error(-1, url)
        class MaybeNoInternetError(url: String, val throwable: IOException) : Error(-1, url)
    }
}