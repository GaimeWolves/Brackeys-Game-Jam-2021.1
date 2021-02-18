package com.gamewolves.bgj2021.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.viewport.Viewport
import com.gamewolves.bgj2021.ecs.components.SnakeComponent
import com.gamewolves.bgj2021.ecs.components.WallComponent
import ktx.ashley.allOf
import ktx.ashley.get
import ktx.collections.iterate
import ktx.graphics.use
import ktx.math.component1
import ktx.math.component2
import kotlin.math.sign

class WallRenderSystem(
        private val batch: SpriteBatch,
        private val viewport: Viewport,
        private val shapeRenderer: ShapeRenderer
) : IteratingSystem(
        allOf(WallComponent::class).get()
) {
    override fun update(deltaTime: Float) {
        //viewport.apply()
        shapeRenderer.color = Color.YELLOW
        Gdx.gl.glLineWidth(1f)
        shapeRenderer.use(ShapeRenderer.ShapeType.Line, viewport.camera.combined) {
            super.update(deltaTime)
        }
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val wall = entity[WallComponent.mapper]
        require(wall != null) { "Entity $entity must have a WallComponent." }

        shapeRenderer.rect(wall.position.x, wall.position.y, 1f, 1f)
    }
}