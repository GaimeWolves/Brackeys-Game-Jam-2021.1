package com.gamewolves.bgj2021.desktop

import com.badlogic.gdx.Application
import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration
import com.gamewolves.bgj2021.Main

object DesktopLauncher {
    @JvmStatic
    fun main(arg: Array<String>) {
        val config = LwjglApplicationConfiguration().apply {
            width = 1600
            height = 900
        }

        LwjglApplication(Main(), config).logLevel = Application.LOG_DEBUG
    }
}