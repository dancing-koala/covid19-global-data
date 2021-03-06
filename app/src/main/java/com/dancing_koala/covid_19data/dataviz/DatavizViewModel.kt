package com.dancing_koala.covid_19data.dataviz

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dancing_koala.covid_19data.android.BaseViewModel
import com.dancing_koala.covid_19data.core.ColorPool
import com.dancing_koala.covid_19data.data.DataCategory
import com.dancing_koala.covid_19data.data.TimeLineDataSet
import com.dancing_koala.covid_19data.network.lmaoninja.LmaoNinjaApiDataRepository
import com.dancing_koala.covid_19data.network.lmaoninja.LmaoNinjaApiDataRepository.ApiResult
import kotlinx.coroutines.launch
import org.kodein.di.generic.instance

class DatavizViewModel(application: Application) : BaseViewModel(application) {

    val viewStateLiveData: LiveData<ViewState>
        get() = _viewStateLiveData

    val subjectsLiveData: LiveData<DataVizCategoryWithSubjects>
        get() = _subjectsLiveData

    private var currentDataCategory = DataCategory.CASES

    private val dataRepository: LmaoNinjaApiDataRepository by kodein.instance()
    private val dataSets = mutableListOf<TimeLineDataSet>()
    private val _viewStateLiveData = MutableLiveData<ViewState>()
    private val _subjectsLiveData = MutableLiveData(
        DataVizCategoryWithSubjects(currentDataCategory, listOf())
    )

    private val colorPool = ColorPool(listOf("#1DE5BC", "#EA7369", "#EABD3C", "#5DBF63").shuffled())
    private val selectedSubjects = mutableListOf<DatavizSubject>()

    fun start() {
        viewModelScope.launch {
            _errorLiveData.value = Error.None
            _viewStateLiveData.value = ViewState.ShowLoading

            val timeLineDataSetsResult = dataRepository.getTimeLineDataSets()

            if (timeLineDataSetsResult is ApiResult.Success) {
                val timeLineDataSets = timeLineDataSetsResult.value as List<TimeLineDataSet>

                dataSets.apply {
                    clear()
                    addAll(timeLineDataSets)
                }

                val labels = dataSets.first().casesTimeLine.keys.sorted()
                _viewStateLiveData.value = ViewState.ShowLabels(labels)
            } else {
                when (timeLineDataSetsResult) {
                    ApiResult.Failure.NetworkError -> _errorLiveData.value = Error.MaybeNoInternet
                    else                           -> _errorLiveData.value = Error.Unknown
                }
            }
            _viewStateLiveData.value = ViewState.HideLoading
        }
    }

    fun onAddDataButtonClick() {
        if (dataSets.isEmpty()) {
            return
        }

        _viewStateLiveData.value = if (colorPool.isEmpty) {
            ViewState.MaximumSubjectsReached
        } else {
            ViewState.ShowSelectionScreen
        }
    }

    fun onDataSetSelected(selectedId: Int) {
        if (selectedId in selectedSubjects.map { it.timeLineDataSet.id }) {
            return
        }

        dataSets.firstOrNull { it.id == selectedId }?.let { dataSet ->
            colorPool.takeColor()?.let { color ->
                val subject = DatavizSubject(dataSet, color)
                selectedSubjects.add(subject)
                notifyNewSelectedSubjects()
            }
        }
    }

    fun onDataCategorySelected(position: Int) {
        currentDataCategory = DataCategory.values()[position]
        notifyNewSelectedSubjects()
    }

    private fun notifyNewSelectedSubjects() {
        _subjectsLiveData.value = DataVizCategoryWithSubjects(
            currentDataCategory,
            selectedSubjects.toList()//Pass a copy for safety
        )
    }

    fun onRemoveItemClick(id: Int) {
        selectedSubjects.firstOrNull { it.timeLineDataSet.id == id }?.let { subject ->
            selectedSubjects.remove(subject)
            colorPool.recycleColor(subject.associatedColor)
            notifyNewSelectedSubjects()
        }
    }

    fun onErrorRetryButtonClick() = start()

    sealed class ViewState {
        object ShowLoading : ViewState()
        object HideLoading : ViewState()
        object ShowSelectionScreen : ViewState()
        class ShowLabels(val labels: List<String>) : ViewState()
        object MaximumSubjectsReached : ViewState()
    }
}
