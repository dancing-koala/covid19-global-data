package com.dancing_koala.covid_19data.splash

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dancing_koala.covid_19data.DataStorage
import com.dancing_koala.covid_19data.data.DataTransformer
import com.dancing_koala.covid_19data.network.jhu.JHURemoteDataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SplashViewModel(application: Application) : AndroidViewModel(application) {

    private val internalViewStateLiveData = MutableLiveData<ViewState>()
    val viewStateLiveData: LiveData<ViewState>
        get() = internalViewStateLiveData

    private val remoteDataRepository = JHURemoteDataRepository()

    fun start() {
        viewModelScope.launch {
            val dailyReports = remoteDataRepository.getLastDailyReports()
            internalViewStateLiveData.value = ViewState.StopAnimation.CurrentData

            val confirmedTimeSeries = remoteDataRepository.getConfirmedTimeSeries()
            internalViewStateLiveData.value = ViewState.StopAnimation.ConfirmedTimeSeries

            val deathsTimeSeries = remoteDataRepository.getDeathsTimeSeries()
            internalViewStateLiveData.value = ViewState.StopAnimation.DeathsTimeSeries

            val recoveredTimeSeries = remoteDataRepository.getRecoveredTimeSeries()
            internalViewStateLiveData.value = ViewState.StopAnimation.RecoveredTimeSeries


            internalViewStateLiveData.value = ViewState.ShowProcessingData

            val processedData = withContext(Dispatchers.Default) {
                DataTransformer().transform(
                    dailyReports, confirmedTimeSeries, deathsTimeSeries, recoveredTimeSeries
                )
            }

            DataStorage.instance.updateData(processedData)

            internalViewStateLiveData.value = ViewState.GoToMapScreen
        }
    }

    sealed class ViewState {
        sealed class StopAnimation : ViewState() {
            object CurrentData : StopAnimation()
            object ConfirmedTimeSeries : StopAnimation()
            object DeathsTimeSeries : StopAnimation()
            object RecoveredTimeSeries : StopAnimation()
        }

        object ShowProcessingData : ViewState()
        object GoToMapScreen : ViewState()
    }
}
