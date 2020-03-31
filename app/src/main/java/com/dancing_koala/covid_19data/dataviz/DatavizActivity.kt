package com.dancing_koala.covid_19data.dataviz

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.dancing_koala.covid_19data.R
import com.dancing_koala.covid_19data.android.ColoredChipAdapter
import com.dancing_koala.covid_19data.android.ColoredChipItem
import com.dancing_koala.covid_19data.core.Color
import com.dancing_koala.covid_19data.core.ColorPool
import com.dancing_koala.covid_19data.data.DataCategory
import com.dancing_koala.covid_19data.data.TimeLineDataSet
import com.dancing_koala.covid_19data.itemselection.ItemSelectionActivity
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

    private val colorPool = ColorPool(
        listOf("#1DE5BC", "#EA7369", "#EABD3C", "#C02223").shuffled()
    )

    private val simpleChipAdapter = ColoredChipAdapter()
    private val selectedSubjects = mutableListOf<DatavizSubject>()
    private var currentDataCategory = DataCategory.CASES

    private val viewModel: DatavizViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dataviz)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
        }

        setUpChart()

        datavizAddDataButton.setOnClickListener { viewModel.onAddDataButtonClick() }

        viewModel.viewStateLiveData.observe(this, Observer {
            when (it) {
                DatavizViewModel.ViewState.ShowSelectionScreen -> showSelectionScreen()
            }
        })

        viewModel.start()
    }

    private fun setUpChart() {
        val customValueFormatter = object : ValueFormatter() {
            private val labels = listOf<String>()

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
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            val selectedId = data?.extras?.getInt(ItemSelectionActivity.EXTRA_ITEM_SELECTED) ?: -1

            if (selectedId == -1) {
                return
            }

            if (requestCode == SELECTION_REQUEST_CODE) {
//                val item = worldData.first { it.localId == selectedId }
//                onStateDataSelected(item)
            }
        }
    }

    override fun onChipCloseClick(coloredChipItem: ColoredChipItem) {
        simpleChipAdapter.removeChip(coloredChipItem)
        val subject = selectedSubjects.first {
            it.timeLineDataSet.id == coloredChipItem.id
        }
        selectedSubjects.remove(subject)
        colorPool.recycleColor(subject.associatedColor)
        updateChartData()
    }

    private fun onStateDataSelected(data: TimeLineDataSet) {
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
                DataCategory.CASES  -> subject.timeLineDataSet.casesTimeLine
                DataCategory.DEATHS -> subject.timeLineDataSet.deathsTimeLine
            }.toChartEntries()

            val lineDataSet = LineDataSet(entries, null).apply {
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
        val intent = Intent(this, ItemSelectionActivity::class.java)
        startActivityForResult(intent, SELECTION_REQUEST_CODE)
    }

    private fun HashMap<String, Int>.toChartEntries(): List<Entry> {
        return keys.sorted().mapIndexed { index, key ->
            Entry(index.toFloat(), this[key]?.toFloat() ?: 0f)
        }
    }

    private fun TimeLineDataSet.toSimpleChipItem(backgroundColor: Color): ColoredChipItem =
        ColoredChipItem(id, locationName, backgroundColor)

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