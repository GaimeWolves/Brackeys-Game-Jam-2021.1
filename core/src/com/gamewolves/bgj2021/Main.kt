package com.gamewolves.bgj2021

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.gamewolves.bgj2021.screens.MenuScreen
import ktx.app.KtxGame
import ktx.app.KtxScreen
import ktx.inject.Context
import ktx.inject.register

class Main : KtxGame<KtxScreen>() {
    val batch by lazy { SpriteBatch() }
    val shapeRenderer by lazy { ShapeRenderer() }
    val font by lazy { BitmapFont() }

    private val context = Context()

    override fun create() {
        context.register {
            bindSingleton(batch)
            bindSingleton(shapeRenderer)
            bindSingleton(font)

            addScreen(MenuScreen(this@Main, inject(), inject(), inject()))
        }
        setScreen<MenuScreen>()
        super.create()
    }

    override fun dispose() {
        context.dispose()
        super.dispose()
    }
}