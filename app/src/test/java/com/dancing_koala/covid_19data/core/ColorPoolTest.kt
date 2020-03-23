package com.dancing_koala.covid_19data.core

import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ColorPoolTest {

    companion object {
        const val COLOR_HEX_1 = "#FF0000"
        const val COLOR_HEX_2 = "#00FF00"
    }

    @Test
    fun takeColor() {
        val colorPool = ColorPool(listOf(COLOR_HEX_1, COLOR_HEX_2).reversed())

        val color1 = colorPool.takeColor()
        assertNotNull(color1)
        assertEquals(COLOR_HEX_1, color1!!.hexValue)

        val color2 = colorPool.takeColor()
        assertNotNull(color2)
        assertEquals(COLOR_HEX_2, color2!!.hexValue)

        val nullColor = colorPool.takeColor()
        assertNull(nullColor)
    }

    @Test
    fun recycleColor_recycleValidColor() {
        val colorPool = ColorPool(listOf(COLOR_HEX_1).reversed())

        val color = colorPool.takeColor()
        assertNotNull(color)
        assertEquals(COLOR_HEX_1, color!!.hexValue)

        val nullColor = colorPool.takeColor()
        assertNull(nullColor)

        colorPool.recycleColor(color)

        val recycledColor = colorPool.takeColor()
        assertNotNull(recycledColor)
        assertEquals(color, recycledColor)
    }

    @Test
    fun recycleColor_recycleInvalidColor() {
        val colorPool = ColorPool(listOf(COLOR_HEX_1).reversed())

        val color = colorPool.takeColor()
        assertNotNull(color)
        assertEquals(COLOR_HEX_1, color!!.hexValue)

        val nullColor = colorPool.takeColor()
        assertNull(nullColor)

        val invalidColor = Color(COLOR_HEX_2)


        colorPool.recycleColor(invalidColor)

        val nullRecycledColor = colorPool.takeColor()
        assertNull(nullRecycledColor)
    }
}