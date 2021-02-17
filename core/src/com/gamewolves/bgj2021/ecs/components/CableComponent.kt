package com.gamewolves.bgj2021.ecs.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool
import ktx.ashley.mapperFor
import ktx.math.vec2

class CableComponent : Component, Pool.Poolable {
    val position = vec2(0f, 0f)
    var active = false
    var id = 0

    override fun reset() {
        position.set(0f, 0f)
        active = false
        id = 0
    }

    companion object {
        val mapper = mapperFor<CableComponent>()
    }
}