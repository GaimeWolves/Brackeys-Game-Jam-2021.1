package com.gamewolves.bgj2021.screens

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.gamewolves.bgj2021.Main
import ktx.app.KtxScreen
import ktx.log.logger

private val log = logger<GameScreen>()

class GameScreen(private val main: Main,
                 private val batch: Batch,
                 private val shapeRenderer: ShapeRenderer,
                 private val font: BitmapFont) : KtxScreen {
    private val camera = OrthographicCamera().apply { setToOrtho(false, 800f, 480f) }

    override fun render(delta: Float) {
        camera.update()

        main.batch.begin()
        main.font.draw(main.batch, "Hallo World!", 10f, 10f)
        main.batch.end()
    }
}