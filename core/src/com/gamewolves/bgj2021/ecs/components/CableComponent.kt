package com.gamewolves.bgj2021.ecs.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.Pool
import ktx.ashley.mapperFor
import ktx.math.vec2

class CableComponent : Component, Pool.Poolable {
    val position = vec2(0f, 0f)
    var texture = TextureRegion()
    var rotation = 0f
    var flipX = false
    var flipY = false
    var active = false
    var id = 0

    override fun reset() {
        position.set(0f, 0f)
        texture = TextureRegion()
        rotation = 0f
        flipX = false
        flipY = false
        active = false
        id = 0
    }

    companion object {
        val mapper = mapperFor<CableComponent>()
    }
}