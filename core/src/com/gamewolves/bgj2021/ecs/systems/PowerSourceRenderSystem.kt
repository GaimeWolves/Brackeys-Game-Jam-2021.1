package com.gamewolves.bgj2021.ecs.systems

import PowerSourceComponent
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.viewport.Viewport
import com.gamewolves.bgj2021.ecs.components.ButtonComponent
import com.gamewolves.bgj2021.ecs.components.CableComponent
import ktx.ashley.allOf
import ktx.ashley.get
import ktx.graphics.use
import kotlin.random.Random

class PowerSourceRenderSystem(
        private val batch: SpriteBatch,
        private val viewport: Viewport,
        private val shapeRenderer: ShapeRenderer
) : IteratingSystem(
        allOf(PowerSourceComponent::class).get()
) {
    override fun update(deltaTime: Float) {
        //viewport.apply()
        Gdx.gl.glLineWidth(3f)
        shapeRenderer.use(ShapeRenderer.ShapeType.Line, viewport.camera.combined) {
            super.update(deltaTime)
        }
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val source = entity[PowerSourceComponent.mapper]
        require(source != null) { "Entity $entity must have a PowerSourceComponent." }

        shapeRenderer.color = Color.RED
        shapeRenderer.line(source.position.x + 0.2f, source.position.y + 0.75f, source.position.x + 0.8f, source.position.y + 0.75f)
        shapeRenderer.line(source.position.x + 0.2f, source.position.y + 0.25f, source.position.x + 0.8f, source.position.y + 0.25f)

        shapeRenderer.color = Color.YELLOW
        shapeRenderer.line(
                source.position.x + 0.5f + Random.nextFloat() - 0.5f,
                source.position.y + 0.5f + Random.nextFloat() - 0.5f,
                source.position.x + 0.5f + Random.nextFloat() - 0.5f,
                source.position.y + 0.5f + Random.nextFloat() - 0.5f
        )
    }
}