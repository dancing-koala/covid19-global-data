package com.dancing_koala.covid_19data.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.dancing_koala.covid_19data.DataStorage
import com.dancing_koala.covid_19data.data.ReportDataSet

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val worldData = DataStorage.instance.countriesData

    val viewStateLiveData: LiveData<ViewState>
        get() = internalViewStateLiveData

    private val internalViewStateLiveData = MutableLiveData<ViewState>()

    fun start() {
        val worldReport = worldData.first { it.id == 0 }
        internalViewStateLiveData.value = ViewState.UpdateMainReportValues(worldReport)
    }

    fun onMapReady() {
        internalViewStateLiveData.value = ViewState.DisplayItemsOnMap(worldData)
    }

    fun onDatavizButtonClick() {
        internalViewStateLiveData.value = ViewState.GoToDataviz
    }

    sealed class ViewState {
        class UpdateMainReportValues(val report: ReportDataSet) : ViewState()
        class DisplayItemsOnMap(val items: List<ReportDataSet>) : ViewState()
        object GoToDataviz : ViewState()
    }

}
