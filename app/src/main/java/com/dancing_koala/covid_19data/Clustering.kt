package com.dancing_koala.covid_19data

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import com.dancing_koala.covid_19data.data.AreaData
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.google.maps.android.ui.IconGenerator

@SuppressLint("InflateParams")
class ReportClusterRenderer(appContext: Context, map: GoogleMap, clusterManager: ClusterManager<ReportClusterItem>) :
    DefaultClusterRenderer<ReportClusterItem>(appContext, map, clusterManager) {

    private val baseMapMarkerSize: Int = appContext.resources.getDimension(R.dimen.base_map_marker_size).toInt()
    private val bigMapMarkerSize: Int = (baseMapMarkerSize * 1.25f).toInt()
    private val veryBigMapMarkerSize: Int = (baseMapMarkerSize * 1.5f).toInt()

    private val itemIconGenerator = IconGenerator(appContext)
    private val itemTextViewCanvas: TextView

    init {
        val inflater = LayoutInflater.from(appContext)
        val layoutParams = ViewGroup.LayoutParams(baseMapMarkerSize, baseMapMarkerSize)

        itemTextViewCanvas = inflater.inflate(R.layout.item_map_marker, null) as TextView
        itemTextViewCanvas.layoutParams = layoutParams
        itemIconGenerator.setContentView(itemTextViewCanvas)
        itemIconGenerator.setBackground(null)
    }

    override fun onBeforeClusterItemRendered(item: ReportClusterItem?, markerOptions: MarkerOptions?) {
        item ?: return

        computeTextViewStyle(itemTextViewCanvas, item.count)
        computeIcon(itemIconGenerator, markerOptions)
    }

    override fun onBeforeClusterRendered(cluster: Cluster<ReportClusterItem>?, markerOptions: MarkerOptions?) {
        cluster ?: return

        val total = cluster.items.sumBy { it.count }
        computeTextViewStyle(itemTextViewCanvas, total)
        computeIcon(itemIconGenerator, markerOptions)
    }

    private fun computeTextViewStyle(textView: TextView, count: Int) {
        textView.text = count.toString()
        val layoutParams = textView.layoutParams

        when {
            count > 9999 -> if (layoutParams.width != veryBigMapMarkerSize) {
                textView.layoutParams = layoutParams.apply {
                    width = veryBigMapMarkerSize
                    height = veryBigMapMarkerSize
                }
            }
            count > 4999 -> if (layoutParams.width != bigMapMarkerSize) {
                textView.layoutParams = layoutParams.apply {
                    width = bigMapMarkerSize
                    height = bigMapMarkerSize
                }
            }
            else         -> if (layoutParams.width != baseMapMarkerSize) {
                textView.layoutParams = layoutParams.apply {
                    width = baseMapMarkerSize
                    height = baseMapMarkerSize
                }
            }
        }
    }

    private fun computeIcon(iconGenerator: IconGenerator, markerOptions: MarkerOptions?) {
        val generatedIcon = iconGenerator.makeIcon()
        markerOptions?.apply {
            icon(BitmapDescriptorFactory.fromBitmap(generatedIcon))
            title("")
            snippet("")
        }
    }
}

class ReportClusterItem(val report: AreaData) : ClusterItem {

    private val title = report.country
    private val latLng = LatLng(report.latitude, report.longitude)

    val count = report.confirmed

    override fun getSnippet(): String = ""

    override fun getTitle(): String = title

    override fun getPosition(): LatLng = latLng
}