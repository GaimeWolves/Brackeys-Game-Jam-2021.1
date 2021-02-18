package com.gamewolves.bgj2021

import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.gamewolves.bgj2021.screens.GameScreen
import kotlinx.coroutines.launch
import ktx.app.KtxGame
import ktx.app.KtxScreen
import ktx.assets.async.AssetStorage
import ktx.async.KtxAsync
import ktx.inject.Context
import ktx.inject.register


class Main : KtxGame<KtxScreen>() {
    val batch by lazy { SpriteBatch() }
    val shapeRenderer by lazy { ShapeRenderer() }
    val font by lazy { BitmapFont() }
    val assetStorage by lazy { AssetStorage() }

    private val context = Context()

    override fun create() {
        assetStorage.setLoader(".tmx") { TmxMapLoader(InternalFileHandleResolver()) }

        KtxAsync.initiate()
        KtxAsync.launch {
            assetStorage.apply {
                load<TextureAtlas>("snake.atlas")

                context.register {
                    bindSingleton(batch)
                    bindSingleton(shapeRenderer)
                    bindSingleton(font)
                    bindSingleton(assetStorage)

                    addScreen(GameScreen(this@Main, inject(), inject(), inject(), inject()))
                }
                setScreen<GameScreen>()
            }
        }
        super.create()
    }

    override fun dispose() {
        context.dispose()
        super.dispose()
    }
}