package com.gamewolves.bgj2021.ecs.systems

import BatteryComponent
import PowerSourceComponent
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.viewport.Viewport
import com.gamewolves.bgj2021.ecs.components.ButtonComponent
import com.gamewolves.bgj2021.ecs.components.CableComponent
import com.gamewolves.bgj2021.screens.GameScreen
import ktx.ashley.allOf
import ktx.ashley.get
import ktx.graphics.use
import kotlin.random.Random

class BatteryRenderSystem(
        private val game: GameScreen,
        private val batch: SpriteBatch,
        private val viewport: Viewport,
        private val uiViewport: Viewport,
        private val font: BitmapFont,
        private val shapeRenderer: ShapeRenderer
) : IteratingSystem(
        allOf(BatteryComponent::class).get()
) {
    override fun processEntity(entity: Entity, deltaTime: Float) {
        val battery = entity[BatteryComponent.mapper]
        require(battery != null) { "Entity $entity must have a BatteryComponent." }

        //viewport.apply()
        Gdx.gl.glLineWidth(3f)
        shapeRenderer.use(ShapeRenderer.ShapeType.Line, viewport.camera.combined) {
            shapeRenderer.color = Color.RED
            shapeRenderer.triangle(
                    battery.position.x + 0.2f,
                    battery.position.y + 0.2f,
                    battery.position.x + 0.8f,
                    battery.position.y + 0.2f,
                    battery.position.x + 0.5f,
                    battery.position.y + 0.8f
            )

            if (battery.charge > 0) {
                shapeRenderer.color = Color.YELLOW
                shapeRenderer.line(
                        battery.position.x + 0.5f + Random.nextFloat() - 0.5f,
                        battery.position.y + 0.5f + Random.nextFloat() - 0.5f,
                        battery.position.x + 0.5f + Random.nextFloat() - 0.5f,
                        battery.position.y + 0.5f + Random.nextFloat() - 0.5f
                )
            }
        }

        //if (battery.charge > 0) {
        //    val uiPosition = battery.position.cpy().scl(game.pixelScale)
        //
        //    uiViewport.apply()
        //    batch.use(uiViewport.camera.combined) {
        //        font.draw(
        //                batch,
        //                battery.charge.toString(),
        //                uiPosition.x - game.pixelScale,
        //                uiPosition.y + game.pixelScale * 1f,
        //                game.pixelScale * 3,
        //                Align.center,
        //                false
        //        )
        //    }
        //}
    }
}