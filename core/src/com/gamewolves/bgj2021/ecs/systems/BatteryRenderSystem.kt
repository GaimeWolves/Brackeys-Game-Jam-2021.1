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
    override fun update(deltaTime: Float) {
        batch.use(viewport.camera.combined) {
            super.update(deltaTime)
        }
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val battery = entity[BatteryComponent.mapper]
        require(battery != null) { "Entity $entity must have a BatteryComponent." }

        when (battery.charge) {
            0 -> batch.draw(battery.emptyTexture, battery.position.x, battery.position.y, 1f, 1f)
            else -> batch.draw(battery.chargedTexture, battery.position.x, battery.position.y, 1f, 1f)
        }

        //if (battery.charge > 0) {
        //    val uiPosition = battery.position.cpy().scl(game.uiPixelScale)
        //
        //    batch.use(uiViewport.camera.combined) {
        //        font.draw(
        //                batch,
        //                battery.charge.toString(),
        //                uiPosition.x - game.uiPixelScale,
        //                uiPosition.y + game.uiPixelScale,
        //                game.uiPixelScale * 3,
        //                Align.center,
        //                false
        //        )
        //    }
        //}
    }
}