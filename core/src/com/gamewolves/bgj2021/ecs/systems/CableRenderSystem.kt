package com.gamewolves.bgj2021.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.viewport.Viewport
import com.gamewolves.bgj2021.ecs.components.ButtonComponent
import com.gamewolves.bgj2021.ecs.components.CableComponent
import com.gamewolves.bgj2021.ui.colorFromHSL
import ktx.ashley.allOf
import ktx.ashley.get
import ktx.graphics.use

class CableRenderSystem(
        private val batch: SpriteBatch,
        private val viewport: Viewport,
        private val shapeRenderer: ShapeRenderer
) : IteratingSystem(
        allOf(CableComponent::class).get()
) {
    override fun update(deltaTime: Float) {
        batch.use(viewport.camera.combined) {
            super.update(deltaTime)
        }
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val cable = entity[CableComponent.mapper]
        require(cable != null) { "Entity $entity must have a CableComponent." }

        val oldColor = batch.color.cpy()

        val saturation = when (cable.active) {
            true -> 0.8f
            false -> 0.6f
        }

        val color = colorFromHSL(cable.id.toFloat() * (1f / 6f), saturation, 0.5f)

        when (cable.active) {
            false -> batch.color = color.mul(batch.color)
            true -> batch.color = color.mul(batch.color)
        }

        val scaleX = when (cable.flipX) {
            true -> -1f
            false -> 1f
        }

        val scaleY = when (cable.flipY) {
            true -> -1f
            false -> 1f
        }

        batch.draw(
                cable.texture,
                cable.position.x,
                cable.position.y,
                0.5f,
                0.5f,
                1f,
                1f,
                scaleX,
                scaleY,
                cable.rotation,
                true
        )

        batch.color = oldColor
    }
}