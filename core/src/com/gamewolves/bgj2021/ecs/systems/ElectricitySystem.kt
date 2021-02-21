package com.gamewolves.bgj2021.ecs.systems

import BatteryComponent
import PowerSourceComponent
import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.signals.Listener
import com.badlogic.ashley.signals.Signal
import com.badlogic.ashley.systems.IteratingSystem
import com.gamewolves.bgj2021.assets.SoundAsset
import com.gamewolves.bgj2021.ecs.components.ButtonComponent
import com.gamewolves.bgj2021.ecs.components.DoorComponent
import com.gamewolves.bgj2021.ecs.components.SnakeComponent
import com.gamewolves.bgj2021.ecs.components.WallComponent
import com.gamewolves.bgj2021.screens.GameScreen
import com.gamewolves.bgj2021.screens.Move
import ktx.ashley.allOf
import ktx.ashley.get

class ElectricitySystem(
        private val game: GameScreen
) : IteratingSystem(
        allOf(BatteryComponent::class).get()
), Listener<Move> {
    private lateinit var lastMove: Move
    private val chargeSfx = game.assetStorage[SoundAsset.BATTERY_CHARGED.descriptor]

    override fun addedToEngine(engine: Engine?) {
        super.addedToEngine(engine)
        game.moveSignal.add(this)
        setProcessing(false)
    }

    override fun removedFromEngine(engine: Engine?) {
        super.removedFromEngine(engine)
        game.moveSignal.remove(this)
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val battery = entity[BatteryComponent.mapper]
        require(battery != null) { "Entity $entity must have a BatteryComponent." }

        var charged = false

        engine.getEntitiesFor(allOf(SnakeComponent::class).get()).forEach { snakeEntity ->
            run {
                snakeEntity[SnakeComponent.mapper]?.let { snake ->
                    if (snake.powered && snake.parts.contains(battery.position)) {
                        charged = true

                        if (battery.charge == battery.maxCharge)
                            return@forEach

                        game.moveHistory.push(Move.ChargeChanged(battery, battery.charge))
                        battery.charge = battery.maxCharge
                        chargeSfx.play(0.15f)
                        return@forEach
                    }
                }
            }
        }

        if (battery.charge > 0 && !charged) {
            game.moveHistory.push(Move.ChargeChanged(battery, battery.charge))
            battery.charge--
        }
    }

    override fun receive(signal: Signal<Move>?, move: Move?) {
        move?.let {
            lastMove = move
            update(0f)
        }
    }
}