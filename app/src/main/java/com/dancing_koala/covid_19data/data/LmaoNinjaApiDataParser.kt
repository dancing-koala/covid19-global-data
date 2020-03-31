package com.dancing_koala.covid_19data.data

import org.json.JSONArray
import org.json.JSONObject

class LmaoNinjaApiDataParser {

    private val dateFormattedCache = HashMap<String, String>()

    fun parseCountriesData(jsonData: String): List<ReportDataSet> {
        val result = mutableListOf<ReportDataSet>()
        val rootJsonArray = JSONArray(jsonData)

        val reportDataHolder = MutableReportDataHolder()

        rootJsonArray.foreachJsonObject { index, item ->
            val reportDataSet = item.toReportDataSet(index + 1)

            reportDataHolder.cases += reportDataSet.cases
            reportDataHolder.casesToday += reportDataSet.casesToday
            reportDataHolder.deaths += reportDataSet.deaths
            reportDataHolder.deathsToday += reportDataSet.deathsToday
            reportDataHolder.recovered += reportDataSet.recovered
            reportDataHolder.recoveredToday += reportDataSet.recoveredToday
            reportDataHolder.active += reportDataSet.active
            reportDataHolder.critical += reportDataSet.critical
            reportDataHolder.casesPerOneMillion += reportDataSet.casesPerOneMillion
            reportDataHolder.deathsPerOneMillion += reportDataSet.deathsPerOneMillion

            result.add(reportDataSet)
        }

        val worldWide = ReportDataSet(
            id = 0,
            country = Country(
                id = 0,
                name = "Worldwide",
                iso2 = "",
                latitude = Double.NaN,
                longitude = Double.NaN
            ),
            cases = reportDataHolder.cases,
            casesToday = reportDataHolder.casesToday,
            deaths = reportDataHolder.deaths,
            deathsToday = reportDataHolder.deathsToday,
            recovered = reportDataHolder.recovered,
            recoveredToday = reportDataHolder.recoveredToday,
            active = reportDataHolder.active,
            critical = reportDataHolder.critical,
            casesPerOneMillion = reportDataHolder.casesPerOneMillion,
            deathsPerOneMillion = reportDataHolder.deathsPerOneMillion
        )

        result.add(0, worldWide)

        return result
    }

    fun parseTimeLineDataSets(jsonData: String): List<TimeLineDataSet> {
        val result = mutableListOf<TimeLineDataSet>()
        val rootJsonArray = JSONArray(jsonData)

        rootJsonArray.foreachJsonObject { index, item ->
            val timeLineDataSet = item.toTimeLineDataSet(index)
            result.add(timeLineDataSet)
        }

        return result
    }

    private fun JSONArray.foreachJsonObject(itemBlock: (index: Int, item: JSONObject) -> Unit) {
        val length = length()

        for (i in 0 until length) {
            itemBlock.invoke(i, getJSONObject(i))
        }
    }

    private fun JSONObject.toReportDataSet(id: Int): ReportDataSet {
        val countryName = getString("country")
        val country = getJSONObject("countryInfo").toCountry(countryName)

        return ReportDataSet(
            id = id,
            country = country,
            cases = getInt("cases"),
            casesToday = optInt("todayCases"),
            deaths = getInt("deaths"),
            deathsToday = optInt("todayDeaths"),
            recovered = getInt("recovered"),
            recoveredToday = optInt("todayRecovered"),
            active = optInt("active"),
            critical = optInt("critical"),
            casesPerOneMillion = optInt("casesPerOneMillion"),
            deathsPerOneMillion = optInt("deathsPerOneMillion")
        )
    }

    private fun JSONObject.toCountry(name: String): Country {
        return Country(
            id = optInt("_id", name.hashCode()),
            name = name,
            iso2 = getString("iso2"),
            latitude = getDouble("lat").takeIf { it != 0.0 } ?: Double.NaN,
            longitude = getDouble("long").takeIf { it != 0.0 } ?: Double.NaN
        )
    }

    private fun JSONObject.toTimeLineDataSet(id: Int): TimeLineDataSet {
        val timeLineObject = getJSONObject("timeline")

        return TimeLineDataSet(
            id = id,
            provinceName = optString("province").takeUnless { it == "null" } ?: "",
            countryName = getString("country"),
            casesTimeLine = timeLineObject.getJSONObject("cases").toTimeLine(),
            deathsTimeLine = timeLineObject.getJSONObject("deaths").toTimeLine()
        )
    }

    private fun JSONObject.toTimeLine(): TimeLine {
        val result = TimeLine()

        keys().forEach { key ->
            val date = dateFormattedCache.getOrPut(key) { formatDate(key) }
            result[date] = optInt(key)
        }

        return result
    }

    private fun formatDate(date: String): String {
        val parts = date.split("/").map { if (it.length < 2) "0$it" else it }
        return "20${parts[2]}-${parts[0]}-${parts[1]}"
    }

    private class MutableReportDataHolder(
        var cases: Int = 0,
        var casesToday: Int = 0,
        var deaths: Int = 0,
        var deathsToday: Int = 0,
        var recovered: Int = 0,
        var recoveredToday: Int = 0,
        var active: Int = 0,
        var critical: Int = 0,
        var casesPerOneMillion: Int = 0,
        var deathsPerOneMillion: Int = 0
    )
}