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
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.dancing_koala.covid_19data.R
import com.dancing_koala.covid_19data.android.*
import com.dancing_koala.covid_19data.data.DataCategory
import com.dancing_koala.covid_19data.dataviz.DatavizViewModel.ViewState
import com.dancing_koala.covid_19data.itemselection.ItemSelectionActivity
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import kotlinx.android.synthetic.main.activity_dataviz.*
import kotlinx.android.synthetic.main.component_error_banner.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DatavizActivity : BaseActivity<DatavizViewModel>(), ColoredChipAdapter.Callback {
    companion object {
        const val SELECTION_REQUEST_CODE = 123
    }

    private val simpleChipAdapter = ColoredChipAdapter(this)

    override val viewModel: DatavizViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dataviz)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_close)
            title = getString(R.string.dataviz_screen_title)
        }

        bannerErrorRetryButton.setOnClickListener { viewModel.onErrorRetryButtonClick() }

        setUpChart()
        setUpViewModel()

        viewModel.start()
    }

    private fun setUpChart() {
        datavizDataCategorySpinner.adapter = DataCategorySpinnerAdapter()
        datavizDataCategorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) = throw NotImplementedError("$this.onNothingSelected(parent)")

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                viewModel.onDataCategorySelected(position)
            }
        }

        datavizSubjectSlider.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        datavizSubjectSlider.adapter = simpleChipAdapter

        datavizLineChart.apply {
            description?.isEnabled = false
            isAutoScaleMinMaxEnabled = true
            data = LineData()
            legend.isEnabled = false
        }

        datavizLineChart.xAxis.apply {
            setDrawLabels(true)
            labelRotationAngle = -45f
            position = XAxis.XAxisPosition.BOTTOM
        }

        datavizLineChart.axisRight.isEnabled = false
        datavizLineChart.axisLeft.apply {
            enableGridDashedLine(10f, 10f, 0f)
            axisMinimum = 0f
        }
    }

    private fun setUpViewModel() {
        viewModel.viewStateLiveData.observe(this, Observer {
            when (it) {
                ViewState.ShowSelectionScreen    -> showSelectionScreen()
                is ViewState.ShowLabels          -> {
                    val customValueFormatter = object : ValueFormatter() {
                        private val labels = it.labels
                        override fun getFormattedValue(value: Float): String = labels[value.toInt()]
                    }

                    datavizLineChart.apply {
                        xAxis.valueFormatter = customValueFormatter
                        invalidate()
                    }
                }
                ViewState.MaximumSubjectsReached -> showMaximumSubjectsReached()
            }
        })

        viewModel.subjectsLiveData.observe(this, Observer {
            updateChips(it.subjects)
            updateChart(it)
            updateComponentsVisibility(it.subjects)
        })
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
            if (requestCode == SELECTION_REQUEST_CODE) {
                viewModel.onDataSetSelected(selectedId)
            }
        }
    }

    override fun onChipCloseClick(coloredChipItem: ColoredChipItem) =
        viewModel.onRemoveItemClick(coloredChipItem.id)

    override fun onAddButtonChipClick() = viewModel.onAddDataButtonClick()

    private fun updateChart(dataVizCategoryWithSubjects: DataVizCategoryWithSubjects) {
        datavizLineChart.data.clearValues()

        val selectedSubjects = dataVizCategoryWithSubjects.subjects

        selectedSubjects.forEach { subject ->
            val entries = when (dataVizCategoryWithSubjects.dataCategory) {
                DataCategory.CASES  -> subject.timeLineDataSet.casesTimeLine
                DataCategory.DEATHS -> subject.timeLineDataSet.deathsTimeLine
            }.toChartEntries()

            val lineDataSet = LineDataSet(entries, null).apply {
                color = subject.associatedColor.intValue
                setDrawCircles(false)
                setDrawValues(false)
                lineWidth = 2f
            }

            datavizLineChart.data.addDataSet(lineDataSet)
        }

        lifecycleScope.launch {
            delay(250)
            datavizLineChart.invalidate()
        }
    }

    private fun updateComponentsVisibility(subjects: List<DatavizSubject>) = if (subjects.isEmpty()) {
        datavizDataCategorySpinnerCard.hide()
        datavizLineChart.hide()
        datavizAdvice.hide()

        datavizEmptyText.show()
    } else {
        datavizDataCategorySpinnerCard.show()
        datavizLineChart.show()
        datavizAdvice.show()

        datavizEmptyText.hide()
    }

    private fun updateChips(subjects: List<DatavizSubject>) =
        simpleChipAdapter.updateChips(subjects.map { it.toSimpleChipItem() }.reversed())

    private fun showSelectionScreen() {
        val intent = Intent(this, ItemSelectionActivity::class.java)
        startActivityForResult(intent, SELECTION_REQUEST_CODE)
    }

    private fun showMaximumSubjectsReached() =
        Toast.makeText(this, getString(R.string.dataviz_maximum_reached), Toast.LENGTH_SHORT).show()

    private fun HashMap<String, Int>.toChartEntries(): List<Entry> =
        keys.sorted().mapIndexed { index, key -> Entry(index.toFloat(), this[key]?.toFloat() ?: 0f) }

    private fun DatavizSubject.toSimpleChipItem(): ColoredChipItem =
        ColoredChipItem(timeLineDataSet.id, timeLineDataSet.locationName, associatedColor)

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