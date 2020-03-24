package com.dancing_koala.covid_19data.splash

import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.dancing_koala.covid_19data.R
import com.dancing_koala.covid_19data.home.HomeActivity
import kotlinx.android.synthetic.main.activity_splash.*

class SplashActivity : AppCompatActivity() {

    private val rotationAnimator = ValueAnimator.ofFloat(0f, 360f).apply {
        duration = 1200L
        repeatCount = ValueAnimator.INFINITE
        repeatMode = ValueAnimator.RESTART
        interpolator = LinearInterpolator()

        addUpdateListener { currentDataRefreshIcon.rotation = animatedValue as Float }
    }

    private val viewModel: SplashViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        viewModel.viewStateLiveData.observe(this, Observer {
            when (it) {
                SplashViewModel.ViewState.StopAnimation -> stopAnimatingAndSetAsDone(currentDataRefreshIcon)
                SplashViewModel.ViewState.GoToMapScreen -> goToMapScreen()
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
        rotationAnimator.cancel()
        imageView.rotation = 0f
        imageView.setImageResource(R.drawable.ic_check_circle)
    }

    private fun goToMapScreen() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }
}