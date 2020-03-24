package com.dancing_koala.covid_19data.network.lmaoninja

import com.dancing_koala.covid_19data.network.BaseService
import okhttp3.OkHttpClient

class LmaoNinjaApiService : BaseService() {
    override val okHttpClient: OkHttpClient = OkHttpClient.Builder().build()

    private val baseUrl = "https://corona.lmao.ninja"
    private val countriesUrl = "$baseUrl/countries"
    private val historicalUrl = "$baseUrl/historical"

    suspend fun fetchCountriesData() = fetchContent(countriesUrl)
}