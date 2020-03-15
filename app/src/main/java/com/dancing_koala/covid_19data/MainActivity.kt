package com.dancing_koala.covid_19data

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.maps.android.clustering.ClusterManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private var dailyReportList = listOf<DailyReport>()
    private var googleMap: GoogleMap? = null
    private var clusterManager: ClusterManager<ReportClusterItem>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mapFragment: SupportMapFragment? = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        val repository = RemoteDataRepository()

        CoroutineScope(Dispatchers.Main).launch {
            dailyReportList = repository.getLastDailyReport()

            var confirmed = 0
            var deaths = 0
            var recovered = 0

            dailyReportList.forEach {
                confirmed += it.confirmed
                deaths += it.deaths
                recovered += it.recovered
            }

            updateCounters("Worldwide", "", confirmed, deaths, recovered)

            updateData()
        }
    }

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
                        it.report.country,
                        it.report.state,
                        it.report.confirmed,
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
        googleMap?.let {
            if (clusterManager?.markerCollection?.markers?.isEmpty() != false) {
                dailyReportList.forEach { report ->
                    clusterManager?.addItem(ReportClusterItem(report))
                }
            }

            clusterManager?.cluster()
        }
    }

    private fun updateCounters(country: String, state: String, confirmed: Int, deaths: Int, recovered: Int) {
        val location = if (state.isEmpty() || state == country) country else "$state, $country"

        locationCountTextView.text = location
        confirmedCountTextView.text = ("Confirmed: $confirmed")
        recoveredCountTextView.text = ("Recovered: $recovered")
        deathsCountTextView.text = ("Deaths: $deaths")
    }
}
