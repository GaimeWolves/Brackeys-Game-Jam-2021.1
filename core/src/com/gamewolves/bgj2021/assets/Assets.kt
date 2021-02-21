package com.gamewolves.bgj2021.assets

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.AssetDescriptor
import com.badlogic.gdx.assets.loaders.BitmapFontLoader
import com.badlogic.gdx.assets.loaders.ShaderProgramLoader
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TmxMapLoader

enum class SoundAsset(
        fileName: String,
        directory: String = "audio",
        val descriptor: AssetDescriptor<Sound> = AssetDescriptor("$directory/$fileName", Sound::class.java)
) {
    BATTERY_CHARGED("battery_charge.wav"),
    BUTTON_DOWN("button_down.wav"),
    BUTTON_UP("button_up.wav"),
    MOVE("move.wav"),
    SELECT("select.wav"),
    SNAKE_DIED("snakedie.wav"),
    PLAYER_WON("win.wav")
}

enum class MusicAsset(
        fileName: String,
        directory: String = "audio",
        val descriptor: AssetDescriptor<Music> = AssetDescriptor("$directory/$fileName", Music::class.java)
) {
    MUSIC("bg_music.ogg")
}

enum class TextureAtlasAsset(
        val isSkinAtlas: Boolean,
        fileName: String,
        directory: String = "graphics",
        val descriptor: AssetDescriptor<TextureAtlas> = AssetDescriptor("$directory/$fileName", TextureAtlas::class.java)
) {
    SNAKE(false, "snake.atlas"),
    UI(true, "ui.atlas")
}

enum class TextureAsset(
        fileName: String,
        directory: String = "graphics",
        val descriptor: AssetDescriptor<Texture> = AssetDescriptor("$directory/$fileName", Texture::class.java)
) {
    BACKGROUND("background.png"),
    LEVEL_SELECT_BG("level_select_bg.png"),
    TITLE_BG("title_bg.png")
}

enum class ShaderProgramAsset(
        shaderName: String,
        directory: String = "shaders",
        val descriptor: AssetDescriptor<ShaderProgram> = AssetDescriptor(
                "$directory/$shaderName",
                ShaderProgram::class.java,
                ShaderProgramLoader.ShaderProgramParameter().apply {
                    vertexFile = "$directory/$shaderName.vert"
                    fragmentFile = "$directory/$shaderName.frag"
                })
) {
    BLUR("blur");

    companion object {
        val DEFAULT by lazy { SpriteBatch.createDefaultShader() }
    }
}

enum class FreeTypeFontAssets(
        fileName: String,
        directory: String = "fonts",
        val descriptor: AssetDescriptor<FreeTypeFontGenerator> = AssetDescriptor(
                "$directory/$fileName",
                FreeTypeFontGenerator::class.java)
) {
    FONT("main.ttf")
}

enum class TiledMapAssets(
        fileName: String,
        directory: String = "levels",
        val descriptor: AssetDescriptor<TiledMap> = AssetDescriptor(
                "$directory/$fileName",
                TiledMap::class.java
        )
) {
    TEST_MAP("level_test.tmx"),
    LEVEL1("level0.tmx"),
    LEVEL2("level1.tmx"),
    LEVEL3("level2.tmx"),
    LEVEL4("level3.tmx"),
    LEVEL5("level4.tmx"),
    LEVEL6("level5.tmx"),
    LEVEL7("level6.tmx"),
    LEVEL8("level7.tmx"),
    LEVEL9("level8.tmx"),
    LEVEL10("level9.tmx");

    companion object {
        fun getLevelById(id: Int): AssetDescriptor<TiledMap> {
            return AssetDescriptor(
                    "levels/level$id.tmx",
                    TiledMap::class.java
            )
        }

        fun getLevelCount(): Int {
            return values().size - 1
        }
    }
}
