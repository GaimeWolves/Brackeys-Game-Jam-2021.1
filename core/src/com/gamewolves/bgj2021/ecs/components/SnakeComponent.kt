package com.gamewolves.bgj2021.ecs.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Pool
import com.gamewolves.bgj2021.ecs.systems.Facing
import com.gamewolves.bgj2021.ecs.systems.SnakeType
import ktx.ashley.mapperFor

class SnakeComponent : Component, Pool.Poolable {
    val parts = arrayListOf<Vector2>()
    var snakeType = SnakeType.FIRST
    var lastDirection = Facing.NORTH
    var powered = false

    override fun reset() {
        parts.clear()
        snakeType = SnakeType.FIRST
        powered = false
    }

    companion object {
        val mapper = mapperFor<SnakeComponent>()
    }
}