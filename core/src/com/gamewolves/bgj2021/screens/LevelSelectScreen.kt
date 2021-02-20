package com.gamewolves.bgj2021.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.actions.Actions.*
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.viewport.FitViewport
import com.gamewolves.bgj2021.Main
import com.gamewolves.bgj2021.assets.TiledMapAssets
import com.gamewolves.bgj2021.ui.ImageTextButtonSkin
import com.gamewolves.bgj2021.ui.LabelSkin
import com.gamewolves.bgj2021.util.hasUnlockedLevel
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import ktx.actors.*
import ktx.async.KtxAsync
import ktx.graphics.color
import ktx.log.logger
import ktx.scene2d.*

private val log = logger<LevelSelectScreen>()

class LevelSelectScreen(
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
                defaults().fillX().expandX()

                label("Levels", LabelSkin.TITLE.name) { cell ->
                    setAlignment(Align.center)
                    cell.apply {
                        padBottom(5f)
                    }
                }

                row()

                table { cell ->
                    defaults().top().fillX()

                    for (i in 0 until 20) {
                        if (i % 5 == 0)
                            row()

                        if (i < TiledMapAssets.getLevelCount())
                            createLevelButton(i)
                        else
                            createDummyButton()
                    }

                    center()

                    cell.fillY().expandY()
                    pack()
                }

                padLeft(uiViewport.worldWidth * 0.1f).padRight(uiViewport.worldWidth * 0.1f)
                padTop(uiViewport.worldHeight * 0.2f).padBottom(uiViewport.worldHeight * 0.2f)
                center()
                setFillParent(true)
                pack()
            }
        }
    }

    private fun KTableWidget.createLevelButton(id: Int) {
        textButton((id + 1).toString(), ImageTextButtonSkin.LEVEL.name) { cell ->
            cell.pad(15f, 30f, 15f, 30f).width(25f).height(25f)

            if (!hasUnlockedLevel(id)) {
                touchable = Touchable.disabled
                color = color(0.5f, 0.5f, 0.5f, 0.5f)
            }

            onClick {
                stage += color(ktx.graphics.color(1f, 1f, 1f, 1f)) +
                        color(ktx.graphics.color(0f, 0f, 0f, 0f), 0.5f) +
                        Actions.run {
                    main.removeScreen<LevelSelectScreen>()
                    dispose()
                    main.addScreen(GameScreen(main, id))
                    main.setScreen<GameScreen>()
                }
            }
        }
    }

    private fun KTableWidget.createDummyButton() {
        textButton("", ImageTextButtonSkin.LEVEL.name) { cell ->
            cell.pad(15f, 30f, 15f, 30f).width(25f).height(25f)
        }
    }
}