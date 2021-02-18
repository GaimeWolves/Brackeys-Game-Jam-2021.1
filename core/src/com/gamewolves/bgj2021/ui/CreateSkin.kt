package com.gamewolves.bgj2021.ui

import com.badlogic.gdx.graphics.g2d.BitmapFont
import ktx.scene2d.Scene2DSkin
import ktx.style.skin
import ktx.style.textButton

fun createSkin(normalFont: BitmapFont) {
    Scene2DSkin.defaultSkin = skin {
        textButton("default") {
            font = normalFont
        }
    }
}