package com.dancing_koala.covid_19data.dataviz

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
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.dancing_koala.covid_19data.DataStorage
import com.dancing_koala.covid_19data.ItemSelectionActivity
import com.dancing_koala.covid_19data.R
import com.dancing_koala.covid_19data.android.SimpleChipAdapter
import com.dancing_koala.covid_19data.android.SimpleChipItem
import com.dancing_koala.covid_19data.data.DataCategory
import com.dancing_koala.covid_19data.data.StateData
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import kotlinx.android.synthetic.main.activity_dataviz.*
import kotlin.math.min

class DatavizActivity : AppCompatActivity(), SimpleChipAdapter.Callback {
    companion object {
        const val SELECTION_REQUEST_CODE = 123
    }


    private val worldData = DataStorage.instance.data

    private val lineColors = listOf("#AF4BCE", "#DB4CB2", "#EB548C", "#EA7369").shuffled().toTypedArray()
    private val simpleChipAdapter = SimpleChipAdapter()
    private val selectedSubjects = mutableListOf<StateData>()
    private var currentDataCategory = DataCategory.CONFIRMED

    private val selectableItems: ArrayList<ItemSelectionActivity.SelectableItem> by lazy {
        worldData.map { it.toSelectableItem() } as ArrayList<ItemSelectionActivity.SelectableItem>
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dataviz)

        datavizAddDataButton.setOnClickListener {
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

        simpleChipAdapter.callback = this
        graphSubjectSlider.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        graphSubjectSlider.adapter = simpleChipAdapter

        graphLineChart.description?.isEnabled = false
        graphLineChart.isAutoScaleMinMaxEnabled = true
        graphLineChart.data = LineData()
        graphLineChart.legend.apply {

        }

        graphLineChart.xAxis.apply {
            setDrawLabels(true)
            valueFormatter = customValueFormatter
            position = XAxis.XAxisPosition.BOTTOM
        }

        graphLineChart.axisRight.isEnabled = false
        graphLineChart.axisLeft.apply {
            enableGridDashedLine(10f, 10f, 0f)
            axisMinimum = 0f
        }

        val world = worldData.first { it.country == "Worldwide" }
        onStateDataSelected(world)
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

    override fun onChipCloseClick(simpleChipItem: SimpleChipItem) {
        simpleChipAdapter.removeChip(simpleChipItem)
        val subject = selectedSubjects.first {
            it.localId == simpleChipItem.id
        }
        selectedSubjects.remove(subject)
        onDataCategorySelected(currentDataCategory)
    }

    private fun onStateDataSelected(data: StateData) {
        selectedSubjects.add(data)
        simpleChipAdapter.addChip(data.toSimpleChipItem())
        onDataCategorySelected(currentDataCategory)
    }

    private fun onDataCategorySelected(dataCategory: DataCategory) {
        currentDataCategory = dataCategory

        graphLineChart.data.clearValues()

        selectedSubjects.forEachIndexed { index, stateData ->
            val data = when (dataCategory) {
                DataCategory.CONFIRMED -> stateData.confirmedPerDayData
                DataCategory.RECOVERED -> stateData.recoveredPerDayData
                DataCategory.DEATHS    -> stateData.deathsPerDayData
            }
            val colorIndex = min(lineColors.lastIndex, index)
            val colorHex = lineColors[colorIndex]

            displayTimeSeriesDataSet(stateData.fullLabel, colorHex, data)
        }
    }


    private fun displayTimeSeriesDataSet(dataSetLabel: String, colorHex: String, dataSet: HashMap<String, Int>) {
        val entries = dataSet.toChartEntries()

        val lineDataSet = LineDataSet(entries, dataSetLabel).apply {
            color = Color.parseColor(colorHex)
            setDrawCircles(false)
            setDrawValues(false)

            lineWidth = 2f
        }

        graphLineChart.data.addDataSet(lineDataSet)
        updateGraph()
    }

    private fun showSelectionScreen() {
        val intent = Intent(this, ItemSelectionActivity::class.java).apply {
            putExtra(ItemSelectionActivity.EXTRA_ITEMS, selectableItems)
        }

        startActivityForResult(intent, SELECTION_REQUEST_CODE)
    }

    private fun updateGraph() {
        graphLineChart.invalidate()
    }

    private fun HashMap<String, Int>.toChartEntries(): List<Entry> {
        return keys.sorted().mapIndexed { index, key ->
            Entry(index.toFloat(), this[key]?.toFloat() ?: 0f)
        }
    }

    private fun StateData.toSelectableItem(): ItemSelectionActivity.SelectableItem =
        ItemSelectionActivity.SelectableItem(localId, fullLabel)

    private fun StateData.toSimpleChipItem(): SimpleChipItem = SimpleChipItem(localId, fullLabel)

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