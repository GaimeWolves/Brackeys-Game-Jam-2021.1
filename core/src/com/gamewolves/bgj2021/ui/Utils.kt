package com.gamewolves.bgj2021.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.utils.GdxRuntimeException
import ktx.graphics.color
import kotlin.math.abs
import kotlin.math.floor

fun getColoredTexture(color: Color): Texture {

    val pixmap: Pixmap = try {
        Pixmap(9, 9, Pixmap.Format.RGBA8888)
    } catch (e: GdxRuntimeException) {
        Pixmap(9, 9, Pixmap.Format.RGB565)
    }

    pixmap.setColor(color)
    pixmap.drawRectangle(0, 0, 9, 9)

    return Texture(pixmap)
}

fun colorFromHSL(h: Float, s: Float, l: Float): Color {
    var h = h
    var r: Float
    var g: Float
    var b: Float
    h %= 1f
    val region = h * 360 / 60f
    val c = (1 - abs(2 * l - 1)) * s
    val x = c * (1 - abs(region % 2 - 1))
    when (floor(region.toDouble()).toInt()) {
        0 -> {
            r = c
            g = x
            b = 0f
        }
        1 -> {
            r = x
            g = c
            b = 0f
        }
        2 -> {
            r = 0f
            g = c
            b = x
        }
        3 -> {
            r = 0f
            g = x
            b = c
        }
        4 -> {
            r = x
            g = 0f
            b = c
        }
        5 -> {
            r = c
            g = 0f
            b = x
        }
        else -> {
            r = 0f
            g = 0f
            b = 0f
        }
    }
    val m = l - c / 2f
    r += m
    g += m
    b += m
    return color(r, g, b)
}