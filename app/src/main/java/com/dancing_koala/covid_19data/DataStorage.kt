package com.dancing_koala.covid_19data

import com.dancing_koala.covid_19data.data.AreaData
import com.dancing_koala.covid_19data.data.ReportDataSet

class DataStorage {
    companion object {
        val instance = DataStorage()
    }

    val countriesData: List<ReportDataSet>
        get() = internalCountriesData

    val data: List<AreaData>
        get() = internalData

    private val internalCountriesData = mutableListOf<ReportDataSet>()
    private val internalData = mutableListOf<AreaData>()

    fun updateData(data: List<AreaData>) {
        internalData.clear()
        internalData.addAll(data)
    }

    fun updateCountriesData(countriesData: List<ReportDataSet>) {
        internalCountriesData.clear()
        internalCountriesData.addAll(countriesData)
    }
}