package com.dancing_koala.covid_19data.dataviz

import android.app.Activity
import android.content.Intent
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
import com.dancing_koala.covid_19data.R
import com.dancing_koala.covid_19data.android.ColoredChipAdapter
import com.dancing_koala.covid_19data.android.ColoredChipItem
import com.dancing_koala.covid_19data.core.Color
import com.dancing_koala.covid_19data.core.ColorPool
import com.dancing_koala.covid_19data.data.DataCategory
import com.dancing_koala.covid_19data.data.StateData
import com.dancing_koala.covid_19data.itemselection.ItemSelectionActivity
import com.dancing_koala.covid_19data.itemselection.ItemSelectionBridge
import com.dancing_koala.covid_19data.itemselection.SelectableItem
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import kotlinx.android.synthetic.main.activity_dataviz.*

class DatavizActivity : AppCompatActivity(), ColoredChipAdapter.Callback {
    companion object {
        const val SELECTION_REQUEST_CODE = 123
    }

    private val worldData = DataStorage.instance.data

    private val colorPool = ColorPool(
        listOf("#1DE5BC", "#EA7369", "#EABD3C", "#C02223").reversed()
    )

    private val simpleChipAdapter = ColoredChipAdapter()
    private val selectedSubjects = mutableListOf<DatavizSubject>()
    private var currentDataCategory = DataCategory.CONFIRMED

    private val selectableItems: ArrayList<SelectableItem> by lazy {
        worldData.map { it.toSelectableItem() } as ArrayList<SelectableItem>
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
        graphLineChart.legend.isEnabled = false

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

        val world = worldData.first { it.localId == 0 }
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

    override fun onChipCloseClick(coloredChipItem: ColoredChipItem) {
        simpleChipAdapter.removeChip(coloredChipItem)
        val subject = selectedSubjects.first {
            it.stateData.localId == coloredChipItem.id
        }
        selectedSubjects.remove(subject)
        colorPool.recycleColor(subject.associatedColor)
        updateChartData()
    }

    private fun onStateDataSelected(data: StateData) {
        colorPool.takeColor()?.let { color ->
            val subject = DatavizSubject(data, color)
            selectedSubjects.add(subject)
            simpleChipAdapter.addChip(data.toSimpleChipItem(color))
            updateChartData()
        }
    }

    private fun onDataCategorySelected(dataCategory: DataCategory) {
        currentDataCategory = dataCategory
        updateChartData()
    }

    private fun updateChartData() {
        graphLineChart.data.clearValues()

        selectedSubjects.forEach { subject ->
            val entries = when (currentDataCategory) {
                DataCategory.CONFIRMED -> subject.stateData.confirmedPerDayData
                DataCategory.RECOVERED -> subject.stateData.recoveredPerDayData
                DataCategory.DEATHS    -> subject.stateData.deathsPerDayData
            }.toChartEntries()

            val lineDataSet = LineDataSet(entries, subject.stateData.fullLabel).apply {
                color = subject.associatedColor.intValue
                setDrawCircles(false)
                setDrawValues(false)

                lineWidth = 2f
            }

            graphLineChart.data.addDataSet(lineDataSet)
        }

        graphLineChart.invalidate()
    }

    private fun showSelectionScreen() {
        ItemSelectionBridge.items = selectableItems
        val intent = Intent(this, ItemSelectionActivity::class.java)
        startActivityForResult(intent, SELECTION_REQUEST_CODE)
    }

    private fun HashMap<String, Int>.toChartEntries(): List<Entry> {
        return keys.sorted().mapIndexed { index, key ->
            Entry(index.toFloat(), this[key]?.toFloat() ?: 0f)
        }
    }

    private fun StateData.toSelectableItem(): SelectableItem = SelectableItem(localId, fullLabel)

    private fun StateData.toSimpleChipItem(backgroundColor: Color): ColoredChipItem =
        ColoredChipItem(localId, fullLabel, backgroundColor)

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