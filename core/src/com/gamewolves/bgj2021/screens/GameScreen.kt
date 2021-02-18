package com.gamewolves.bgj2021.screens

import BatteryComponent
import PowerSourceComponent
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.PooledEngine
import com.badlogic.ashley.signals.Signal
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.glutils.FrameBuffer
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.maps.MapGroupLayer
import com.badlogic.gdx.maps.objects.PolylineMapObject
import com.badlogic.gdx.maps.objects.RectangleMapObject
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer
import com.badlogic.gdx.maps.tiled.tiles.AnimatedTiledMapTile
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.actions.Actions.*
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.viewport.FitViewport
import com.gamewolves.bgj2021.Main
import com.gamewolves.bgj2021.ecs.components.*
import com.gamewolves.bgj2021.ecs.components.Facing
import com.gamewolves.bgj2021.ecs.systems.*
import com.gamewolves.bgj2021.serializable.SnakeData
import com.gamewolves.bgj2021.ui.createSkin
import kotlinx.coroutines.launch
import ktx.actors.*
import ktx.app.KtxScreen
import ktx.ashley.entity
import ktx.ashley.with
import ktx.assets.async.AssetStorage
import ktx.async.KtxAsync
import ktx.collections.*
import ktx.graphics.use
import ktx.inject.register
import ktx.log.debug
import ktx.log.info
import ktx.log.logger
import ktx.math.vec2
import ktx.scene2d.actors
import ktx.scene2d.table
import ktx.scene2d.textButton
import ktx.tiled.*
import java.util.*
import kotlin.math.floor

private val log = logger<GameScreen>()

class GameScreen(private val main: Main,
                 private val assetStorage: AssetStorage,
                 private val batch: SpriteBatch,
                 private val shapeRenderer: ShapeRenderer,
                 private val font: BitmapFont) : KtxScreen {
    private val maxBlurRadius = 3f
    private val defaultShader = SpriteBatch.createDefaultShader()

    private val viewport = FitViewport(32f,18f).apply { apply() }
    private val uiViewport = FitViewport(960f, 540f).apply { apply() }

    private val blurShader by lazy {
        val vertexShaderSource = Gdx.files.internal("shaders/blur.vert").readString()
        val fragmentShaderSource = Gdx.files.internal("shaders/blur.frag").readString()
        val shader = ShaderProgram(vertexShaderSource, fragmentShaderSource)

        if (shader.log.isNotEmpty())
            log.info { shader.log }

        shader.use {
            shader.setUniformf("dir", 0f, 0f)
            shader.setUniformf("resolution", Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())
            shader.setUniformf("radius", 0f)
        }

        return@lazy shader
    }
    private val fboA by lazy { FrameBuffer(Pixmap.Format.RGBA8888, Gdx.graphics.width, Gdx.graphics.height, false, false) }
    private val fboB by lazy { FrameBuffer(Pixmap.Format.RGBA8888, Gdx.graphics.width, Gdx.graphics.height, false, false) }
    private var blurRadius = maxBlurRadius

    private lateinit var pauseButton: Table
    private lateinit var pauseMenu: Table
    private lateinit var levelCompleteMenu: Table

    private val stage by lazy { Stage(uiViewport, batch).apply {
        Gdx.input.inputProcessor = this
        createSkin(font)

        this.actors {
            lateinit var hidePauseMenuAction: SequenceAction
            lateinit var showPauseMenuAction: SequenceAction
            lateinit var hidePauseButtonAction: SequenceAction
            lateinit var showPauseButtonAction: SequenceAction

            fun rebuildActions() {
                hidePauseMenuAction = Actions.run { pauseMenu.touchable = Touchable.disabled } +
                        alpha(1f) +
                        moveTo(0f, 0f) +
                        (fadeOut(0.5f) / moveBy(0f, -5f, 0.5f)) +
                        Actions.run { pauseMenu.isVisible = false }
                showPauseMenuAction = Actions.run { pauseMenu.isVisible = true } +
                        alpha(0f) +
                        moveTo(0f, -5f) +
                        (fadeIn(0.5f) / moveBy(0f, 5f, 0.5f)) +
                        Actions.run { pauseMenu.touchable = Touchable.enabled }

                hidePauseButtonAction = Actions.run { pauseButton.touchable = Touchable.disabled } +
                        alpha(1f) +
                        moveTo(0f, 0f) +
                        (fadeOut(0.5f) / moveBy(0f, 5f, 0.5f)) +
                        Actions.run { pauseButton.isVisible = false }
                showPauseButtonAction = Actions.run { pauseButton.isVisible = true } +
                        alpha(0f) +
                        moveTo(0f, 5f) +
                        (fadeIn(0.5f) / moveBy(0f, -5f, 0.5f)) +
                        Actions.run { pauseButton.touchable = Touchable.enabled }
            }
            pauseButton = table {
                align(Align.topRight)
                defaults().top().right().padTop(5f).padRight(5f)

                textButton("Pause") {
                    onClick {
                        isPaused = true

                        rebuildActions()

                        pauseMenu += showPauseMenuAction
                        pauseButton += hidePauseButtonAction
                    }
                }

                setFillParent(true)
                pack()
            }
            pauseMenu = table {
                defaults().fillX().center()

                textButton("Continue") { cell ->
                    cell.padLeft(10f).padRight(10f)

                    onClick {
                        isPaused = false

                        rebuildActions()

                        pauseButton += showPauseButtonAction
                        pauseMenu += hidePauseMenuAction
                    }
                }

                textButton("Restart") { cell ->
                    cell.padLeft(10f).padRight(10f)

                    onClick {
                        while (!moveHistory.empty())
                            revertHistory()

                        isPaused = false

                        rebuildActions()

                        pauseButton += showPauseButtonAction
                        pauseMenu += hidePauseMenuAction
                    }
                }

                textButton("Exit") {
                    onClick { dispose() }
                }

                isVisible = false
                touchable = Touchable.disabled
                setFillParent(true)
                pack()
            }
            levelCompleteMenu = table {
                defaults().fillX().center()

                textButton("Next level") { cell ->
                    cell.padLeft(10f).padRight(10f)

                    onClick { dispose() }
                }
                textButton("Exit") { cell ->
                    cell.padLeft(10f).padRight(10f)

                    onClick { dispose() }
                }

                isVisible = false
                touchable = Touchable.disabled

                setFillParent(true)
                pack()
            }
        }
    } }

    private val doorSystem by lazy { DoorInputSystem(this@GameScreen) }
    private val buttonSystem by lazy { ButtonInputSystem(this@GameScreen) }
    private val snakeSystem by lazy { SnakeInputSystem(this@GameScreen) }
    private val electricitySystem by lazy { ElectricitySystem(this@GameScreen) }
    private val goalSystem by lazy { GoalSystem(this@GameScreen) }

    private val engine by lazy { PooledEngine().apply {
        addSystem(snakeSystem)
        addSystem(buttonSystem)
        addSystem(doorSystem)
        addSystem(electricitySystem)
        addSystem(goalSystem)
        addSystem(CableInputSystem(this@GameScreen))
        addSystem(FloorRenderSystem(batch, viewport, shapeRenderer))
        addSystem(WallRenderSystem(batch, viewport, shapeRenderer))
        addSystem(CableRenderSystem(batch, viewport, shapeRenderer))
        addSystem(DoorRenderSystem(batch, viewport, shapeRenderer))
        addSystem(ButtonRenderSystem(batch, viewport, shapeRenderer))
        addSystem(PowerSourceRenderSystem(batch, viewport, shapeRenderer))
        addSystem(BatteryRenderSystem(this@GameScreen, batch, viewport, uiViewport, font, shapeRenderer))
        addSystem(GoalRenderSystem(batch, viewport, shapeRenderer))
        addSystem(SnakeRenderSystem(batch, assetStorage["snake.atlas"], viewport, shapeRenderer))
    } }

    private val map by lazy { assetStorage.loadSync<TiledMap>("levels/level_test.tmx") }
    private val spawnPositions by lazy {
        val json = Gdx.files.internal("levels/level_test.snake").readString()
        SnakeData.deserialize(json)
    }

    val moveSignal = Signal<Move.SnakeMove>()
    val uiPixelScale = uiViewport.worldWidth / viewport.worldWidth
    val moveHistory = Stack<Move>()

    var hasWon = false
    var isPaused = false

    val currentSnakes = arrayListOf<Entity>()

    override fun show() {
        generateEntities()

        super.show()
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height, true)
        uiViewport.update(width, height, true)

        blurShader.use {
            blurShader.setUniformf("resolution", width.toFloat(), height.toFloat())
        }
    }

    override fun render(delta: Float) {
        if ((!hasWon && !isPaused) && blurRadius > 0f) {
            blurRadius -= delta * maxBlurRadius

            if (blurRadius < 0f)
                blurRadius = 0f
        } else if ((hasWon || isPaused) && blurRadius < maxBlurRadius) {
            blurRadius += delta * maxBlurRadius

            if (blurRadius > maxBlurRadius)
                blurRadius = maxBlurRadius
        }

        applyBlur { engine.update(delta) }

        stage.run {
            uiViewport.apply()
            act()
            draw()
        }

        if ((hasWon || isPaused) && snakeSystem.checkProcessing()) {
            snakeSystem.setProcessing(false)
            doorSystem.setProcessing(false)
            buttonSystem.setProcessing(false)
            goalSystem.setProcessing(false)
        }
        else if (!isPaused && !hasWon && !snakeSystem.checkProcessing()) {
            snakeSystem.setProcessing(true)
            doorSystem.setProcessing(true)
            buttonSystem.setProcessing(true)
            goalSystem.setProcessing(true)
        }

        if (snakeSystem.checkProcessing()) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE))
                revertHistory()
        }
    }

    private fun applyBlur(mainRenderPass: () -> Unit) {
        fboA.begin()
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        batch.shader = defaultShader
        mainRenderPass()
        batch.flush()
        fboA.end()

        fboB.begin()
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        batch.use(viewport.camera.combined) {
            batch.shader = blurShader
            blurShader.setUniformf("dir", 1f, 0f)
            blurShader.setUniformf("radius", blurRadius)
            val texture = fboA.colorBufferTexture.apply { setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear) }
            batch.draw(
                    texture,
                    0f,
                    0f,
                    viewport.worldWidth,
                    viewport.worldHeight,
                    0,
                    0,
                    viewport.screenWidth,
                    viewport.screenHeight,
                    false,
                    false
            )
        }
        batch.flush()
        fboB.end()

        fboA.begin()
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        batch.use(viewport.camera.combined) {
            batch.shader = blurShader
            blurShader.setUniformf("dir", 0f, 1f)
            val texture = fboB.colorBufferTexture.apply { setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear) }
            batch.draw(
                    texture,
                    0f,
                    0f,
                    viewport.worldWidth,
                    viewport.worldHeight,
                    0,
                    0,
                    viewport.screenWidth,
                    viewport.screenHeight,
                    false,
                    false
            )
        }
        batch.flush()
        fboA.end()

        viewport.apply()
        batch.use(viewport.camera.projection) {
            val texture = fboA.colorBufferTexture.apply { setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear) }
            batch.draw(
                    texture,
                    -viewport.worldWidth * 0.5f,
                    -viewport.worldHeight * 0.5f,
                    viewport.worldWidth,
                    viewport.worldHeight,
                    0,
                    0,
                    viewport.screenWidth,
                    viewport.screenHeight,
                    false,
                    true
            )
        }

        batch.shader = defaultShader
    }

    private fun revertHistory() {
        if (moveHistory.empty())
            return

        when (val move = moveHistory.pop()) {
            is Move.SnakeMove -> {
                snakeSystem.revertSnake(move)
            }
            is Move.Recombination -> {
                snakeSystem.revertSnake(move)
                revertHistory()
            }
            is Move.Separation -> {
                snakeSystem.revertSnake(move)
                revertHistory()
            }
            is Move.ButtonChanged -> {
                move.button.pressed = move.pressed
                revertHistory()
            }
            is Move.DoorChanged -> {
                move.door.open = move.open
                revertHistory()
            }
            is Move.PoweredChanged -> {
                move.snake.powered = move.powered
                revertHistory()
            }
            is Move.ChargeChanged -> {
                move.battery.charge = move.charge
                revertHistory()
            }
        }
    }

    private fun generateEntities() {
        val batteryCharges = arrayListOf<Pair<Vector2, Int>>()
        val goals = arrayListOf<Pair<Vector2, SnakeType>>()

        map.layer("entity-data").run {
            this.objects.forEach {
                when {
                    it.containsProperty("snakeType") -> {
                        val polyLineObj = it as PolylineMapObject
                        val verticesFloats = polyLineObj.polyline.transformedVertices
                        val parts = arrayListOf<Vector2>()
                        for (i in 0 until verticesFloats.size / 2)
                            parts += vec2(floor(verticesFloats[i * 2] / map.tileWidth), floor(verticesFloats[i * 2 + 1] / map.tileHeight))

                        val snakeType = SnakeType.valueOf(it.property("snakeType", "FIRST"))

                        engine.entity {
                            with<SnakeComponent> {
                                this.parts += parts
                                this.snakeType = snakeType
                            }
                        }
                    }
                    it.containsProperty("maxCharge") -> {
                        val pos = vec2(floor(it.x / map.tileWidth), floor(it.y / map.tileHeight))
                        val charge = it.property("maxCharge", 10)

                        batteryCharges += Pair(pos, charge)
                    }
                    it.containsProperty("goalType") -> {
                        val pos = vec2(floor(it.x / map.tileWidth), floor(it.y / map.tileHeight))
                        val snakeType = SnakeType.valueOf(it.property("goalType", "FIRST"))

                        goals += Pair(pos, snakeType)
                    }
                }
            }
        }


        fun loadNormalLayer(name: String, creator: (Int, Int, TiledMapTileLayer.Cell) -> Unit) {
            (map.layer(name) as TiledMapTileLayer).run {
                for (x in 0 until this.width) {
                    for (y in 0 until this.height) {
                        val cell = this.getCell(x, y)
                        if (cell != null)
                            creator(x, y, cell)
                    }
                }
            }
        }

        fun loadEntityLayer(name: String, id: Int, creator: (Int, Int, TiledMapTileLayer.Cell) -> Unit) {
            (map.layer(id.toString()) as MapGroupLayer).run {
                (this.layers.find { it.name == name } as TiledMapTileLayer?)?.let { layer ->
                    for (x in 0 until layer.width) {
                        for (y in 0 until layer.height) {
                            val cell = layer.getCell(x, y)
                            if (cell != null)
                                creator(x, y, cell)
                        }
                    }
                }
            }
        }

        // create walls
        loadNormalLayer("walls") { x, y, cell ->
            engine.entity {
                with<WallComponent> {
                    position.set(x.toFloat(), y.toFloat())
                    texture.setRegion(cell.tile.textureRegion)
                }
            }
        }

        // create floor
        loadNormalLayer("floor") { x, y, cell ->
            engine.entity {
                with<FloorComponent> {
                    position.set(x.toFloat(), y.toFloat())
                    texture.setRegion(cell.tile.textureRegion)
                }
            }
        }

        // create goals
        loadNormalLayer("goals") { x, y, cell ->
            engine.entity {
                with<GoalComponent> {
                    position.set(x.toFloat(), y.toFloat())
                    snakeType = goals.find { (pos, _) -> pos == position }?.second!!
                    texture.setRegion(cell.tile.textureRegion)
                }
            }
        }

        // load power sources
        loadNormalLayer("sources") { x, y, cell ->
            engine.entity {
                with<PowerSourceComponent> {
                    position.set(x.toFloat(), y.toFloat())
                    texture.setRegion(cell.tile.textureRegion)
                }
            }
        }

        // load grouped entities
        val idLayerCount = map.layers.size() - 5 // minus normal layer count
        for (layerId in 0 until idLayerCount) {
            loadEntityLayer("buttons", layerId) { x, y, cell ->
                engine.entity {
                    with<ButtonComponent> {
                        position.set(x.toFloat(), y.toFloat())
                        id = layerId
                        texture.setRegion(cell.tile.textureRegion)
                    }
                }
            }

            loadEntityLayer("doors", layerId) { x, y, cell ->
                engine.entity {
                    with<DoorComponent> {
                        position.set(x.toFloat(), y.toFloat())
                        id = layerId
                        facing = Facing.values()[cell.rotation]

                        val frames = (cell.tile as AnimatedTiledMapTile).frameTiles.map { it.textureRegion }
                        closedTexture.setRegion(frames[0])
                        openTexture.setRegion(frames[1])
                    }
                }
            }

            loadEntityLayer("cables", layerId) { x, y, cell ->
                engine.entity {
                    with<CableComponent> {
                        position.set(x.toFloat(), y.toFloat())
                        id = layerId
                        texture.setRegion(cell.tile.textureRegion)
                        rotation = cell.rotation.toFloat() * 90f + 90f
                        flipX = cell.flipHorizontally
                        flipY = cell.flipVertically
                    }
                }
            }

            loadEntityLayer("batteries", layerId) { x, y, cell ->
                engine.entity {

                    with<BatteryComponent> {
                        position.set(x.toFloat(), y.toFloat())
                        id = layerId
                        maxCharge = batteryCharges.find { (pos, _) -> pos == position }?.second!!

                        val frames = (cell.tile as AnimatedTiledMapTile).frameTiles.map { it.textureRegion }
                        emptyTexture.setRegion(frames[0])
                        chargedTexture.setRegion(frames[1])
                    }
                }
            }
        }
    }
}

sealed class Move {
    class SnakeMove(val snakeType: SnakeType, var position: Vector2, val reversed: Boolean): Move()
    class Recombination(var oldFirstSnake: Array<Vector2>, var oldSecondSnake: Array<Vector2>): Move()
    class Separation(var oldDoubleSnake: Array<Vector2>): Move()
    class ButtonChanged(val button: ButtonComponent, val pressed: Boolean): Move()
    class DoorChanged(val door: DoorComponent, val open: Boolean): Move()
    class PoweredChanged(val snake: SnakeComponent, val powered: Boolean): Move()
    class ChargeChanged(val battery: BatteryComponent, val charge: Int): Move()
}