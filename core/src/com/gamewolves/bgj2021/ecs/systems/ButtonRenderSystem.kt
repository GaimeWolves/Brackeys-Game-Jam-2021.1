package com.gamewolves.bgj2021.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.viewport.Viewport
import com.gamewolves.bgj2021.ecs.components.ButtonComponent
import com.gamewolves.bgj2021.ui.colorFromHSL
import ktx.ashley.allOf
import ktx.ashley.get
import ktx.graphics.use

class ButtonRenderSystem(
        private val batch: SpriteBatch,
        private val viewport: Viewport,
        private val shapeRenderer: ShapeRenderer
) : IteratingSystem(
        allOf(ButtonComponent::class).get()
) {
    override fun update(deltaTime: Float) {
        batch.use(viewport.camera.combined) {
            super.update(deltaTime)
        }
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val button = entity[ButtonComponent.mapper]
        require(button != null) { "Entity $entity must have a ButtonComponent." }

        val oldColor = batch.color.cpy()

        val saturation = when (button.pressed) {
            true -> 0.8f
            false -> 0.6f
        }

        val color = colorFromHSL(button.id.toFloat() * (1f / 6f), saturation, 0.5f)

        when (button.pressed) {
            false -> batch.color = color.mul(batch.color)
            true -> batch.color = color.mul(batch.color)
        }

        batch.draw(button.texture, button.position.x, button.position.y, 1f, 1f)

        batch.color = oldColor
    }
}