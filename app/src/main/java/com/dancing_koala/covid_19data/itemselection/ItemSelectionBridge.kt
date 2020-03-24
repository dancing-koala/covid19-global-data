package com.dancing_koala.covid_19data.itemselection

class ItemSelectionBridge {
    companion object {
        var items: List<SelectableItem>? = null

        fun consume(): List<SelectableItem>? {
            return items?.let { nonNullItems ->
                items = null
                nonNullItems
            }
        }
    }
}