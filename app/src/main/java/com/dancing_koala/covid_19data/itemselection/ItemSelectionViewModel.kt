package com.dancing_koala.covid_19data.itemselection

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.util.*

class ItemSelectionViewModel(application: Application) : AndroidViewModel(application) {

    private val noneItem = SelectableItem(-1, "None")

    val viewStateLiveData: LiveData<ViewState>
        get() = internalViewStateLiveData

    private val internalViewStateLiveData = MutableLiveData<ViewState>()

    private lateinit var baseItems: List<SelectableItem>

    fun start(items: List<SelectableItem>) {
        baseItems =
            if (items.isNotEmpty() && items.first() != noneItem) {
                mutableListOf(noneItem).apply { addAll(items) }
            } else {
                items
            }

        onNewSearchQuery("")

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
            baseItems.toList().filter {
                it.label
                    .toLowerCase(Locale.ROOT)
                    .contains(newText ?: "")
            }
        )
    }
}


sealed class ViewState {
    class UpdateItems(val items: List<SelectableItem>) : ViewState()
    class FinishAsOk(val selectItemId: Int) : ViewState()
    object FinishAsCanceled : ViewState()
}
