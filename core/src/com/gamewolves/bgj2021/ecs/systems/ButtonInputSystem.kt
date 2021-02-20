package com.gamewolves.bgj2021.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.gamewolves.bgj2021.assets.SoundAsset
import com.gamewolves.bgj2021.ecs.components.ButtonComponent
import com.gamewolves.bgj2021.ecs.components.SnakeComponent
import com.gamewolves.bgj2021.screens.GameScreen
import com.gamewolves.bgj2021.screens.Move
import ktx.ashley.allOf
import ktx.ashley.get

class ButtonInputSystem(
        private val game: GameScreen
) : IteratingSystem(
        allOf(ButtonComponent::class).get()
) {
    private val buttonDown = game.assetStorage[SoundAsset.BUTTON_DOWN.descriptor]
    private val buttonUp = game.assetStorage[SoundAsset.BUTTON_UP.descriptor]

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val button = entity[ButtonComponent.mapper]
        require(button != null) { "Entity $entity must have a ButtonComponent." }

        val oldPressed = button.pressed
        button.pressed = false

        engine.getEntitiesFor(allOf(SnakeComponent::class).get()).forEach { snakeEntity ->
            run {
                snakeEntity[SnakeComponent.mapper]?.let { snake ->
                    if (snake.parts.contains(button.position)) {
                        button.pressed = true
                        return@forEach
                    }
                }
            }
        }

        if (oldPressed != button.pressed) {
            game.moveHistory.push(Move.ButtonChanged(button, oldPressed))

            when (button.pressed) {
                true -> buttonDown.play(0.25f)
                false -> buttonUp.play(0.25f)
            }
        }
    }
}