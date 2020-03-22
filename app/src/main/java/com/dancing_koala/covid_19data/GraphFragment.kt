package com.dancing_koala.covid_19data

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.dancing_koala.covid_19data.data.DataCategory
import com.dancing_koala.covid_19data.data.StateData
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import kotlinx.android.synthetic.main.fragment_graph.*
import kotlinx.android.synthetic.main.fragment_graph.view.*

class GraphFragment : Fragment() {

    companion object {
        const val SELECTION_REQUEST_CODE = 123
        fun newInstance(): GraphFragment = GraphFragment()
    }

    private val worldData = DataStorage.instance.data

    private lateinit var lineChart: LineChart
    private lateinit var currentStateData: StateData

    private var currentDataCategory = DataCategory.CONFIRMED

    private val selectableItems: ArrayList<ItemSelectionActivity.SelectableItem> by lazy {
        worldData.map { it.toSelectableItem() } as ArrayList<ItemSelectionActivity.SelectableItem>
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_graph, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.graphFirstDataSetNameButton.setOnClickListener {
            showSelectionScreen()
        }

        val customValueFormatter = object : ValueFormatter() {
            private val labels = worldData.first().confirmedPerDayData.keys.sorted()

            override fun getFormattedValue(value: Float): String = labels[value.toInt()]
        }

        graphDataCategorySpinner.adapter = DataCategorySpinnerAdapter()
        graphDataCategorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) = throw NotImplementedError("$this.onNothingSelected(parent)")

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                onDataCategorySelected(DataCategory.values()[position])
            }
        }

        lineChart = view.graphLineChart
        lineChart.description?.isEnabled = false

        lineChart.xAxis.apply {
            setDrawLabels(true)
            valueFormatter = customValueFormatter
            position = XAxis.XAxisPosition.BOTTOM
        }

        lineChart.axisRight.isEnabled = false
        lineChart.axisLeft.apply {
            enableGridDashedLine(10f, 10f, 0f)
            axisMinimum = 0f
        }

        currentStateData = worldData.first { it.country == "Worldwide" }
        onStateDataSelected(currentStateData)
    }

    private fun onDataCategorySelected(dataCategory: DataCategory) {
        currentDataCategory = dataCategory

        val data = when (dataCategory) {
            DataCategory.CONFIRMED -> currentStateData.confirmedPerDayData
            DataCategory.RECOVERED -> currentStateData.recoveredPerDayData
            DataCategory.DEATHS    -> currentStateData.deathsPerDayData
        }

        displayTimeSeriesDataSet(currentStateData.fullLabel, data)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            val selectedId = data?.extras?.getInt(ItemSelectionActivity.EXTRA_ITEM_SELECTED) ?: -1

            if (selectedId == -1) {
                return
            }

            val item = worldData.first { it.localId == selectedId }
            when (requestCode) {
                SELECTION_REQUEST_CODE -> onStateDataSelected(item)
            }
        }
    }

    private fun onStateDataSelected(data: StateData) {
        currentStateData = data
        graphFirstDataSetNameButton.text = data.fullLabel
        onDataCategorySelected(currentDataCategory)
    }

    private fun displayTimeSeriesDataSet(dataSetLabel: String, dataSet: HashMap<String, Int>) {
        val entries = dataSet.toChartEntries()

        val lineDataSet = LineDataSet(entries, dataSetLabel).apply {
            color = Color.parseColor("#009688")
            setDrawCircles(false)
            setDrawValues(false)

            lineWidth = 2f
        }

        lineChart.data = LineData(listOf(lineDataSet))
        lineChart.invalidate()
    }

    private fun showSelectionScreen() {
        val intent = Intent(context, ItemSelectionActivity::class.java).apply {
            putExtra(ItemSelectionActivity.EXTRA_ITEMS, selectableItems)
        }

        startActivityForResult(intent, SELECTION_REQUEST_CODE)
    }

    private fun HashMap<String, Int>.toChartEntries(): List<Entry> {
        return keys.sorted().mapIndexed { index, key ->
            Entry(index.toFloat(), this[key]?.toFloat() ?: 0f)
        }
    }

    private fun StateData.toSelectableItem(): ItemSelectionActivity.SelectableItem =
        ItemSelectionActivity.SelectableItem(localId, fullLabel)

    private class DataCategorySpinnerAdapter : BaseAdapter() {
        private val items = DataCategory.values()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view = convertView ?: LayoutInflater.from(parent?.context).inflate(R.layout.item_data_catagory, parent, false)
            (view as TextView).text = getItem(position)

            return view
        }

        override fun getItem(position: Int): String = items[position].label

        override fun getItemId(position: Int): Long = items[position].ordinal.toLong()

        override fun getCount(): Int = items.size
    }
}
