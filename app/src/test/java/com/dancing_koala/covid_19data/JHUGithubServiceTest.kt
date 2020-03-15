package com.dancing_koala.covid_19data

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class JHUGithubServiceTest {

    private val service = JHUGithubService()

    @Test
    fun fetchConfirmedTimeSeriesData() = runBlocking {
        val result = service.fetchConfirmedTimeSeriesData()
        assertTrue(result is JHUGithubService.Result.Success)
        assertEquals(200, result.responseCode)
    }

    @Test
    fun fetchDeathsTimeSeriesData() = runBlocking {
        val result = service.fetchDeathsTimeSeriesData()
        assertTrue(result is JHUGithubService.Result.Success)
        assertEquals(200, result.responseCode)
    }

    @Test
    fun fetchRecoveredTimeSeriesData() = runBlocking {
        val result = service.fetchRecoveredTimeSeriesData()
        assertTrue(result is JHUGithubService.Result.Success)
        assertEquals(200, result.responseCode)
    }

    @Test
    fun fetchDailyReportData_Success() = runBlocking {
        val result = service.fetchDailyReportData(2020, 1, 24)
        assertTrue(result is JHUGithubService.Result.Success)
        assertEquals(200, result.responseCode)
    }

    @Test
    fun fetchDailyReportData_Failure() = runBlocking {
        val result = service.fetchDailyReportData(1991, 1, 24)
        assertTrue(result is JHUGithubService.Result.Error.HttpClientError)
        assertEquals(404, result.responseCode)
    }
}