package com.gamewolves.bgj2021.ui

import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.ScreenUtils
import com.gamewolves.bgj2021.assets.FreeTypeFontAssets
import com.gamewolves.bgj2021.assets.TextureAtlasAsset
import ktx.assets.async.AssetStorage
import ktx.freetype.generateFont
import ktx.graphics.color
import ktx.scene2d.Scene2DSkin
import ktx.style.*

val fonts = HashMap<String, BitmapFont>()

enum class ImageTextButtonSkin {
    LEVEL
}

enum class ImageButtonSkin {
    PLAY,
    PAUSE,
    LEVEL_SELECT,
    EXIT,
    NEXT,
    RESTART
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

enum class SkinImage(val atlasKey: String) {
    LEVEL_BUTTON_UP("level_button_up"),
    LEVEL_BUTTON_DOWN("level_button_down"),
    PLAY_BUTTON("play"),
    LEVEL_SELECT_BUTTON("level_select"),
    PAUSE_BUTTON("pause"),
    EXIT_BUTTON("exit"),
    NEXT_BUTTON("next"),
    RESTART_BUTTON("restart")
}

fun createSkin(assetStorage: AssetStorage) {
    createFonts(assetStorage)

    Scene2DSkin.defaultSkin = skin(assetStorage[TextureAtlasAsset.UI.descriptor]) {
        createTextButtonSkins(this)
        createLabelSkins(this)
        createImageTextButtonSkins(this)
        createImageButtonSkins(this)
    }
}

private fun Skin.createTextButtonSkins(
        skin: Skin
) {
    textButton("default") {
        font = fonts[Fonts.DEFAULT.name]
    }
    textButton(ImageTextButtonSkin.LEVEL.name) {
        font = fonts[Fonts.LEVEL.name]
    }
}

private fun Skin.createImageTextButtonSkins(
        skin: Skin
) {
    imageTextButton(ImageTextButtonSkin.LEVEL.name) {
        font = fonts[Fonts.LEVEL.name]
        up = skin.getDrawable(SkinImage.LEVEL_BUTTON_UP.atlasKey)
        down = skin.getDrawable(SkinImage.LEVEL_BUTTON_DOWN.atlasKey)
    }
}

private fun Skin.createImageButtonSkins(
        skin: Skin
) {
    imageButton(ImageButtonSkin.PLAY.name) {
        up = skin.getDrawable(SkinImage.PLAY_BUTTON.atlasKey)
        down = up
    }
    imageButton(ImageButtonSkin.PAUSE.name) {
        up = skin.getDrawable(SkinImage.PAUSE_BUTTON.atlasKey)
        down = up
    }
    imageButton(ImageButtonSkin.LEVEL_SELECT.name) {
        up = skin.getDrawable(SkinImage.LEVEL_SELECT_BUTTON.atlasKey)
        down = up
    }
    imageButton(ImageButtonSkin.EXIT.name) {
        up = skin.getDrawable(SkinImage.EXIT_BUTTON.atlasKey)
        down = up
    }
    imageButton(ImageButtonSkin.NEXT.name) {
        up = skin.getDrawable(SkinImage.NEXT_BUTTON.atlasKey)
        down = up
    }
    imageButton(ImageButtonSkin.RESTART.name) {
        up = skin.getDrawable(SkinImage.RESTART_BUTTON.atlasKey)
        down = up
    }
}

private fun Skin.createLabelSkins(
        skin: Skin
) {
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