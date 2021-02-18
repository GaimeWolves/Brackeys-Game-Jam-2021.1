package com.gamewolves.bgj2021.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.viewport.Viewport
import com.gamewolves.bgj2021.ecs.components.ButtonComponent
import com.gamewolves.bgj2021.ecs.components.GoalComponent
import ktx.ashley.allOf
import ktx.ashley.get
import ktx.graphics.use

class GoalRenderSystem(
        private val batch: SpriteBatch,
        private val viewport: Viewport,
        private val shapeRenderer: ShapeRenderer
) : IteratingSystem(
        allOf(GoalComponent::class).get()
) {
    override fun update(deltaTime: Float) {
        //viewport.apply()
        Gdx.gl.glLineWidth(3f)
        shapeRenderer.use(ShapeRenderer.ShapeType.Line, viewport.camera.combined) {
            super.update(deltaTime)
        }
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val goal = entity[GoalComponent.mapper]
        require(goal != null) { "Entity $entity must have a GoalComponent." }

        when (goal.snakeType) {
            SnakeType.FIRST -> shapeRenderer.color = Color.GREEN
            SnakeType.SECOND -> shapeRenderer.color = Color.BLUE
            SnakeType.DOUBLE -> shapeRenderer.color = Color.BLUE
        }

        shapeRenderer.circle(goal.position.x + 0.5f, goal.position.y + 0.5f, 0.45f)
    }
}