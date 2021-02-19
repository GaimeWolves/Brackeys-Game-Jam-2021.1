package com.gamewolves.bgj2021.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.actions.Actions.*
import com.badlogic.gdx.utils.viewport.FitViewport
import com.gamewolves.bgj2021.Main
import ktx.actors.*
import ktx.log.logger
import ktx.scene2d.actors
import ktx.scene2d.table
import ktx.scene2d.textButton

private val log = logger<MenuScreen>()

class MenuScreen(
        private val main: Main
) : Screen(main) {
    private val uiViewport = FitViewport(960f, 540f)

    private val stage by lazy { Stage(uiViewport, batch).apply { Gdx.input.inputProcessor = this } }

    override fun show() {
        setupUI()
    }

    override fun resize(width: Int, height: Int) {
        uiViewport.update(width, height, true)
    }

    override fun render(delta: Float) {
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
                defaults().fillX().expandX().fillY()

                textButton("pley") {
                    onClick {
                        stage += alpha(1f) + fadeOut(0.5f) + Actions.run {
                            main.removeScreen<MenuScreen>()
                            dispose()
                            main.addScreen(LevelSelectScreen(main))
                            main.setScreen<LevelSelectScreen>()
                        }
                    }
                }

                textButton("êxîtt") { cell ->
                    cell.padRight(uiViewport.worldWidth * 0.1f)

                    onClick {
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