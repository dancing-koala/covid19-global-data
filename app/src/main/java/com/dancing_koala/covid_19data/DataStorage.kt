package com.dancing_koala.covid_19data

import com.dancing_koala.covid_19data.data.AreaData

class DataStorage {
    companion object {
        val instance = DataStorage()
    }

    val data: List<AreaData>
        get() = internalData

    private val internalData: MutableList<AreaData> = mutableListOf()

    fun updateData(data: List<AreaData>) {
        internalData.clear()
        internalData.addAll(data)
    }
}