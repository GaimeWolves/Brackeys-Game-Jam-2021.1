package com.gamewolves.bgj2021.ecs.systems

import BatteryComponent
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.gamewolves.bgj2021.ecs.components.ButtonComponent
import com.gamewolves.bgj2021.ecs.components.CableComponent
import com.gamewolves.bgj2021.ecs.components.DoorComponent
import com.gamewolves.bgj2021.screens.GameScreen
import com.gamewolves.bgj2021.screens.Move
import ktx.ashley.allOf
import ktx.ashley.get

class CableInputSystem(
        private val game: GameScreen
) : IteratingSystem(
        allOf(CableComponent::class).get()
) {
    override fun processEntity(entity: Entity, deltaTime: Float) {
        val cable = entity[CableComponent.mapper]
        require(cable != null) { "Entity $entity must have a CableComponent." }

        cable.active = true

        engine.getEntitiesFor(allOf(ButtonComponent::class).get()).forEach { buttonEntity ->
            run {
                buttonEntity[ButtonComponent.mapper]?.let { button ->
                    if (button.id == cable.id && !button.pressed) {
                        cable.active = false
                        return@forEach
                    }
                }
            }
        }

        if (cable.active) {
            engine.getEntitiesFor(allOf(BatteryComponent::class).get()).forEach { batteryEntity ->
                run {
                    batteryEntity[BatteryComponent.mapper]?.let { battery ->
                        if (battery.id == cable.id && battery.charge == 0) {
                            cable.active = false
                            return@forEach
                        }
                    }
                }
            }
        }
    }
}