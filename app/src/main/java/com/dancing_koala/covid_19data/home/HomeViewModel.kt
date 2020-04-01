package com.dancing_koala.covid_19data.home

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dancing_koala.covid_19data.android.BaseViewModel
import com.dancing_koala.covid_19data.data.ReportDataSet
import com.dancing_koala.covid_19data.network.lmaoninja.LmaoNinjaApiRemoteDataRepository
import kotlinx.coroutines.launch
import org.kodein.di.generic.instance

class HomeViewModel(application: Application) : BaseViewModel(application) {

    val reportsLiveData: LiveData<List<ReportDataSet>>
        get() = internalReportsLiveData

    val viewStateLiveData: LiveData<ViewState>
        get() = internalViewStateLiveData

    private val internalReportsLiveData = MutableLiveData<List<ReportDataSet>>()
    private val internalViewStateLiveData = MutableLiveData<ViewState>()
    private val remoteDataRepository: LmaoNinjaApiRemoteDataRepository by kodein.instance()

    private lateinit var worldReport: ReportDataSet

    fun start() {
        viewModelScope.launch {
            internalViewStateLiveData.value = ViewState.ShowLoading

            val reportDataSets= remoteDataRepository.getReportDataSets()
            internalReportsLiveData.value = reportDataSets

            if (reportDataSets.isNotEmpty()) {
                worldReport = reportDataSets.first { it.id == 0 }
                internalViewStateLiveData.value = ViewState.UpdateMainReportValues(worldReport)
            }

            internalViewStateLiveData.value = ViewState.HideLoading
        }
    }

    fun onDatavizButtonClick() {
        internalViewStateLiveData.value = ViewState.GoToDataviz
    }

    fun onWorldReportButtonClick() {
        internalViewStateLiveData.value = ViewState.UpdateMainReportValues(worldReport)
    }

    fun onMapItemClick(itemId: Int) {
        internalReportsLiveData.value?.firstOrNull { it.id == itemId }?.let {
            internalViewStateLiveData.value = ViewState.UpdateMainReportValues(it)
        }
    }

    sealed class ViewState {
        class UpdateMainReportValues(val report: ReportDataSet) : ViewState()
        object GoToDataviz : ViewState()
        object ShowLoading : ViewState()
        object HideLoading : ViewState()
    }
}
