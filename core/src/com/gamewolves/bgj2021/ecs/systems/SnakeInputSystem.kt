package com.gamewolves.bgj2021.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.math.Vector2
import com.gamewolves.bgj2021.ecs.components.DoorComponent
import com.gamewolves.bgj2021.ecs.components.Facing
import com.gamewolves.bgj2021.ecs.components.SnakeComponent
import com.gamewolves.bgj2021.ecs.components.WallComponent
import com.gamewolves.bgj2021.screens.GameScreen
import com.gamewolves.bgj2021.screens.Move
import ktx.ashley.allOf
import ktx.ashley.entity
import ktx.ashley.get
import ktx.ashley.with
import ktx.math.vec2

class SnakeInputSystem(
        private val game: GameScreen
) : IteratingSystem(
        allOf(SnakeComponent::class).get()
) {
    override fun processEntity(entity: Entity, deltaTime: Float) {
        val snake = entity[SnakeComponent.mapper]
        require(snake != null) { "Entity $entity must have a SnakeComponent.." }

        when (snake.snakeType) {
            SnakeType.FIRST -> {
                when {
                    Gdx.input.isKeyJustPressed(Input.Keys.W) -> moveSnake(snake, Direction.UP, false)
                    Gdx.input.isKeyJustPressed(Input.Keys.S) -> moveSnake(snake, Direction.DOWN, false)
                    Gdx.input.isKeyJustPressed(Input.Keys.A) -> moveSnake(snake, Direction.LEFT, false)
                    Gdx.input.isKeyJustPressed(Input.Keys.D) -> moveSnake(snake, Direction.RIGHT, false)
                }
            }
            SnakeType.SECOND -> {
                when {
                    Gdx.input.isKeyJustPressed(Input.Keys.UP) -> moveSnake(snake, Direction.UP, false)
                    Gdx.input.isKeyJustPressed(Input.Keys.DOWN) -> moveSnake(snake, Direction.DOWN, false)
                    Gdx.input.isKeyJustPressed(Input.Keys.LEFT) -> moveSnake(snake, Direction.LEFT, false)
                    Gdx.input.isKeyJustPressed(Input.Keys.RIGHT) -> moveSnake(snake, Direction.RIGHT, false)
                }
            }
            SnakeType.DOUBLE -> {
                if (checkSplitting(entity, snake))
                    return

                when {
                    Gdx.input.isKeyJustPressed(Input.Keys.W) -> moveSnake(snake, Direction.UP, false)
                    Gdx.input.isKeyJustPressed(Input.Keys.S) -> moveSnake(snake, Direction.DOWN, false)
                    Gdx.input.isKeyJustPressed(Input.Keys.A) -> moveSnake(snake, Direction.LEFT, false)
                    Gdx.input.isKeyJustPressed(Input.Keys.D) -> moveSnake(snake, Direction.RIGHT, false)
                    Gdx.input.isKeyJustPressed(Input.Keys.UP) -> moveSnake(snake, Direction.UP, true)
                    Gdx.input.isKeyJustPressed(Input.Keys.DOWN) -> moveSnake(snake, Direction.DOWN, true)
                    Gdx.input.isKeyJustPressed(Input.Keys.LEFT) -> moveSnake(snake, Direction.LEFT, true)
                    Gdx.input.isKeyJustPressed(Input.Keys.RIGHT) -> moveSnake(snake, Direction.RIGHT, true)
                }
            }
        }
    }

    private fun moveSnake(snake: SnakeComponent, direction: Direction, reversed: Boolean) {
        var oldPosition = when (reversed) {
            true -> snake.parts.last().cpy()
            false -> snake.parts.first().cpy()
        }

        var newPosition = oldPosition.cpy()

        when(direction) {
            Direction.UP -> newPosition.y += 1f
            Direction.DOWN -> newPosition.y -= 1f
            Direction.LEFT -> newPosition.x -= 1f
            Direction.RIGHT -> newPosition.x += 1f
        }

        if (!checkCollisions(newPosition, oldPosition))
            return

        when (reversed) {
            true -> game.moveHistory.push(Move.SnakeMove(snake, snake.parts.first().cpy(), reversed))
            false -> game.moveHistory.push(Move.SnakeMove(snake, snake.parts.last().cpy(), reversed))
        }

        if (reversed) {
            snake.parts.asReversed().replaceAll { oldPosition ->
                val tmp = newPosition.cpy()
                newPosition = oldPosition
                tmp
            }
        }
        else {
            snake.parts.replaceAll { oldPosition ->
                val tmp = newPosition.cpy()
                newPosition = oldPosition
                tmp
            }
        }

        checkRecombination()
    }

    fun revertSnake(move: Move) {
        if (move is Move.SnakeMove) {
            if (move.reversed) {
                move.snake.parts.replaceAll { oldPosition ->
                    val tmp = move.position.cpy()
                    move.position = oldPosition
                    tmp
                }
            }
            else {
                move.snake.parts.asReversed().replaceAll { oldPosition ->
                    val tmp = move.position.cpy()
                    move.position = oldPosition
                    tmp
                }
            }
        } else if (move is Move.Recombination) {
            val oldSnake1 = engine.entity {
                with<SnakeComponent> {
                    parts += move.oldFirstSnake
                    snakeType = SnakeType.FIRST
                }
            }

            val oldSnake2 = engine.entity {
                with<SnakeComponent> {
                    parts += move.oldSecondSnake
                    snakeType = SnakeType.SECOND
                }
            }

            engine.removeEntity(game.currentSnakes.first())

            game.currentSnakes.clear()
            game.currentSnakes += oldSnake1
            game.currentSnakes += oldSnake2
        } else if (move is Move.Separation) {
            val oldSnake = engine.entity {
                with<SnakeComponent> {
                    parts += move.oldDoubleSnake
                    snakeType = SnakeType.DOUBLE
                }
            }

            engine.removeEntity(game.currentSnakes[0])
            engine.removeEntity(game.currentSnakes[1])

            game.currentSnakes.clear()
            game.currentSnakes += oldSnake
        }
    }

    private fun checkCollisions(position: Vector2, oldPosition: Vector2): Boolean
    {
        entities.forEach { entity ->
            val snake = entity[SnakeComponent.mapper]
            require(snake != null) { "Entity $entity must have a SnakeComponent.." }

            if (snake.parts.contains(position))
                return false

            engine.getEntitiesFor(allOf(WallComponent::class).get()).forEach { wallEntity ->
                run {
                    wallEntity[WallComponent.mapper]?.let { wall ->
                        if (wall.position == position)
                            return false
                    }
                }
            }

            if (!checkDoorCollisions(position, oldPosition))
                return false
        }

        return true
    }

    private fun checkRecombination() {
        if (entities.size() != 2) // We need separated snakes
            return

        val snake1 = entities[0]
        val snake2 = entities[1]

        val snakeComponent1 = snake1[SnakeComponent.mapper]
        require(snakeComponent1 != null) { "Entity $snake1 must have a SnakeComponent.." }

        val snakeComponent2 = snake2[SnakeComponent.mapper]
        require(snakeComponent2 != null) { "Entity $snake2 must have a SnakeComponent.." }

        val tail1 = snakeComponent1.parts.last()
        val tail2 = snakeComponent2.parts.last()

        // They have to be next to each other
        if (tail1.dst2(tail2) != 1f)
            return

        if (!checkDoorCollisions(tail1, tail2))
            return

        val newParts = arrayListOf<Vector2>()
        newParts += when (snakeComponent1.snakeType) {
            SnakeType.FIRST -> snakeComponent1.parts + snakeComponent2.parts.asReversed()
            SnakeType.SECOND -> snakeComponent2.parts + snakeComponent1.parts.asReversed()
            else -> error("There should not be a double snake here")
        }

        val newSnake = engine.entity {
            with<SnakeComponent> {
                parts += newParts
                snakeType = SnakeType.DOUBLE
            }
        }

        when (snakeComponent1.snakeType) {
            SnakeType.FIRST -> game.moveHistory.push(Move.Recombination(snakeComponent1.parts.toTypedArray(), snakeComponent2.parts.toTypedArray()))
            SnakeType.SECOND -> game.moveHistory.push(Move.Recombination(snakeComponent2.parts.toTypedArray(), snakeComponent1.parts.toTypedArray()))
            else -> error("There should not be a double snake here")
        }

        engine.removeEntity(snake1)
        engine.removeEntity(snake2)

        game.currentSnakes.clear()
        game.currentSnakes += newSnake
    }

    private fun checkSplitting(entity: Entity, snake: SnakeComponent): Boolean {
        engine.getEntitiesFor(allOf(DoorComponent::class).get()).forEach { doorEntity ->
            run {
                doorEntity[DoorComponent.mapper]?.let { door ->
                    if (!door.open && snake.parts.contains(door.position)) {
                        val idx = snake.parts.indexOf(door.position)

                        if (idx != 0 && idx != snake.parts.size - 1) {
                            val lPos = snake.parts[idx]
                            val rPos = snake.parts[idx - 1]

                            val addToFirst = when {
                                door.facing == Facing.SOUTH && isSplit(lPos, rPos, vec2(0f, door.position.y - 0.5f)) -> 0
                                door.facing == Facing.NORTH && isSplit(lPos, rPos, vec2(0f, door.position.y + 0.5f)) -> 0
                                door.facing == Facing.EAST && isSplit(lPos, rPos, vec2(door.position.x - 0.5f, 0f)) -> 0
                                door.facing == Facing.WEST && isSplit(lPos, rPos, vec2(door.position.x + 0.5f, 0f)) -> 0
                                else -> 1
                            }

                            val copyList = snake.parts.toTypedArray()
                            val snake1Parts = copyList.copyOfRange(0, idx + addToFirst)
                            val snake2Parts = copyList.copyOfRange(idx + addToFirst, snake.parts.size).reversedArray()

                            val snake1 = engine.entity {
                                with<SnakeComponent> {
                                    parts += snake1Parts
                                    snakeType = SnakeType.FIRST
                                }
                            }

                            val snake2 = engine.entity {
                                with<SnakeComponent> {
                                    parts += snake2Parts
                                    snakeType = SnakeType.SECOND
                                }
                            }

                            game.moveHistory.push(Move.Separation(copyList))

                            engine.removeEntity(entity)

                            game.currentSnakes.clear()
                            game.currentSnakes += snake1
                            game.currentSnakes += snake2

                            return true
                        }
                    }
                }
            }
        }

        return false
    }

    private fun checkDoorCollisions(left: Vector2, right: Vector2): Boolean {
        engine.getEntitiesFor(allOf(DoorComponent::class).get()).forEach { doorEntity ->
            run {
                doorEntity[DoorComponent.mapper]?.let { door ->
                    if ((door.position == left || door.position == right) && !door.open) {
                        when {
                            door.facing == Facing.SOUTH && isSplit(left, right, vec2(0f, door.position.y - 0.5f)) -> return false
                            door.facing == Facing.NORTH && isSplit(left, right, vec2(0f, door.position.y + 0.5f)) -> return false
                            door.facing == Facing.EAST && isSplit(left, right, vec2(door.position.x - 0.5f, 0f)) -> return false
                            door.facing == Facing.WEST && isSplit(left, right, vec2(door.position.x + 0.5f, 0f)) -> return false
                        }
                    }
                }
            }
        }

        return true
    }

    private fun isSplit(left: Vector2, right: Vector2, axis: Vector2): Boolean {
        return when {
            axis.x != 0f -> (left.x < axis.x && right.x > axis.x) || (left.x > axis.x && right.x < axis.x)
            else -> (left.y < axis.y && right.y > axis.y) || (left.y > axis.y && right.y < axis.y)
        }
    }
}

enum class Direction {
    UP, DOWN, LEFT, RIGHT
}

enum class SnakeType {
    FIRST,
    SECOND,
    DOUBLE
}