package com.gamewolves.bgj2021.ecs.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Pool
import com.gamewolves.bgj2021.ecs.systems.SnakeType
import ktx.ashley.mapperFor
import ktx.math.vec2

class DoorComponent : Component, Pool.Poolable {
    val position = vec2(0f, 0f)
    var closedTexture = TextureRegion()
    var openTexture = TextureRegion()
    var open = false
    var facing = Facing.EAST
    var id = 0

    override fun reset() {
        position.set(0f, 0f)
        closedTexture = TextureRegion()
        openTexture = TextureRegion()
        open = false
        facing = Facing.EAST
        id = 0
    }

    companion object {
        val mapper = mapperFor<DoorComponent>()
    }
}

enum class Facing {
    SOUTH, EAST, NORTH, WEST
}