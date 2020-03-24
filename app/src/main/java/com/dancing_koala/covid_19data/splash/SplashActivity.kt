package com.dancing_koala.covid_19data.splash

import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.dancing_koala.covid_19data.HomeActivity
import com.dancing_koala.covid_19data.R
import com.dancing_koala.covid_19data.splash.SplashViewModel.ViewState.*
import kotlinx.android.synthetic.main.activity_splash.*

class SplashActivity : AppCompatActivity() {

    private val rotationAnimator = ValueAnimator.ofFloat(0f, 360f).apply {
        duration = 1200L
        repeatCount = ValueAnimator.INFINITE
        repeatMode = ValueAnimator.RESTART
        interpolator = LinearInterpolator()

        addUpdateListener {
            refreshIcons.forEach { it.rotation = animatedValue as Float }
        }
    }

    private val viewModel: SplashViewModel by viewModels()

    private val refreshIcons: MutableList<ImageView> by lazy {
        mutableListOf(currentDataRefreshIcon, timeSeriesConfirmedIcon, timeSeriesDeathsIcon, timeSeriesRecoveredIcon)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        viewModel.viewStateLiveData.observe(this, Observer {
            when (it) {
                StopAnimation.CurrentData         -> stopAnimatingAndSetAsDone(currentDataRefreshIcon)
                StopAnimation.ConfirmedTimeSeries -> stopAnimatingAndSetAsDone(timeSeriesConfirmedIcon)
                StopAnimation.DeathsTimeSeries    -> stopAnimatingAndSetAsDone(timeSeriesDeathsIcon)
                StopAnimation.RecoveredTimeSeries -> stopAnimatingAndSetAsDone(timeSeriesRecoveredIcon)
                ShowProcessingData                -> {
                    downloadIconsContainer.visibility = View.GONE
                    processingDataLabel.visibility = View.VISIBLE
                }
                GoToMapScreen                     -> goToMapScreen()
            }
        })

        viewModel.start()
    }

    override fun onResume() {
        super.onResume()
        rotationAnimator.start()
    }

    override fun onPause() {
        super.onPause()
        rotationAnimator.pause()
    }

    private fun stopAnimatingAndSetAsDone(imageView: ImageView) {
        rotationAnimator.pause()
        refreshIcons.remove(imageView)
        rotationAnimator.start()
        imageView.rotation = 0f
        imageView.setImageResource(R.drawable.ic_check_circle)

        if (refreshIcons.isEmpty()) {
            rotationAnimator.cancel()
        }
    }

    private fun goToMapScreen() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }
}