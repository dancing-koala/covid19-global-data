package com.dancing_koala.covid_19data.android

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.dancing_koala.covid_19data.App

abstract class BaseViewModel(application: Application) : AndroidViewModel(application) {
    protected val kodein = (application as App).kodein
}