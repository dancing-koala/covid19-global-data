package com.dancing_koala.covid_19data.splash

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dancing_koala.covid_19data.DataStorage
import com.dancing_koala.covid_19data.network.lmaoninja.LmaoNinjaApiRemoteDataRepository
import kotlinx.coroutines.launch

class SplashViewModel(application: Application) : AndroidViewModel(application) {

    private val internalViewStateLiveData = MutableLiveData<ViewState>()
    val viewStateLiveData: LiveData<ViewState>
        get() = internalViewStateLiveData

    private val remoteDataRepository = LmaoNinjaApiRemoteDataRepository()

    fun start() {
        viewModelScope.launch {
            val countriesData = remoteDataRepository.getCountriesData()

            internalViewStateLiveData.value = ViewState.StopAnimation

            DataStorage.instance.updateCountriesData(countriesData)

            internalViewStateLiveData.value = ViewState.GoToMapScreen
        }
    }

    sealed class ViewState {
        object StopAnimation : ViewState()
        object GoToMapScreen : ViewState()
    }
}
