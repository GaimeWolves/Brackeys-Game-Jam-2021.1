package com.gamewolves.bgj2021.ecs.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Pool
import com.gamewolves.bgj2021.ecs.systems.SnakeType
import ktx.ashley.mapperFor
import ktx.math.vec2

class WallComponent : Component, Pool.Poolable {
    val position = vec2(0f, 0f)
    var texture = TextureRegion()

    override fun reset() {
        position.set(0f, 0f)
        texture = TextureRegion()
    }

    companion object {
        val mapper = mapperFor<WallComponent>()
    }
}