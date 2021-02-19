package com.gamewolves.bgj2021.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.viewport.Viewport
import com.gamewolves.bgj2021.ecs.components.SnakeComponent
import ktx.ashley.allOf
import ktx.ashley.get
import ktx.collections.iterate
import ktx.graphics.use
import ktx.math.component1
import ktx.math.component2
import kotlin.math.sign

class SnakeRenderSystem(
        private val batch: SpriteBatch,
        private val snakeAtlas: TextureAtlas,
        private val viewport: Viewport,
        private val shapeRenderer: ShapeRenderer
) : IteratingSystem(
        allOf(SnakeComponent::class).get()
) {
    private val headTexture = snakeAtlas.findRegion("head")
    private val tailTexture = snakeAtlas.findRegion("tail")
    private val lineTexture = snakeAtlas.findRegion("line")
    private val cornerTexture = snakeAtlas.findRegion("corner")

    override fun update(deltaTime: Float) {
        batch.use(viewport.camera.combined) {
            super.update(deltaTime)
        }
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val snake = entity[SnakeComponent.mapper]
        require(snake != null) { "Entity $entity must have a SnakeComponent." }

        snake.parts.forEachIndexed{ idx, position -> drawTile(snake, idx, position) }
    }

    private fun drawTile(snake: SnakeComponent, idx: Int, position: Vector2) {
        val (x, y) = position

        when (idx) {
            0 -> {
                val rotation = when (snake.parts.size) {
                    1 -> snake.lastDirection.rotation
                    else -> determineFacing(snake.parts[idx + 1], snake.parts[idx]).rotation
                }

                batch.draw(headTexture, x, y, 0.5f, 0.5f, 1f, 1f, 1f, 1f, rotation)
            }
            snake.parts.size - 1 -> {
                val texture = when(snake.snakeType) {
                    SnakeType.DOUBLE -> headTexture
                    else -> tailTexture
                }

                val rotation = determineFacing(snake.parts[idx - 1], snake.parts[idx]).rotation
                batch.draw(texture, x, y, 0.5f, 0.5f, 1f, 1f, 1f, 1f, rotation)
            }
            else -> {
                val facingNext = determineFacing(snake.parts[idx + 1], snake.parts[idx])
                val facingPrev = determineFacing(snake.parts[idx - 1], snake.parts[idx])

                if ((facingNext == Facing.WEST && facingPrev == Facing.EAST)
                        || (facingNext == Facing.EAST && facingPrev == Facing.WEST)
                        || (facingNext == Facing.NORTH && facingPrev == Facing.SOUTH)
                        || (facingNext == Facing.SOUTH && facingPrev == Facing.NORTH)) {
                    val rotation = facingNext.rotation
                    batch.draw(lineTexture, x, y, 0.5f, 0.5f, 1f, 1f, 1f, 1f, rotation)
                } else {
                    val rotation = determineCornerTileRotation(facingNext, facingPrev)
                    batch.draw(cornerTexture, x, y, 0.5f, 0.5f, 1f, 1f, 1f, 1f, rotation)
                }
            }
        }
    }

    private fun determineFacing(anchor: Vector2, other: Vector2): Facing {
        return when {
            other.x < anchor.x -> Facing.WEST
            other.x > anchor.x -> Facing.EAST
            other.y < anchor.y -> Facing.SOUTH
            other.y > anchor.y -> Facing.NORTH
            else -> Facing.NONE
        }
    }

    private fun determineCornerTileRotation(facingNext: Facing, facingPrev: Facing): Float {
        return when {
            facingPrev == Facing.NORTH && facingNext == Facing.EAST -> 270f
            facingPrev == Facing.NORTH && facingNext == Facing.WEST -> 0f
            facingPrev == Facing.SOUTH && facingNext == Facing.EAST -> 180f
            facingPrev == Facing.SOUTH && facingNext == Facing.WEST -> 90f
            facingPrev == Facing.EAST && facingNext == Facing.NORTH -> 270f
            facingPrev == Facing.EAST && facingNext == Facing.SOUTH -> 180f
            facingPrev == Facing.WEST && facingNext == Facing.NORTH -> 0f
            facingPrev == Facing.WEST && facingNext == Facing.SOUTH -> 90f
            else -> 0f
        }
    }
}

enum class Facing(val rotation: Float) {
    NORTH(90f), SOUTH(270f), WEST(180f), EAST(0f), NONE(0f)
}