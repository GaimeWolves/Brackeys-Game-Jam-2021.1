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
        //viewport.apply()
        shapeRenderer.color = Color.BLUE
        Gdx.gl.glLineWidth(1f)
        shapeRenderer.use(ShapeRenderer.ShapeType.Line, viewport.camera.combined) {
            super.update(deltaTime)
        }
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val door = entity[DoorComponent.mapper]
        require(door != null) { "Entity $entity must have a DoorComponent." }

        if (Gdx.input.isKeyJustPressed(Input.Keys.O))
            door.open = !door.open

        val width = when (door.open) {
            true -> 0.1f
            false -> 1f
        }

        when (door.facing) {
            Facing.EAST -> shapeRenderer.rect(door.position.x + 0.9f, door.position.y, 0.1f, width)
            Facing.WEST -> shapeRenderer.rect(door.position.x, door.position.y, 0.1f, width)
            Facing.NORTH -> shapeRenderer.rect(door.position.x, door.position.y + 0.9f, width, 0.1f)
            Facing.SOUTH -> shapeRenderer.rect(door.position.x, door.position.y, width, 0.1f)
        }
    }
}