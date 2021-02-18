package com.gamewolves.bgj2021.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.viewport.Viewport
import com.gamewolves.bgj2021.ecs.components.DoorComponent
import com.gamewolves.bgj2021.ecs.components.Facing
import com.gamewolves.bgj2021.ecs.components.SnakeComponent
import com.gamewolves.bgj2021.ecs.components.WallComponent
import ktx.ashley.allOf
import ktx.ashley.get
import ktx.collections.iterate
import ktx.graphics.use
import ktx.math.component1
import ktx.math.component2
import kotlin.math.sign

class DoorRenderSystem(
        private val batch: SpriteBatch,
        private val viewport: Viewport,
        private val shapeRenderer: ShapeRenderer
) : IteratingSystem(
        allOf(DoorComponent::class).get()
) {
    override fun update(deltaTime: Float) {
        batch.use(viewport.camera.combined) {
            super.update(deltaTime)
        }
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val door = entity[DoorComponent.mapper]
        require(door != null) { "Entity $entity must have a DoorComponent." }

        val angle = when (door.facing) {
            Facing.EAST -> 90f
            Facing.WEST -> 270f
            Facing.NORTH -> 180f
            Facing.SOUTH -> 0f
        }

        val texture = when (door.open) {
            true -> door.openTexture
            false -> door.closedTexture
        }

        batch.draw(
                texture,
                door.position.x,
                door.position.y,
                0.5f,
                0.5f,
                1f,
                1f,
                1f,
                1f,
                angle
        )
    }
}