package com.dancing_koala.covid_19data.data

import com.dancing_koala.covid_19data.fastcsv.reader.CsvReader
import java.io.Reader
import java.io.StringReader
import java.text.SimpleDateFormat
import java.util.*

class JHUDataParser {

    fun parseTimeSeriesCsv(csvData: String): List<StateTimeSeries> =
        parseTimeSeriesCsv(StringReader(csvData))

    fun parseTimeSeriesCsv(reader: Reader): List<StateTimeSeries> {
        val csvReader = CsvReader()
        val csvContainer = csvReader.read(reader)
        val csvRows = csvContainer.rows

        val headersRow = csvRows.first().fields

        val result = mutableListOf<StateTimeSeries>()

        for (rowIndex in 1 until csvRows.size) {
            val row = csvRows[rowIndex]
            val perDayData = hashMapOf<String, Int>()

            for (i in 4 until row.fieldCount) {
                val date = headersRow[i]
                perDayData[date] = row.getField(i).toIntOrNull() ?: -1
            }

            val timeSeries = StateTimeSeries(
                state = row.getField(0),
                country = row.getField(1),
                latitude = row.getField(2).toDoubleOrNull() ?: 0.0,
                longitude = row.getField(3).toDoubleOrNull() ?: 0.0,
                perDayData = perDayData
            )

            result.add(timeSeries)
        }

        return result
    }

    fun parseDailyReportCsv(csvData: String): List<DailyReport> =
        parseDailyReportCsv(StringReader(csvData))

    fun parseDailyReportCsv(reader: Reader): List<DailyReport> {
        val csvReader = CsvReader()
        val csvContainer = csvReader.read(reader)
        val csvRows = csvContainer.rows

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

        val result = mutableListOf<DailyReport>()

        val colStateIndex = 2
        val colCountryIndex = 3
        val colLastUpdate = 4
        val colLatitude = 5
        val colLongitude = 6
        val colConfirmed = 7
        val colDeaths = 8
        val colRecovered = 9
        val colActive = 10

        for (rowIndex in 1 until csvRows.size) {
            val row = csvRows[rowIndex]
            val dailyReport = DailyReport(
                state = row.getField(colStateIndex),
                country = row.getField(colCountryIndex),
                lastUpdate = dateFormat.parse(row.getField(colLastUpdate)) ?: Date(0L),
                confirmed = row.getField(colConfirmed).toInt(),
                deaths = row.getField(colDeaths).toInt(),
                recovered = row.getField(colRecovered).toInt(),
                active = row.getField(colActive).toInt(),
                latitude = row.getField(colLatitude).toDouble(),
                longitude = row.getField(colLongitude).toDouble()
            )

            result.add(dailyReport)
        }

        return result
    }
}