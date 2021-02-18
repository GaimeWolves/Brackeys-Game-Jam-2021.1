package com.gamewolves.bgj2021.ecs.systems

import BatteryComponent
import PowerSourceComponent
import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.signals.Listener
import com.badlogic.ashley.signals.Signal
import com.badlogic.ashley.systems.IteratingSystem
import com.gamewolves.bgj2021.ecs.components.*
import com.gamewolves.bgj2021.screens.GameScreen
import com.gamewolves.bgj2021.screens.Move
import ktx.ashley.allOf
import ktx.ashley.get
import ktx.async.KtxAsync

class GoalSystem(
        private val game: GameScreen
) : IteratingSystem(
        allOf(GoalComponent::class).get()
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

    override fun update(deltaTime: Float) {
        game.hasWon = true
        super.update(deltaTime)
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val goal = entity[GoalComponent.mapper]
        require(goal != null) { "Entity $entity must have a GoalComponent." }

        engine.getEntitiesFor(allOf(SnakeComponent::class).get()).forEach { snakeEntity ->
            run {
                snakeEntity[SnakeComponent.mapper]?.let { snake ->
                    if (snake.snakeType == goal.snakeType && !snake.parts.contains(goal.position)) {
                        //game.moveHistory.push(Move.ChargeChanged(battery, battery.charge))
                        game.hasWon = false
                        return
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