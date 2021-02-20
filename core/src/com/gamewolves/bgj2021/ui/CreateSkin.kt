package com.gamewolves.bgj2021.ui

import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.ScreenUtils
import com.gamewolves.bgj2021.assets.FreeTypeFontAssets
import ktx.assets.async.AssetStorage
import ktx.freetype.generateFont
import ktx.graphics.color
import ktx.scene2d.Scene2DSkin
import ktx.style.imageTextButton
import ktx.style.label
import ktx.style.skin
import ktx.style.textButton

val fonts = HashMap<String, BitmapFont>()

enum class ImageTextButtonSkin {
    LEVEL
}

enum class LabelSkin {
    TITLE, MAP_TEXT
}

enum class Fonts {
    DEFAULT,
    TITLE,
    LEVEL,
    MAP_TEXT
}

fun createSkin(assetStorage: AssetStorage) {
    createFonts(assetStorage)

    Scene2DSkin.defaultSkin = skin {
        createTextButtonSkins()
        createLabelSkins()
    }
}

private fun Skin.createTextButtonSkins() {
    textButton("default") {
        font = fonts[Fonts.DEFAULT.name]
    }
    textButton(ImageTextButtonSkin.LEVEL.name) {
        font = fonts[Fonts.LEVEL.name]
    }
}

private fun Skin.createLabelSkins() {
    label("default") {
        font = fonts[Fonts.DEFAULT.name]
    }
    label(LabelSkin.TITLE.name) {
        font = fonts[Fonts.TITLE.name]
    }
    label(LabelSkin.MAP_TEXT.name) {
        font = fonts[Fonts.MAP_TEXT.name]
    }
}

private fun createFonts(assetStorage: AssetStorage) {
    val generator = assetStorage[FreeTypeFontAssets.FONT.descriptor]

    fonts[Fonts.DEFAULT.name] = generator.generateFont {
        minFilter = Texture.TextureFilter.Linear
        magFilter = Texture.TextureFilter.Linear
        size = 20
        spaceX = 1
        padLeft = 0
        padRight = 0
    }

    fonts[Fonts.LEVEL.name] = generator.generateFont {
        minFilter = Texture.TextureFilter.Linear
        magFilter = Texture.TextureFilter.Linear
        size = 30
        spaceX = 1
        padLeft = 0
        padRight = 0
    }

    fonts[Fonts.MAP_TEXT.name] = generator.generateFont {
        minFilter = Texture.TextureFilter.Linear
        magFilter = Texture.TextureFilter.Linear
        size = 40
        spaceX = 1
        padLeft = 0
        padRight = 0
    }

    fonts[Fonts.TITLE.name] = generator.generateFont {
        minFilter = Texture.TextureFilter.Linear
        magFilter = Texture.TextureFilter.Linear
        size = 40
        spaceX = 1
        padLeft = 0
        padRight = 0
        color = color(204f / 255f, 188f / 255f, 14f / 255f)
    }
}