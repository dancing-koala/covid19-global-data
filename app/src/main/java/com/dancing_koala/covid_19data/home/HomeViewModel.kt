package com.dancing_koala.covid_19data.home

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dancing_koala.covid_19data.android.BaseViewModel
import com.dancing_koala.covid_19data.data.ReportDataSet
import com.dancing_koala.covid_19data.network.lmaoninja.LmaoNinjaApiDataRepository
import com.dancing_koala.covid_19data.network.lmaoninja.LmaoNinjaApiDataRepository.ApiResult
import kotlinx.coroutines.launch
import org.kodein.di.generic.instance

class HomeViewModel(application: Application) : BaseViewModel(application) {

    val reportsLiveData: LiveData<List<ReportDataSet>>
        get() = internalReportsLiveData

    val viewStateLiveData: LiveData<ViewState>
        get() = _viewStateLiveData

    private val internalReportsLiveData = MutableLiveData<List<ReportDataSet>>()
    private val _viewStateLiveData = MutableLiveData<ViewState>()
    private val dataRepository: LmaoNinjaApiDataRepository by kodein.instance()

    private lateinit var worldReport: ReportDataSet

    fun start() {
        viewModelScope.launch {
            _viewStateLiveData.value = ViewState.ShowLoading

            val reportDataSetsResult = dataRepository.getReportDataSets()

            if (reportDataSetsResult is ApiResult.Success) {
                val reportDataSets = reportDataSetsResult.value as List<ReportDataSet>

                internalReportsLiveData.value = reportDataSets

                if (reportDataSets.isNotEmpty()) {
                    worldReport = reportDataSets.first { it.id == 0 }
                    _viewStateLiveData.value = ViewState.UpdateMainReportValues(worldReport)
                }
            } else {
                when (reportDataSetsResult) {
                    ApiResult.Failure.NetworkError    -> _viewStateLiveData.value = ViewState.ShowNetworkError
                    is ApiResult.Failure.UnknownError -> {
                        _viewStateLiveData.value = ViewState.ShowUnknownError
                    }
                }
            }

            _viewStateLiveData.value = ViewState.HideLoading
        }
    }

    fun onDatavizButtonClick() {
        _viewStateLiveData.value = ViewState.GoToDataviz
    }

    fun onWorldReportButtonClick() {
        _viewStateLiveData.value = ViewState.UpdateMainReportValues(worldReport)
    }

    fun onMapItemClick(itemId: Int) {
        internalReportsLiveData.value?.firstOrNull { it.id == itemId }?.let {
            _viewStateLiveData.value = ViewState.UpdateMainReportValues(it)
        }
    }

    sealed class ViewState {
        class UpdateMainReportValues(val report: ReportDataSet) : ViewState()
        object GoToDataviz : ViewState()
        object ShowLoading : ViewState()
        object HideLoading : ViewState()
        object ShowNetworkError : ViewState()
        object ShowUnknownError : ViewState()
    }
}
