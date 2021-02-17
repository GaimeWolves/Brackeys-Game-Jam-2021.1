package com.gamewolves.bgj2021.screens

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.PooledEngine
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.FitViewport
import com.gamewolves.bgj2021.Main
import com.gamewolves.bgj2021.ecs.components.*
import com.gamewolves.bgj2021.ecs.components.Facing
import com.gamewolves.bgj2021.ecs.systems.*
import ktx.app.KtxScreen
import ktx.ashley.entity
import ktx.ashley.with
import ktx.assets.async.AssetStorage
import ktx.collections.*
import ktx.graphics.use
import ktx.log.logger
import java.util.*

private val log = logger<GameScreen>()

class GameScreen(private val main: Main,
                 private val assetStorage: AssetStorage,
                 private val batch: SpriteBatch,
                 private val shapeRenderer: ShapeRenderer,
                 private val font: BitmapFont) : KtxScreen {
    private val viewport = FitViewport(32f,18f)
    private val uiViewport = FitViewport(480f, 270f)
    private val stage = Stage(uiViewport, batch)

    private val doorSystem by lazy { DoorInputSystem(this@GameScreen) }
    private val buttonSystem by lazy { ButtonInputSystem(this@GameScreen) }
    private val snakeSystem by lazy { SnakeInputSystem(this@GameScreen) }

    private val engine by lazy { PooledEngine().apply {
        addSystem(snakeSystem)
        addSystem(buttonSystem)
        addSystem(doorSystem)
        addSystem(CableInputSystem(this@GameScreen))
        addSystem(WallRenderSystem(batch, viewport, shapeRenderer))
        addSystem(CableRenderSystem(batch, viewport, shapeRenderer))
        addSystem(DoorRenderSystem(batch, viewport, shapeRenderer))
        addSystem(ButtonRenderSystem(batch, viewport, shapeRenderer))
        addSystem(SnakeRenderSystem(batch, assetStorage["snake.atlas"], viewport, shapeRenderer))
    } }

    val moveHistory = Stack<Move>()
    val currentSnakes = arrayListOf<Entity>(engine.entity {
        with<SnakeComponent> {
            parts += arrayListOf(
                    Vector2(27f, 9f),
                    Vector2(27f, 8f),
                    Vector2(27f, 7f),
                    Vector2(27f, 6f),
                    Vector2(27f, 5f),
                    Vector2(27f, 4f))
            snakeType = SnakeType.SECOND
        }
    }, engine.entity {
        with<SnakeComponent> {
            parts += arrayListOf(
                    Vector2(5f, 9f),
                    Vector2(5f, 8f),
                    Vector2(5f, 7f),
                    Vector2(5f, 6f),
                    Vector2(5f, 5f),
                    Vector2(5f, 4f))
            snakeType = SnakeType.FIRST
        }
    })

    val walls = arrayListOf(
            engine.entity { with<WallComponent> { position.set(8f, 10f) }},
            engine.entity { with<WallComponent> { position.set(9f, 10f) }},
            engine.entity { with<WallComponent> { position.set(11f, 10f) }},
            engine.entity { with<WallComponent> { position.set(12f, 10f) }}
    )

    val doors = arrayListOf(
            engine.entity {
                with<DoorComponent> {
                    position.set(10f, 10f)
                    facing = Facing.SOUTH
                    open = true
                }
            }
    )

    val buttons = arrayListOf(
            engine.entity {
                with<ButtonComponent> {
                    position.set(10f, 6f)
                }
            }
    )

    val cables = arrayListOf(
            engine.entity {
                with<CableComponent> {
                    position.set(10f, 7f)
                }
            },
            engine.entity {
                with<CableComponent> {
                    position.set(10f, 8f)
                }
            },
            engine.entity {
                with<CableComponent> {
                    position.set(10f, 9f)
                }
            }
    )

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height, true)
    }

    override fun render(delta: Float) {
        engine.update(delta)

        if (Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE))
            revertHistory()
    }

    private fun revertHistory() {
        if (moveHistory.empty())
            return

        when (val move = moveHistory.pop()) {
            is Move.SnakeMove -> {
                snakeSystem.revertSnake(move)
            }
            is Move.Recombination -> {
                snakeSystem.revertSnake(move)
                revertHistory()
            }
            is Move.Separation -> {
                snakeSystem.revertSnake(move)
                revertHistory()
            }
            is Move.ButtonChanged -> {
                move.button.pressed = move.pressed
                revertHistory()
            }
            is Move.DoorChanged -> {
                move.door.open = move.open
                revertHistory()
            }
        }
    }
}

sealed class Move {
    class SnakeMove(var snake: SnakeComponent, var position: Vector2, val reversed: Boolean): Move()
    class Recombination(var oldFirstSnake: Array<Vector2>, var oldSecondSnake: Array<Vector2>): Move()
    class Separation(var oldDoubleSnake: Array<Vector2>): Move()
    class ButtonChanged(val button: ButtonComponent, val pressed: Boolean): Move()
    class DoorChanged(val door: DoorComponent, val open: Boolean): Move()
}