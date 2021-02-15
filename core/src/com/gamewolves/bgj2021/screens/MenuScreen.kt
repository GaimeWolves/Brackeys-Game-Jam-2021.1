package com.gamewolves.bgj2021.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.gamewolves.bgj2021.Main
import ktx.app.KtxScreen
import ktx.graphics.use

class MenuScreen(private val main: Main,
                 private val batch: Batch,
                 private val shapeRenderer: ShapeRenderer,
                 private val font: BitmapFont) : KtxScreen {
    private val camera = OrthographicCamera().apply { setToOrtho(false, 800f, 480f) }

    override fun render(delta: Float) {
        camera.update()
        batch.projectionMatrix = camera.combined

        batch.use { batch ->
            font.draw(batch, "Welcome to snek!!! ", 100f, 150f)
            font.draw(batch, "Tap anywhere to begin!", 100f, 100f)
        }

        if (Gdx.input.isTouched) {
            main.addScreen(GameScreen(main, batch, shapeRenderer, font))
            main.setScreen<GameScreen>()
            main.removeScreen<MenuScreen>()
            dispose()
        }
    }
}