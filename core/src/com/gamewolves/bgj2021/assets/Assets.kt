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
        directory: String = "sound",
        val descriptor: AssetDescriptor<Sound> = AssetDescriptor("$directory/$fileName", Sound::class.java)
) {
}

enum class MusicAsset(
        fileName: String,
        directory: String = "music",
        val descriptor: AssetDescriptor<Music> = AssetDescriptor("$directory/$fileName", Music::class.java)
) {
}

enum class TextureAtlasAsset(
        val isSkinAtlas: Boolean,
        fileName: String,
        directory: String = "graphics",
        val descriptor: AssetDescriptor<TextureAtlas> = AssetDescriptor("$directory/$fileName", TextureAtlas::class.java)
) {
    SNAKE(false, "snake.atlas"),
}

enum class TextureAsset(
        fileName: String,
        directory: String = "graphics",
        val descriptor: AssetDescriptor<Texture> = AssetDescriptor("$directory/$fileName", Texture::class.java)
) {
    BACKGROUND("background.png"),
    GRID_OVERLAY("grid_overlay.png")
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
    TEST_MAP("level_test.tmx");

    companion object {
        private var levelCount = -1

        fun getLevelById(id: Int): AssetDescriptor<TiledMap> {
            return AssetDescriptor(
                    "levels/level$id.tmx",
                    TiledMap::class.java
            )
        }

        fun getLevelCount(): Int {
            if (levelCount == -1) {
                val levelFolderHandle = Gdx.files.internal("levels")
                levelCount = levelFolderHandle.list(".tmx").size - 1
            }

            return levelCount // Don't count test level
        }
    }
}
