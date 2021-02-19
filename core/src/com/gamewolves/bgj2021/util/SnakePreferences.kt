package com.gamewolves.bgj2021.util

import com.badlogic.gdx.Gdx
import ktx.preferences.*

private val prefs = Gdx.app.getPreferences("gamewolves-snakegame")

fun hasUnlockedLevel(id: Int): Boolean {
    if (id == 0)
        return  true

    return prefs["level$id", false]
}

fun unlockLevel(id: Int) {
    prefs["level$id"] = true
    prefs.flush()
}