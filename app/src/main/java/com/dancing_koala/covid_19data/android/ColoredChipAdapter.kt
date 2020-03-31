package com.dancing_koala.covid_19data.android

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dancing_koala.covid_19data.R
import com.dancing_koala.covid_19data.core.Color
import com.google.android.material.chip.Chip


class ColoredChipAdapter(var callback: Callback? = null) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    companion object {
        const val VIEW_TYPE_ADD_BUTTON = 0
        const val VIEW_TYPE_ITEM = 1
    }

    private val data = mutableListOf<ColoredChipItem>()

    override fun getItemViewType(position: Int): Int = when (position) {
        0    -> VIEW_TYPE_ADD_BUTTON
        else -> VIEW_TYPE_ITEM
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_ADD_BUTTON) {
            AddButtonHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_dataviz_add_button, parent, false)).apply {
                itemView.setOnClickListener { callback?.onAddButtonChipClick() }
            }
        } else {
            ItemHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_dataviz_subject, parent, false)).apply {
                chip.setOnCloseIconClickListener {
                    callback?.onChipCloseClick(coloredChipItem = data[currentPosition - 1])
                }
            }
        }
    }

    override fun getItemCount(): Int = data.size + 1

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == VIEW_TYPE_ITEM) {
            val itemHolder = holder as ItemHolder
            val item = data[position - 1]

            itemHolder.chip.chipBackgroundColor = ColorStateList.valueOf(item.backgroundColor.intValue)
            itemHolder.chip.text = item.label
            itemHolder.currentPosition = position
        }
    }

    fun updateChips(chipItems: List<ColoredChipItem>) {
        data.apply {
            clear()
            addAll(chipItems)
        }

        notifyDataSetChanged()
    }

    class AddButtonHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    class ItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val chip = itemView as Chip
        var currentPosition = RecyclerView.NO_POSITION
    }

    interface Callback {
        fun onAddButtonChipClick()
        fun onChipCloseClick(coloredChipItem: ColoredChipItem)
    }
}

data class ColoredChipItem(val id: Int, val label: String, val backgroundColor: Color)