package com.dancing_koala.covid_19data.itemselection

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dancing_koala.covid_19data.R


class SelectableItemAdapter(
    private val selectionCallback: (Int) -> Unit
) : RecyclerView.Adapter<SelectableItemAdapter.ViewHolder>() {

    private val items = mutableListOf<SelectableItem>()

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

    private fun onItemClicked(position: Int) = selectionCallback.invoke(items[position].id)

    fun updateItems(newItems: List<SelectableItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    class ViewHolder(val itemTextView: TextView) : RecyclerView.ViewHolder(itemTextView)
}