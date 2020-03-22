package com.dancing_koala.covid_19data

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_item_selection.*
import java.io.Serializable

class ItemSelectionActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_ITEMS = "ItemSelectionActivity.extra.items"
        const val EXTRA_ITEM_SELECTED = "ItemSelectionActivity.extra.selecte_item"
    }

    private val noneItem = SelectableItem(-1, "None")

    private lateinit var items: List<SelectableItem>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_selection)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Country selection"
        }

        if (intent?.extras == null) {
            finish()
            return
        }

        val serializableSelectableItems = intent?.extras?.getSerializable(EXTRA_ITEMS)

        items = mutableListOf(noneItem).apply {
            if (serializableSelectableItems is List<*> && serializableSelectableItems.first() is SelectableItem) {
                addAll(serializableSelectableItems as List<SelectableItem>)
            }
        }

        itemsRecyclerView.apply {
            adapter = SelectableItemsAdapter(items) { position ->
                setResultAndFinish(position)
            }
            layoutManager = LinearLayoutManager(context)
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    private fun setResultAndFinish(position: Int) {
        setResult(Activity.RESULT_OK, Intent().putExtra(EXTRA_ITEM_SELECTED, items[position].id))
        finish()
    }


    private class SelectableItemsAdapter(
        private val items: List<SelectableItem>,
        private val selectionCallback: (Int) -> Unit
    ) : RecyclerView.Adapter<SelectableItemsAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val textView = LayoutInflater.from(parent.context).inflate(R.layout.item_selection_item, parent, false) as TextView
            textView.isClickable = true
            textView.isFocusable = true

            val holder = ViewHolder(textView)

            textView.setOnClickListener { onItemClicked(holder.adapterPosition) }

            return holder
        }

        override fun getItemCount(): Int = items.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.itemTextView.text = items[position].label
        }

        private fun onItemClicked(position: Int) = selectionCallback.invoke(position)

        class ViewHolder(val itemTextView: TextView) : RecyclerView.ViewHolder(itemTextView)
    }

    data class SelectableItem(val id: Int, val label: String) : Serializable
}