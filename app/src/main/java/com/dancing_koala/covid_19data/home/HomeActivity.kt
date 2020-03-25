package com.dancing_koala.covid_19data.home

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.dancing_koala.covid_19data.DataStorage
import com.dancing_koala.covid_19data.R
import com.dancing_koala.covid_19data.ReportClusterItem
import com.dancing_koala.covid_19data.ReportClusterRenderer
import com.dancing_koala.covid_19data.dataviz.DatavizActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.maps.android.clustering.ClusterManager
import kotlinx.android.synthetic.main.activity_home.*

class HomeActivity : AppCompatActivity(), OnMapReadyCallback {

    private val worldData = DataStorage.instance.countriesData
    private var googleMap: GoogleMap? = null
    private lateinit var clusterManager: ClusterManager<ReportClusterItem>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val mapFragment: SupportMapFragment? = supportFragmentManager.findFragmentById(R.id.homeMapFragment) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        homeDatavizButton.setOnClickListener {
            goToDatavizScreen()
        }

        val worldwide = worldData.first { it.id == 0 }
        updateCounters(worldwide.country.name, "", worldwide.cases, worldwide.deaths, worldwide.recovered)
    }

    private fun goToDatavizScreen() = startActivity(Intent(this, DatavizActivity::class.java))

    override fun onResume() {
        super.onResume()
        updateData()
    }

    override fun onMapReady(map: GoogleMap?) {
        map?.let { nnMap ->
            googleMap = nnMap

            clusterManager = ClusterManager<ReportClusterItem>(this, nnMap).apply {
                renderer = ReportClusterRenderer(applicationContext, nnMap, this)
                nnMap.setOnCameraIdleListener(this)
                nnMap.setOnMarkerClickListener(this)

                setOnClusterItemClickListener {
                    updateCounters(
                        it.report.country.name,
                        "",
                        it.report.cases,
                        it.report.deaths,
                        it.report.recovered
                    )

                    nnMap.animateCamera(CameraUpdateFactory.newLatLng(it.position))

                    true
                }

                setOnClusterClickListener {
                    nnMap.animateCamera(CameraUpdateFactory.newLatLngZoom(it.position, nnMap.cameraPosition.zoom + 1f))
                    true
                }
            }
            updateData()
        }
    }

    private fun updateData() {
        if (googleMap == null) {
            return
        }

        if (clusterManager.markerCollection?.markers?.isEmpty() != false) {


            worldData.forEach { report ->
                if (!report.country.latitude.isNaN()) {
                    clusterManager.addItem(ReportClusterItem(report))
                }
            }
            clusterManager.cluster()
        }
    }

    private fun updateCounters(country: String, state: String, confirmed: Int, deaths: Int, recovered: Int) {
        val location = if (state.isEmpty() || state == country) country else "$state, $country"

        homeDataSetNameTextView.text = location
        homeCasesCountTextView.text = (confirmed.toString())
        homeRecoveredCountTextView.text = (recovered.toString())
        homeDeathsCountTextView.text = (deaths.toString())
    }
}
