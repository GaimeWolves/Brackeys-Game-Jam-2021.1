package com.gamewolves.bgj2021.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.actions.Actions.*
import com.badlogic.gdx.utils.viewport.FitViewport
import com.gamewolves.bgj2021.Main
import com.gamewolves.bgj2021.assets.SoundAsset
import com.gamewolves.bgj2021.assets.TextureAsset
import com.gamewolves.bgj2021.ui.ImageButtonSkin
import ktx.actors.*
import ktx.graphics.color
import ktx.graphics.use
import ktx.log.logger
import ktx.scene2d.actors
import ktx.scene2d.imageButton
import ktx.scene2d.table
import ktx.scene2d.textButton

private val log = logger<MenuScreen>()

class MenuScreen(
        private val main: Main
) : Screen(main) {
    private val uiViewport = FitViewport(960f, 540f)

    private val stage by lazy { Stage(uiViewport, batch).apply { Gdx.input.inputProcessor = this } }
    private val bg = assetStorage[TextureAsset.TITLE_BG.descriptor]
    private val selectSfx = assetStorage[SoundAsset.SELECT.descriptor]

    private var isShowing = true
    private var fadeTime = 0f

    override fun show() {
        setupUI()

        stage += alpha(0f) then fadeIn(0.5f)
    }

    override fun resize(width: Int, height: Int) {
        uiViewport.update(width, height, true)
    }

    override fun render(delta: Float) {
        if (isShowing) {
            fadeTime += delta * 2f
            batch.color = Color.BLACK.cpy().lerp(1f, 1f, 1f, 1f, fadeTime)

            if (fadeTime > 1) {
                batch.color = Color.WHITE.cpy()
                fadeTime = 0f
                isShowing = false
            }
        }

        batch.use(uiViewport.camera.projection) {
            batch.draw(bg, -uiViewport.worldWidth * 0.5f, -uiViewport.worldHeight * 0.5f, uiViewport.worldWidth, uiViewport.worldHeight)
        }

        stage.run {
            uiViewport.apply()
            act()
            draw()
        }
    }

    override fun dispose() {
        stage.dispose()
    }

    private fun setupUI() {
        stage.actors {
            table {
                defaults().fillX().expandX().fillY().padTop(100f).padLeft(100f)

                imageButton(ImageButtonSkin.PLAY.name) { cell ->
                    cell.width(100f).height(100f)
                    color = color(0.3f, 0.3f, 0.3f, 1f)
                    onClick {
                        selectSfx.play(0.25f)
                        stage += alpha(1f) + fadeOut(0.5f) + Actions.run {
                            main.removeScreen<MenuScreen>()
                            dispose()
                            main.addScreen(LevelSelectScreen(main))
                            main.setScreen<LevelSelectScreen>()
                        }
                    }
                }

                imageButton(ImageButtonSkin.EXIT.name) { cell ->
                    cell.width(100f).height(100f)
                    cell.padRight(uiViewport.worldWidth * 0.1f)

                    color = color(0.3f, 0.3f, 0.3f, 1f)

                    onClick {
                        selectSfx.play(0.25f)
                        stage += alpha(1f) + fadeOut(0.5f) + Actions.run {
                            Gdx.app.exit()
                        }
                    }
                }

                center()
                padLeft(uiViewport.worldWidth / 2f)
                setFillParent(true)
                pack()
            }
        }
    }
}