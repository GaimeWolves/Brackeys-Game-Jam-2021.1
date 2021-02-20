package com.gamewolves.bgj2021.screens

import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.gamewolves.bgj2021.Main
import ktx.app.KtxScreen
import ktx.assets.async.AssetStorage

abstract class Screen(
        private val main: Main
): KtxScreen {
    val assetStorage: AssetStorage = main.assetStorage
    val batch: SpriteBatch = main.batch
    val shapeRenderer: ShapeRenderer = main.shapeRenderer
    val font: BitmapFont = main.font
}