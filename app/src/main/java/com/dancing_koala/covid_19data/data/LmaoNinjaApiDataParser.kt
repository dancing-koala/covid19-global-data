package com.dancing_koala.covid_19data.data

import org.json.JSONArray
import org.json.JSONObject

class LmaoNinjaApiDataParser {

    fun parseCountriesData(jsonData: String): List<ReportDataSet> {
        val result = mutableListOf<ReportDataSet>()
        val rootJsonArray = JSONArray(jsonData)

        val reportDataHolder = MutableReportDataHolder()

        for (i in 0 until rootJsonArray.length()) {
            val reportDataSet = rootJsonArray.getJSONObject(i).toReportDataSet(i + 1)

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