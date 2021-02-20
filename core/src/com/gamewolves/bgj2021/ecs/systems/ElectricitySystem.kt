package com.gamewolves.bgj2021.ecs.systems

import BatteryComponent
import PowerSourceComponent
import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.signals.Listener
import com.badlogic.ashley.signals.Signal
import com.badlogic.ashley.systems.IteratingSystem
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
), Listener<Move.SnakeMove> {
    private lateinit var lastMove: Move.SnakeMove

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

        if (battery.charge > 0) {
            game.moveHistory.push(Move.ChargeChanged(battery, battery.charge))
            battery.charge--
        }

        engine.getEntitiesFor(allOf(SnakeComponent::class).get()).forEach { snakeEntity ->
            run {
                snakeEntity[SnakeComponent.mapper]?.let { snake ->
                    if (snake.snakeType == lastMove.snakeType && snake.powered && snake.parts.contains(battery.position)) {
                        game.moveHistory.push(Move.ChargeChanged(battery, battery.charge))
                        battery.charge = battery.maxCharge
                    }
                }
            }
        }

    }

    override fun receive(signal: Signal<Move.SnakeMove>?, move: Move.SnakeMove?) {
        move?.let {
            lastMove = move
            update(0f)
        }
    }
}