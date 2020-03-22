package com.dancing_koala.covid_19data

import com.dancing_koala.covid_19data.data.StateData

class DataStorage {
    companion object {
        val instance = DataStorage()
    }

    val data: List<StateData>
        get() = internalData

    private val internalData: MutableList<StateData> = mutableListOf()

    fun updateData(data: List<StateData>) {
        internalData.clear()
        internalData.addAll(data)
    }
}