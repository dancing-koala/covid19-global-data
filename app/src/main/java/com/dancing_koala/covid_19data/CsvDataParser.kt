package com.dancing_koala.covid_19data

import com.dancing_koala.covid_19data.fastcsv.reader.CsvReader
import java.io.Reader
import java.io.StringReader
import java.text.SimpleDateFormat
import java.util.*

class CsvDataParser {

    fun parseTimeSeriesCsv(csvData: String): List<StateTimeSeries> =
        parseTimeSeriesCsv(StringReader(csvData))

    fun parseTimeSeriesCsv(reader: Reader): List<StateTimeSeries> {
        val csvReader = CsvReader()
        val csvContainer = csvReader.read(reader)
        val csvRows = csvContainer.rows

        val headersRow = csvRows.first()

        val result = mutableListOf<StateTimeSeries>()

        for (rowIndex in 1 until csvRows.size) {
            val row = csvRows[rowIndex]
            val perDayData = hashMapOf<String, Int>()

            for (i in 4 until row.fieldCount) {
                val date = headersRow.getField(i)
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

        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);

        val result = mutableListOf<DailyReport>()

        for (rowIndex in 1 until csvRows.size) {
            val row = csvRows[rowIndex]
            println(row)
            val dailyReport = DailyReport(
                state = row.getField(0),
                country = row.getField(1),
                lastUpdate = dateFormat.parse(row.getField(2)) ?: Date(0L),
                confirmed = row.getField(3).toInt(),
                deaths = row.getField(4).toInt(),
                recovered = row.getField(5).toInt(),
                latitude = row.getField(6).toDouble(),
                longitude = row.getField(7).toDouble()
            )

            result.add(dailyReport)
        }

        return result
    }
}