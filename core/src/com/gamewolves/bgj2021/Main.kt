package com.gamewolves.bgj2021

import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver
import com.badlogic.gdx.assets.loaders.resolvers.LocalFileHandleResolver
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.gamewolves.bgj2021.assets.*
import com.gamewolves.bgj2021.screens.GameScreen
import com.gamewolves.bgj2021.screens.LoadingScreen
import com.gamewolves.bgj2021.ui.createSkin
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import ktx.app.KtxGame
import ktx.app.KtxScreen
import ktx.assets.async.AssetStorage
import ktx.async.KtxAsync
import ktx.collections.gdxArrayOf
import ktx.freetype.async.loadFreeTypeFont
import ktx.freetype.async.registerFreeTypeFontLoaders


class Main : KtxGame<KtxScreen>() {
    val batch by lazy { SpriteBatch() }
    val shapeRenderer by lazy { ShapeRenderer() }
    val font by lazy { BitmapFont() }
    val assetStorage by lazy {
        AssetStorage(fileResolver = InternalFileHandleResolver()).apply {
            this.setLoader(".tmx") { TmxMapLoader(InternalFileHandleResolver()) }
            this.registerFreeTypeFontLoaders(replaceDefaultBitmapFontLoader = true)
        }
    }

    private lateinit var music: Music

    override fun create() {
        KtxAsync.initiate()

        val assets = gdxArrayOf(
                TextureAtlasAsset.values().filter { it.isSkinAtlas }.map { assetStorage.loadAsync(it.descriptor) },
                FreeTypeFontAssets.values().map { assetStorage.loadAsync(it.descriptor) },
                MusicAsset.values().map { assetStorage.loadAsync(it.descriptor) }
        ).flatten()

        KtxAsync.launch {
            assets.joinAll()
            createSkin(assetStorage)
            music = assetStorage[MusicAsset.MUSIC.descriptor]
            music.isLooping = true
            music.volume = 0.5f
            music.play()

            addScreen(LoadingScreen(this@Main))
            setScreen<LoadingScreen>()
        }
        super.create()
    }

    override fun dispose() {
        music.stop()

        super.dispose()
        assetStorage.dispose()
        batch.dispose()
        shapeRenderer.dispose()
    }
}