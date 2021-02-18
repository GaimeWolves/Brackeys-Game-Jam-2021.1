package com.gamewolves.bgj2021.ecs.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Pool
import com.gamewolves.bgj2021.ecs.systems.SnakeType
import ktx.ashley.mapperFor
import ktx.math.vec2

class GoalComponent : Component, Pool.Poolable {
    val position = vec2(0f, 0f)
    var snakeType = SnakeType.FIRST
    var reached = false

    override fun reset() {
        position.set(0f, 0f)
        snakeType = SnakeType.FIRST
        reached = false
    }

    companion object {
        val mapper = mapperFor<GoalComponent>()
    }
}