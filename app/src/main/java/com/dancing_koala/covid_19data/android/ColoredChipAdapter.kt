package com.dancing_koala.covid_19data.android

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dancing_koala.covid_19data.R
import com.dancing_koala.covid_19data.core.Color
import com.google.android.material.chip.Chip


class ColoredChipAdapter : RecyclerView.Adapter<ColoredChipAdapter.Holder>() {

    private val data = mutableListOf<ColoredChipItem>()

    var callback: Callback? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val holder = Holder(LayoutInflater.from(parent.context).inflate(R.layout.item_dataviz_subject, parent, false))
        holder.chip.setOnCloseIconClickListener {
            callback?.onChipCloseClick(coloredChipItem = data[holder.currentPosition])
        }
        return holder
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val item = data[position]

        holder.chip.chipBackgroundColor = ColorStateList.valueOf(item.backgroundColor.intValue)
        holder.chip.text = item.label
        holder.currentPosition = position
    }

    fun addChip(coloredChipItem: ColoredChipItem) {
        data.add(coloredChipItem)
        notifyItemInserted(data.lastIndex)
    }

    fun removeChip(coloredChipItem: ColoredChipItem) {
        val itemIndex = data.indexOf(coloredChipItem)
        data.remove(coloredChipItem)
        notifyItemRemoved(itemIndex)
    }

    class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val chip = itemView as Chip
        var currentPosition = RecyclerView.NO_POSITION
    }

    interface Callback {
        fun onChipCloseClick(coloredChipItem: ColoredChipItem)
    }
}

data class ColoredChipItem(val id: Int, val label: String, val backgroundColor: Color)