package com.gamewolves.bgj2021.ecs.systems

import BatteryComponent
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.gamewolves.bgj2021.ecs.components.ButtonComponent
import com.gamewolves.bgj2021.ecs.components.DoorComponent
import com.gamewolves.bgj2021.screens.GameScreen
import com.gamewolves.bgj2021.screens.Move
import ktx.ashley.allOf
import ktx.ashley.get

class DoorInputSystem(
        private val game: GameScreen
) : IteratingSystem(
        allOf(DoorComponent::class).get()
) {
    override fun processEntity(entity: Entity, deltaTime: Float) {
        val door = entity[DoorComponent.mapper]
        require(door != null) { "Entity $entity must have a DoorComponent." }

        val oldOpen = door.open
        door.open = true

        engine.getEntitiesFor(allOf(ButtonComponent::class).get()).forEach { buttonEntity ->
            run {
                buttonEntity[ButtonComponent.mapper]?.let { button ->
                    if (button.id == door.id && !button.pressed) {
                        door.open = false
                        return@forEach
                    }
                }
            }
        }

        if (door.open) {
            engine.getEntitiesFor(allOf(BatteryComponent::class).get()).forEach { batteryEntity ->
                run {
                    batteryEntity[BatteryComponent.mapper]?.let { battery ->
                        if (battery.id == door.id && battery.charge == 0) {
                            door.open = false
                            return@forEach
                        }
                    }
                }
            }
        }

        if (oldOpen != door.open)
            game.moveHistory.push(Move.DoorChanged(door, oldOpen))
    }
}