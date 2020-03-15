package com.dancing_koala.covid_19data

import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.io.FileNotFoundException
import java.io.FileReader
import java.text.SimpleDateFormat
import java.util.*

class CsvDataParserTest {

    val timeSeriesFileName = "time_series_shortened.csv"
    val dailyReportFileName = "daily_report_shortened.csv"

    @Before
    fun setUp() {
    }

    @After
    fun tearDown() {
    }

    @Test
    fun parseTimeSeriesCsv() {
        val expected = listOf(
            StateTimeSeries(
                "StateA", "CountryA", 15.0, 101.0,
                hashMapOf(
                    "1/22/20" to 2,
                    "1/23/20" to 3,
                    "1/24/20" to 5,
                    "1/25/20" to 7,
                    "1/26/20" to 8,
                    "1/27/20" to 8,
                    "1/28/20" to 14,
                    "1/29/20" to 14,
                    "1/30/20" to 14
                )
            ),
            StateTimeSeries(
                "StateB", "CountryB", 36.0, 138.0,
                hashMapOf(
                    "1/22/20" to 2,
                    "1/23/20" to 1,
                    "1/24/20" to 2,
                    "1/25/20" to 2,
                    "1/26/20" to 4,
                    "1/27/20" to 4,
                    "1/28/20" to 7,
                    "1/29/20" to 7,
                    "1/30/20" to 11
                )
            ),
            StateTimeSeries(
                "StateC", "CountryC", 1.2833, 103.8333,
                hashMapOf(
                    "1/22/20" to 0,
                    "1/23/20" to 1,
                    "1/24/20" to 3,
                    "1/25/20" to 3,
                    "1/26/20" to 4,
                    "1/27/20" to 5,
                    "1/28/20" to 7,
                    "1/29/20" to 7,
                    "1/30/20" to 10
                )
            ),
            StateTimeSeries(
                "StateD", "CountryD", 28.1667, 84.25,
                hashMapOf(
                    "1/22/20" to 0,
                    "1/23/20" to 0,
                    "1/24/20" to 0,
                    "1/25/20" to 1,
                    "1/26/20" to 1,
                    "1/27/20" to 1,
                    "1/28/20" to 1,
                    "1/29/20" to 1,
                    "1/30/20" to 1
                )
            ),
            StateTimeSeries(
                "StateE", "CountryE", 2.5, 112.5,
                hashMapOf(
                    "1/22/20" to 0,
                    "1/23/20" to 0,
                    "1/24/20" to 0,
                    "1/25/20" to 3,
                    "1/26/20" to 4,
                    "1/27/20" to 4,
                    "1/28/20" to 4,
                    "1/29/20" to 7,
                    "1/30/20" to 8
                )
            )
        )


        val fileUrl = javaClass.getResource("/$timeSeriesFileName") ?: throw FileNotFoundException(timeSeriesFileName)
        val fileReader = FileReader(fileUrl.file)
        val parser = CsvDataParser()
        val actual = parser.parseTimeSeriesCsv(fileReader)

        Assert.assertEquals(expected.size, actual.size)

        for (i in actual.indices) {
            Assert.assertEquals(expected[i], actual[i])
        }
    }

    @Test
    fun parseDailyReportCsv() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);

        val expected = listOf(
            DailyReport("Hubei", "China", dateFormat.parse("2020-03-13T11:09:03")!!, 67786, 3062, 51553, 30.9756, 112.2707),
            DailyReport("", "Afghanistan", dateFormat.parse("2020-03-11T20:00:00")!!, 7, 0, 0, 33.0, 65.0),
            DailyReport("", "Monaco", dateFormat.parse("2020-03-11T20:00:00")!!, 2, 0, 0, 43.7333, 7.4167),
            DailyReport("", "Liechtenstein", dateFormat.parse("2020-03-11T20:00:00")!!, 1, 0, 0, 47.14, 9.55),
            DailyReport("", "Guyana", dateFormat.parse(" 2020-03-11T20:00:00")!!, 1, 1, 0, 5.0, -58.75),
            DailyReport("", "Taiwan*", dateFormat.parse("2020-03-11T20:00:00")!!, 50, 1, 20, 23.7, 121.0)
        )

        val fileUrl = javaClass.getResource("/$dailyReportFileName") ?: throw FileNotFoundException(timeSeriesFileName)
        val fileReader = FileReader(fileUrl.file)
        val parser = CsvDataParser()
        val actual = parser.parseDailyReportCsv(fileReader)

        Assert.assertEquals(expected.size, actual.size)

        for (i in actual.indices) {
            Assert.assertEquals(expected[i], actual[i])
        }
    }
}