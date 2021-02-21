package com.gamewolves.bgj2021.ecs.systems

import BatteryComponent
import PowerSourceComponent
import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.signals.Listener
import com.badlogic.ashley.signals.Signal
import com.badlogic.ashley.systems.IteratingSystem
import com.gamewolves.bgj2021.assets.SoundAsset
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
), Listener<Move> {
    private val winSfx = game.assetStorage[SoundAsset.PLAYER_WON.descriptor]

    private lateinit var lastMove: Move
    private var goalsReached = 0

    override fun addedToEngine(engine: Engine?) {
        super.addedToEngine(engine)
        game.moveSignal.add(this)
    }

    override fun removedFromEngine(engine: Engine?) {
        super.removedFromEngine(engine)
        game.moveSignal.remove(this)
    }

    override fun update(deltaTime: Float) {
        goalsReached = 0
        super.update(deltaTime)

        if (goalsReached == entities.size()) {
            game.hasWon = true
            winSfx.play(0.25f)
        }
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val goal = entity[GoalComponent.mapper]
        require(goal != null) { "Entity $entity must have a GoalComponent." }

        engine.getEntitiesFor(allOf(SnakeComponent::class).get()).forEach { snakeEntity ->
            run {
                snakeEntity[SnakeComponent.mapper]?.let { snake ->
                    if (snake.snakeType == goal.snakeType && snake.parts.contains(goal.position)) {
                        goalsReached++
                        return
                    }
                }
            }
        }

    }

    override fun receive(signal: Signal<Move>?, move: Move?) {
        move?.let {
            lastMove = move
            update(0f)
        }
    }
}