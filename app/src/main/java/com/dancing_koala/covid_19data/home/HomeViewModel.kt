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
        get() = _reportsLiveData

    val viewStateLiveData: LiveData<ViewState>
        get() = _viewStateLiveData

    private val _reportsLiveData = MutableLiveData<List<ReportDataSet>>(listOf())
    private val _viewStateLiveData = MutableLiveData<ViewState>()
    private val dataRepository: LmaoNinjaApiDataRepository by kodein.instance()

    private lateinit var worldReport: ReportDataSet

    fun start() {
        viewModelScope.launch {
            _errorLiveData.value = Error.None
            _viewStateLiveData.value = ViewState.ShowLoading

            val reportDataSetsResult = dataRepository.getReportDataSets()

            if (reportDataSetsResult is ApiResult.Success) {
                val reportDataSets = reportDataSetsResult.value as List<ReportDataSet>

                _reportsLiveData.value = reportDataSets

                if (reportDataSets.isNotEmpty()) {
                    worldReport = reportDataSets.first { it.id == 0 }
                    _viewStateLiveData.value = ViewState.UpdateMainReportValues(worldReport)
                }
            } else {
                when (reportDataSetsResult) {
                    ApiResult.Failure.NetworkError    -> _errorLiveData.value = Error.MaybeNoInternet
                    is ApiResult.Failure.UnknownError -> _errorLiveData.value = Error.Unknown
                }
            }

            _viewStateLiveData.value = ViewState.HideLoading
        }
    }

    fun onDatavizButtonClick() {
        if (_reportsLiveData.value?.isEmpty() != false) {
            return
        }
        _viewStateLiveData.value = ViewState.GoToDataviz
    }

    fun onGlobalReportButtonClick() {
        _viewStateLiveData.value = ViewState.UpdateMainReportValues(worldReport)
    }

    fun onMapItemClick(itemId: Int) {
        _reportsLiveData.value?.firstOrNull { it.id == itemId }?.let {
            _viewStateLiveData.value = ViewState.UpdateMainReportValues(it)
        }
    }

    fun onErrorRetryButtonClick() = start()

    fun onMenuItemsCreated() {
        _errorLiveData.value = errorLiveData.value
    }

    sealed class ViewState {
        class UpdateMainReportValues(val report: ReportDataSet) : ViewState()
        object GoToDataviz : ViewState()
        object ShowLoading : ViewState()
        object HideLoading : ViewState()
    }
}
