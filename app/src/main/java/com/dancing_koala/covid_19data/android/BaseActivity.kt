package com.dancing_koala.covid_19data.android

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.dancing_koala.covid_19data.R
import kotlinx.android.synthetic.main.component_error_banner.*

abstract class BaseActivity<VM : BaseViewModel> : AppCompatActivity() {

    protected abstract val viewModel: VM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.errorLiveData.observe(this, Observer { error ->
            when (error) {
                BaseViewModel.Error.None            -> hideError()
                BaseViewModel.Error.MaybeNoInternet -> showNetworkError()
            }
        })
    }

    protected open fun showNetworkError() {
        bannerErrorText.setText(R.string.internet_connection_error_retry)
        bannerError.show()
    }

    protected open fun hideError() = bannerError.hide()
}