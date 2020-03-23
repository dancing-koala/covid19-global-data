package com.dancing_koala.covid_19data

import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.dancing_koala.covid_19data.data.DataTransformer
import com.dancing_koala.covid_19data.dataviz.DatavizActivity
import com.dancing_koala.covid_19data.network.RemoteDataRepository
import kotlinx.android.synthetic.main.activity_splash.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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

    private val refreshIcons: MutableList<ImageView> by lazy {
        mutableListOf(currentDataRefreshIcon, timeSeriesConfirmedIcon, timeSeriesDeathsIcon, timeSeriesRecoveredIcon)
    }

    private val remoteDataRepository = RemoteDataRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        CoroutineScope(Dispatchers.Main).launch {
            delay(250L)

            val dailyReports = remoteDataRepository.getLastDailyReports()
            stopAnimatingAndSetAsDone(currentDataRefreshIcon)
            val confirmedTimeSeries = remoteDataRepository.getConfirmedTimeSeries()
            stopAnimatingAndSetAsDone(timeSeriesConfirmedIcon)
            val deathsTimeSeries = remoteDataRepository.getDeathsTimeSeries()
            stopAnimatingAndSetAsDone(timeSeriesDeathsIcon)
            val recoveredTimeSeries = remoteDataRepository.getRecoveredTimeSeries()
            stopAnimatingAndSetAsDone(timeSeriesRecoveredIcon)
            rotationAnimator.cancel()

            delay(500L)

            downloadIconsContainer.visibility = View.GONE
            processingDataLabel.visibility = View.VISIBLE

            val processedData = DataTransformer().transform(
                dailyReports, confirmedTimeSeries, deathsTimeSeries, recoveredTimeSeries
            )

            DataStorage.instance.updateData(processedData)

            delay(500L)

            goToMapScreen()
        }
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
    }

    private fun goToMapScreen() {
        startActivity(Intent(this, DatavizActivity::class.java))
        finish()
    }
}