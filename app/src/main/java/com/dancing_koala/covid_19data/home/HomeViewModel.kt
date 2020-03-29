package com.dancing_koala.covid_19data.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dancing_koala.covid_19data.data.ReportDataSet
import com.dancing_koala.covid_19data.network.lmaoninja.LmaoNinjaApiRemoteDataRepository
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val internalReportsLiveData = MutableLiveData<List<ReportDataSet>>()

    val reportsLiveData: LiveData<List<ReportDataSet>>
        get() = internalReportsLiveData

    private val internalViewStateLiveData = MutableLiveData<ViewState>()

    val viewStateLiveData: LiveData<ViewState>
        get() = internalViewStateLiveData

    private val remoteDataRepository = LmaoNinjaApiRemoteDataRepository()

    fun start() {
        viewModelScope.launch {
            internalViewStateLiveData.value = ViewState.ShowLoading

            val tempWorldData = remoteDataRepository.getCountriesReports()

            internalReportsLiveData.value = tempWorldData

            if (tempWorldData.isNotEmpty()) {
                val worldReport = tempWorldData.first { it.id == 0 }
                internalViewStateLiveData.value = ViewState.UpdateMainReportValues(worldReport)
            }

            internalViewStateLiveData.value = ViewState.HideLoading
        }
    }

    fun onDatavizButtonClick() {
        internalViewStateLiveData.value = ViewState.GoToDataviz
    }

    sealed class ViewState {
        class UpdateMainReportValues(val report: ReportDataSet) : ViewState()
        object GoToDataviz : ViewState()
        object ShowLoading : ViewState()
        object HideLoading : ViewState()
    }

}
