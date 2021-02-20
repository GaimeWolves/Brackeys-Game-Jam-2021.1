package com.gamewolves.bgj2021.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.viewport.FitViewport
import com.gamewolves.bgj2021.Main
import com.gamewolves.bgj2021.assets.*
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import ktx.actors.plusAssign
import ktx.actors.then
import ktx.async.KtxAsync
import ktx.collections.gdxArrayOf
import ktx.log.logger
import ktx.scene2d.actors
import ktx.scene2d.label
import ktx.scene2d.table

private val log = logger<LoadingScreen>()

class LoadingScreen(
        private val main: Main
) : Screen(main) {
    private val uiViewport = FitViewport(960f, 540f)

    private val stage by lazy { Stage(uiViewport, batch).apply { Gdx.input.inputProcessor = this } }

    private var isShowing = true
    private var fadeTime = 0f

    override fun show() {
        val assetRefs = gdxArrayOf(
                TextureAtlasAsset.values().filter { !it.isSkinAtlas }.map { assetStorage.loadAsync(it.descriptor) },
                TextureAsset.values().map { assetStorage.loadAsync(it.descriptor) },
                SoundAsset.values().map { assetStorage.loadAsync(it.descriptor) },
                MusicAsset.values().map { assetStorage.loadAsync(it.descriptor) },
                ShaderProgramAsset.values().map { assetStorage.loadAsync(it.descriptor) },
                TiledMapAssets.values().map { assetStorage.loadAsync(it.descriptor) },
                (0 until TiledMapAssets.getLevelCount()).map { assetStorage.loadAsync(TiledMapAssets.getLevelById(it)) }
        ).flatten()

        KtxAsync.launch {
            assetRefs.joinAll()
            assetsLoaded()
        }

        setupUI()
        stage += Actions.alpha(0f) then Actions.fadeIn(0.5f)
    }

    override fun resize(width: Int, height: Int) {
        uiViewport.update(width, height, true)
    }

    override fun render(delta: Float) {
        if (assetStorage.progress.isFinished && main.containsScreen<MenuScreen>()) {
            main.removeScreen(LoadingScreen::class.java)
            dispose()
            main.setScreen<MenuScreen>()
        }

        if (isShowing) {
            fadeTime += delta
            batch.color = Color.BLACK.cpy().lerp(1f, 1f, 1f, 1f, fadeTime)

            if (fadeTime > 1) {
                batch.color = Color.WHITE.cpy()
                fadeTime = 0f
                isShowing = false
            }
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

    private fun assetsLoaded() {
        main.addScreen(MenuScreen(main))
    }

    private fun setupUI() {
        stage.actors {
            table {
                defaults().fillX().expandX().fillY()

                label("Snek gem") { cell ->
                    setAlignment(Align.center)
                    cell.apply {
                        padBottom(5f)
                    }
                }
                row()

                label("loding,.,") {
                    setAlignment(Align.center)
                }

                center()
                setFillParent(true)
                pack()
            }
        }
    }
}