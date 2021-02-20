package com.gamewolves.bgj2021.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.utils.GdxRuntimeException

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