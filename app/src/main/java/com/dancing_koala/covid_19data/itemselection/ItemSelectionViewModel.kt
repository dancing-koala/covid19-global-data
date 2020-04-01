package com.dancing_koala.covid_19data.itemselection

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dancing_koala.covid_19data.android.BaseViewModel
import com.dancing_koala.covid_19data.data.TimeLineDataSet
import com.dancing_koala.covid_19data.network.lmaoninja.LmaoNinjaApiDataRepository
import com.dancing_koala.covid_19data.network.lmaoninja.LmaoNinjaApiDataRepository.ApiResult
import kotlinx.coroutines.launch
import org.kodein.di.generic.instance
import java.util.*

class ItemSelectionViewModel(application: Application) : BaseViewModel(application) {

    val viewStateLiveData: LiveData<ViewState>
        get() = internalViewStateLiveData

    private val internalViewStateLiveData = MutableLiveData<ViewState>()
    private val dataRepository: LmaoNinjaApiDataRepository by kodein.instance()
    private val baseItems = mutableListOf<SelectableItem>()
    private val noneItem = SelectableItem(-1, "None")

    fun start() {
        viewModelScope.launch {
            val timeLineDataSetsResult = dataRepository.getTimeLineDataSets()

            if (timeLineDataSetsResult is ApiResult.Success) {
                val timeLineDataSets = timeLineDataSetsResult.value as List<TimeLineDataSet>
                val selectableItems = timeLineDataSets.map { it.toSelectableItem() }

                baseItems.apply {
                    clear()
                    add(noneItem)
                    addAll(selectableItems)
                }
            }

            onNewSearchQuery("")
        }
    }

    fun onItemSelected(id: Int) {
        internalViewStateLiveData.value = ViewState.FinishAsOk(id)
    }

    fun onBackClick() {
        internalViewStateLiveData.value = ViewState.FinishAsCanceled
    }

    fun onClearSearch() = onNewSearchQuery("")

    fun onNewSearchQuery(newText: String?) {
        internalViewStateLiveData.value = ViewState.UpdateItems(
            baseItems.filter {
                it.label.toLowerCase(Locale.ROOT).contains(newText ?: "")
            }
        )
    }

    private fun TimeLineDataSet.toSelectableItem() = SelectableItem(id, locationName)
}


sealed class ViewState {
    class UpdateItems(val items: List<SelectableItem>) : ViewState()
    class FinishAsOk(val selectItemId: Int) : ViewState()
    object FinishAsCanceled : ViewState()
}
