package com.dancing_koala.covid_19data.home

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.dancing_koala.covid_19data.R
import com.dancing_koala.covid_19data.ReportClusterItem
import com.dancing_koala.covid_19data.ReportClusterRenderer
import com.dancing_koala.covid_19data.android.hide
import com.dancing_koala.covid_19data.android.show
import com.dancing_koala.covid_19data.data.ReportDataSet
import com.dancing_koala.covid_19data.dataviz.DatavizActivity
import com.dancing_koala.covid_19data.home.HomeViewModel.ViewState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.maps.android.clustering.ClusterManager
import kotlinx.android.synthetic.main.activity_home.*

class HomeActivity : AppCompatActivity(), OnMapReadyCallback {

    private val viewModel: HomeViewModel by viewModels()

    private var googleMap: GoogleMap? = null
    private lateinit var clusterManager: ClusterManager<ReportClusterItem>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val mapFragment: SupportMapFragment? = supportFragmentManager.findFragmentById(R.id.homeMapFragment) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        homeDatavizButton.setOnClickListener { viewModel.onDatavizButtonClick() }

        viewModel.viewStateLiveData.observe(this, Observer {
            when (it) {
                is ViewState.UpdateMainReportValues -> with(it.report) { updateCounters(country.name, cases, deaths, recovered) }
                is ViewState.GoToDataviz            -> goToDatavizScreen()
                ViewState.ShowLoading               -> homeLoadingIndicator.show()
                ViewState.HideLoading               -> homeLoadingIndicator.hide()
            }
        })

        viewModel.start()
    }

    private fun goToDatavizScreen() = startActivity(Intent(this, DatavizActivity::class.java))

    override fun onMapReady(map: GoogleMap?) {
        map?.let { validMap ->
            googleMap = validMap

            clusterManager = ClusterManager<ReportClusterItem>(this, validMap).apply {
                renderer = ReportClusterRenderer(applicationContext, validMap, this)
                validMap.setOnCameraIdleListener(this)
                validMap.setOnMarkerClickListener(this)

                setOnClusterItemClickListener {
                    updateCounters(
                        it.report.country.name,
                        it.report.cases,
                        it.report.deaths,
                        it.report.recovered
                    )

                    validMap.animateCamera(CameraUpdateFactory.newLatLng(it.position))

                    true
                }

                setOnClusterClickListener {
                    validMap.animateCamera(CameraUpdateFactory.newLatLngZoom(it.position, validMap.cameraPosition.zoom + 1f))
                    true
                }
            }

            viewModel.reportsLiveData.observe(this, Observer {
                updateMarkers(it)
            })
        }
    }

    private fun updateMarkers(data: List<ReportDataSet>) {
        googleMap ?: return

        if (clusterManager.markerCollection?.markers?.isEmpty() != false) {
            data.forEach { report ->
                if (!report.country.latitude.isNaN()) {
                    clusterManager.addItem(ReportClusterItem(report))
                }
            }

            clusterManager.cluster()
        }
    }

    private fun updateCounters(country: String, confirmed: Int, deaths: Int, recovered: Int) {
        homeDataSetNameTextView.text = country
        homeCasesCountTextView.text = confirmed.toString()
        homeRecoveredCountTextView.text = recovered.toString()
        homeDeathsCountTextView.text = deaths.toString()
    }
}
