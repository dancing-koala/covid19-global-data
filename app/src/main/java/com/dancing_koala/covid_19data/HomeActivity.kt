package com.dancing_koala.covid_19data

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.maps.android.clustering.ClusterManager
import kotlinx.android.synthetic.main.activity_home.*

class HomeActivity : AppCompatActivity(), OnMapReadyCallback {

    private val worldData = DataStorage.instance.data
    private var googleMap: GoogleMap? = null
    private var clusterManager: ClusterManager<ReportClusterItem>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val mapFragment: SupportMapFragment? = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        graphButton.setOnClickListener {
            goToGraphScreen()
        }

        val worldwide = worldData.first { it.localId == 0 }
        updateCounters(worldwide.country, "", worldwide.confirmed, worldwide.deaths, worldwide.recovered)
        updateData()
    }

    private fun goToGraphScreen() {
        startActivity(Intent(this, GraphActivity::class.java))
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
                worldData.forEach { report ->
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
