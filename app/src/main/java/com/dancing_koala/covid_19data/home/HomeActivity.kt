package com.dancing_koala.covid_19data.home

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.dancing_koala.covid_19data.R
import com.dancing_koala.covid_19data.ReportClusterItem
import com.dancing_koala.covid_19data.ReportClusterRenderer
import com.dancing_koala.covid_19data.android.BaseActivity
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
import kotlinx.android.synthetic.main.component_error_banner.*

class HomeActivity : BaseActivity<HomeViewModel>(), OnMapReadyCallback {

    override val viewModel: HomeViewModel by viewModels()

    private var googleMap: GoogleMap? = null
    private lateinit var clusterManager: ClusterManager<ReportClusterItem>
    private var chartActionItem: MenuItem? = null
    private var globalActionItem: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val mapFragment: SupportMapFragment? = supportFragmentManager.findFragmentById(R.id.homeMapFragment) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        bannerErrorRetryButton.setOnClickListener { viewModel.onErrorRetryButtonClick() }

        viewModel.viewStateLiveData.observe(this, Observer {
            when (it) {
                is ViewState.UpdateMainReportValues -> with(it.report) {
                    updateCounters(country.name, cases, deaths, recovered)
                    updateGlobalActionState(reportDataSet = this)
                }
                is ViewState.GoToDataviz            -> goToDatavizScreen()
                ViewState.ShowLoading               -> homeLoadingIndicator.show()
                ViewState.HideLoading               -> homeLoadingIndicator.hide()
            }
        })

        viewModel.start()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.home_menu, menu)

        menu?.let {
            chartActionItem = it.findItem(R.id.homeMenuChartAction)
            globalActionItem = it.findItem(R.id.homeMenuGlobalAction)
        }

        viewModel.onMenuItemsCreated()

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.homeMenuGlobalAction -> true.also { viewModel.onGlobalReportButtonClick() }
            R.id.homeMenuChartAction  -> true.also { viewModel.onDatavizButtonClick() }
            else                      -> super.onOptionsItemSelected(item)
        }
    }

    override fun showNetworkError() {
        super.showNetworkError()
        chartActionItem?.setState(enabled = false)
        globalActionItem?.setState(enabled = false)
    }

    override fun hideError() {
        super.hideError()
        chartActionItem?.setState(enabled = true)
        globalActionItem?.setState(enabled = true)
    }

    override fun onMapReady(map: GoogleMap?) {
        map?.let { validMap ->
            googleMap = validMap

            clusterManager = ClusterManager<ReportClusterItem>(this, validMap).apply {
                renderer = ReportClusterRenderer(applicationContext, validMap, this)
                validMap.setOnCameraIdleListener(this)
                validMap.setOnMarkerClickListener(this)

                setOnClusterItemClickListener {
                    viewModel.onMapItemClick(it.report.id)
                    validMap.animateCamera(CameraUpdateFactory.newLatLng(it.position))
                    true
                }

                setOnClusterClickListener {
                    validMap.animateCamera(CameraUpdateFactory.newLatLngZoom(it.position, validMap.cameraPosition.zoom + 1f))
                    true
                }
            }

            viewModel.reportsLiveData.observe(this, Observer { updateMarkers(it) })
        }
    }

    private fun goToDatavizScreen() = startActivity(Intent(this, DatavizActivity::class.java))

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

    private fun updateGlobalActionState(reportDataSet: ReportDataSet) {
        globalActionItem?.apply {
            setState(enabled = !reportDataSet.isGlobalReport())
        }
    }

    private fun ReportDataSet.isGlobalReport() = id == 0

    private fun MenuItem.setState(enabled: Boolean) {
        isEnabled = enabled
        icon?.alpha = if (enabled) 255 else 64
    }
}
