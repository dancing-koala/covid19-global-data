package com.dancing_koala.covid_19data.android

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dancing_koala.covid_19data.R
import com.google.android.material.chip.Chip


class SimpleChipAdapter : RecyclerView.Adapter<SimpleChipAdapter.Holder>() {

    private val data = mutableListOf<SimpleChipItem>()

    var callback: Callback? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val holder = Holder(LayoutInflater.from(parent.context).inflate(R.layout.item_dataviz_subject, parent, false))
        holder.chip.setOnCloseIconClickListener {
            callback?.onChipCloseClick(simpleChipItem = data[holder.currentPosition])
        }
        return holder
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.chip.text = data[position].label
        holder.currentPosition = position
    }

    fun addChip(simpleChipItem: SimpleChipItem) {
        data.add(simpleChipItem)
        notifyItemInserted(data.lastIndex)
    }

    fun removeChip(simpleChipItem: SimpleChipItem) {
        val itemIndex = data.indexOf(simpleChipItem)
        data.remove(simpleChipItem)
        notifyItemRemoved(itemIndex)
    }

    class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val chip = itemView as Chip
        var currentPosition = RecyclerView.NO_POSITION
    }

    interface Callback {
        fun onChipCloseClick(simpleChipItem: SimpleChipItem)
    }
}

data class SimpleChipItem(val id: Int, val label: String)