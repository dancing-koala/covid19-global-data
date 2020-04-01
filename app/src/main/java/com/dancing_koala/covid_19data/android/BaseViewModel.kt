package com.dancing_koala.covid_19data.android

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.dancing_koala.covid_19data.App

abstract class BaseViewModel(application: Application) : AndroidViewModel(application) {
    protected val kodein = (application as App).kodein

    val errorLiveData: LiveData<Error>
        get() = _errorLiveData

    protected val _errorLiveData = MutableLiveData<Error>()

    sealed class Error {
        object None : Error()
        object Unknown : Error()
        object MaybeNoInternet : Error()
    }
}