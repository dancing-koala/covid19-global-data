package com.dancing_koala.covid_19data.dataviz

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dancing_koala.covid_19data.android.BaseViewModel
import com.dancing_koala.covid_19data.data.TimeLineDataSet
import com.dancing_koala.covid_19data.network.lmaoninja.LmaoNinjaApiRemoteDataRepository
import kotlinx.coroutines.launch
import org.kodein.di.generic.instance

class DatavizViewModel(application: Application) : BaseViewModel(application) {

    private val remoteDataRepository: LmaoNinjaApiRemoteDataRepository by kodein.instance()

    private val internalTimeLineDataSetsLiveData = MutableLiveData<List<TimeLineDataSet>>()

    private val internalViewStateLiveData = MutableLiveData<ViewState>()

    val viewStateLiveData: LiveData<ViewState>
        get() = internalViewStateLiveData

    fun start() {
        viewModelScope.launch {
            internalViewStateLiveData.value = ViewState.ShowLoading

            val timeLineDataSets = remoteDataRepository.getCountriesTimeLines()
            internalTimeLineDataSetsLiveData.value = timeLineDataSets

            internalViewStateLiveData.value = ViewState.HideLoading
        }
    }

    fun onAddDataButtonClick() {
        internalViewStateLiveData.value = ViewState.ShowSelectionScreen
    }

    sealed class ViewState {
        object ShowLoading : ViewState()
        object HideLoading : ViewState()
        object ShowSelectionScreen : ViewState()
    }
}