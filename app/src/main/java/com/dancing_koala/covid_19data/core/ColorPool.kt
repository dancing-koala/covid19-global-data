package com.dancing_koala.covid_19data.core

import java.util.*
import android.graphics.Color as SystemColor

class ColorPool(hexColors: List<String>) {

    private val availableColors = Stack<Color>().apply {
        addAll(hexColors.map { Color(it) })
    }

    private val usedColors = mutableListOf<Color>()

    val isEmpty: Boolean
        get() = availableColors.isEmpty()

    fun takeColor(): Color? =
        if (availableColors.isNotEmpty()) {
            availableColors.pop()?.apply { usedColors.add(this) }
        } else null


    fun recycleColor(color: Color) {
        if (usedColors.contains(color)) {
            usedColors.remove(color)
            availableColors.push(color)
        }
    }
}

data class Color(val hexValue: String) {
    val intValue = SystemColor.parseColor(hexValue)
}
