package com.gamewolves.bgj2021.screens

import BatteryComponent
import PowerSourceComponent
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.PooledEngine
import com.badlogic.ashley.signals.Signal
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
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
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.viewport.FitViewport
import com.gamewolves.bgj2021.Main
import com.gamewolves.bgj2021.assets.*
import com.gamewolves.bgj2021.ecs.components.*
import com.gamewolves.bgj2021.ecs.components.Facing
import com.gamewolves.bgj2021.ecs.systems.*
import com.gamewolves.bgj2021.ui.ImageButtonSkin
import com.gamewolves.bgj2021.ui.LabelSkin
import com.gamewolves.bgj2021.util.unlockLevel
import kotlinx.coroutines.launch
import ktx.actors.*
import ktx.app.KtxScreen
import ktx.ashley.entity
import ktx.ashley.with
import ktx.assets.async.AssetStorage
import ktx.async.KtxAsync
import ktx.collections.*
import ktx.graphics.color
import ktx.graphics.use
import ktx.log.debug
import ktx.log.info
import ktx.log.logger
import ktx.math.vec2
import ktx.scene2d.*
import ktx.tiled.*
import java.util.*
import kotlin.math.floor

private val log = logger<GameScreen>()

const val MAX_BLUR_RADIUS = 3f

class GameScreen(
        val main: Main,
        private val id: Int
) : Screen(main) {
    private val map by lazy { assetStorage[TiledMapAssets.getLevelById(id)] }
    private val mapLabels = arrayListOf<Label>()

    private val viewport = FitViewport(map.width.toFloat(), map.height.toFloat()).apply { apply() }
    private val uiViewport = FitViewport(960f, 960f * (map.height.toFloat() / map.width.toFloat())).apply { apply() }

    private val selectSfx = assetStorage[SoundAsset.SELECT.descriptor]
    private val background = assetStorage[TextureAsset.BACKGROUND.descriptor]

    private val blurShader by lazy { assetStorage[ShaderProgramAsset.BLUR.descriptor] }

    private lateinit var fboA: FrameBuffer
    private lateinit var fboB: FrameBuffer
    private var blurRadius = MAX_BLUR_RADIUS

    private lateinit var pauseButton: Table
    private lateinit var pauseMenu: Table
    private lateinit var levelCompleteMenu: Table
    private lateinit var snakeDiedLabel: Label

    private val stage by lazy { Stage(uiViewport, batch).apply { Gdx.input.inputProcessor = this } }

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
        addSystem(SnakeRenderSystem(batch, assetStorage[TextureAtlasAsset.SNAKE.descriptor], viewport, shapeRenderer))
        addSystem(BatteryUIRenderSystem(this@GameScreen, batch, viewport, uiViewport, font, shapeRenderer))
    } }

    val moveSignal = Signal<Move.SnakeMove>()
    val uiPixelScale = uiViewport.worldWidth / viewport.worldWidth
    val moveHistory = Stack<Move>()

    var hasWon = false
    var snakeDead = false

    var isPaused = false
    var isFading = false
    var isShowing = true
    var fadeTime = 0f

    val currentSnakes = arrayListOf<Entity>()

    override fun show() {
        generateEntities()
        createUI()
        stage += alpha(0f) then fadeIn(0.5f)

        super.show()
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height, true)
        uiViewport.update(width, height, true)
        fboA = FrameBuffer(Pixmap.Format.RGBA8888, viewport.screenWidth, viewport.screenHeight, false, false)
        fboB = FrameBuffer(Pixmap.Format.RGBA8888, viewport.screenWidth, viewport.screenHeight, false, false)

        blurShader.use {
            blurShader.setUniformf("resolution", viewport.screenWidth.toFloat(), viewport.screenHeight.toFloat())
        }
    }

    override fun render(delta: Float) {
        if (hasWon && !levelCompleteMenu.isVisible) {
            unlockLevel(id + 1)

            levelCompleteMenu += Actions.run { levelCompleteMenu.isVisible = true } +
                    alpha(0f) +
                    moveTo(0f, -5f) +
                    (fadeIn(0.5f) / moveBy(0f, 5f, 0.5f)) +
                    Actions.run { levelCompleteMenu.touchable = Touchable.enabled }

            pauseButton += Actions.run { pauseButton.touchable = Touchable.disabled } +
                    alpha(1f) +
                    moveTo(0f, 0f) +
                    (fadeOut(0.5f) / moveBy(0f, 5f, 0.5f)) +
                    Actions.run { pauseButton.isVisible = false }
        }

        if ((!hasWon && !isPaused && !snakeDead) && blurRadius > 0f) {
            blurRadius -= delta * MAX_BLUR_RADIUS

            if (blurRadius < 0f)
                blurRadius = 0f
        } else if ((hasWon || isPaused || snakeDead) && blurRadius < MAX_BLUR_RADIUS) {
            blurRadius += delta * MAX_BLUR_RADIUS

            if (blurRadius > MAX_BLUR_RADIUS)
                blurRadius = MAX_BLUR_RADIUS
        }

        if (snakeDead && snakeSystem.checkProcessing()) {
            snakeDiedLabel.isVisible = true
            snakeSystem.setProcessing(false)
        } else if (!snakeDead && !hasWon && !isPaused && !snakeSystem.checkProcessing()) {
            snakeDiedLabel.isVisible = false
            snakeSystem.setProcessing(true)
        }

        if (!snakeDead && snakeDiedLabel.isVisible)
            snakeDiedLabel.isVisible = false

        if (isShowing) {
            fadeTime += delta * 2f
            batch.color = Color.BLACK.cpy().lerp(1f, 1f, 1f, 1f, fadeTime)

            if (fadeTime > 1) {
                batch.color = Color.WHITE.cpy()
                fadeTime = 0f
                isShowing = false
            }
        } else if (isFading) {
            fadeTime += delta * 2f
            batch.color = Color.WHITE.cpy().lerp(0f, 0f, 0f, 1f, fadeTime)
        } else {
            batch.color = color(1f, 1f, 1f)
        }

        applyBlur {
            batch.use(viewport.camera.projection) {
                batch.draw(background, viewport.worldWidth * -0.5f, viewport.worldHeight * -0.5f, viewport.worldWidth, viewport.worldHeight)
            }
            engine.update(delta)
            batch.use(uiViewport.camera.projection) {
                mapLabels.forEach { it.draw(batch, 1f) }
            }
        }

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

    override fun dispose() {
        stage.dispose()
        batch.color = Color.WHITE.cpy()
    }

    private fun applyBlur(mainRenderPass: () -> Unit) {
        fboA.begin()
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        batch.shader = ShaderProgramAsset.DEFAULT
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

        batch.shader = ShaderProgramAsset.DEFAULT
    }

    private fun createUI() {
        stage.actors {
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

            snakeDiedLabel = label("Your snake died :(\nBackspace to undo") {
                setAlignment(Align.center)
                color = color(0.9f, 0.35f, 0.1f)
                setFontScale(1.5f)
                wrap = true
                isVisible = false
                setFillParent(true)
            }

            pauseButton = table {
                defaults().expandX()
                align(Align.topRight)

                imageButton(ImageButtonSkin.PAUSE.name) { cell ->
                    cell.width(50f).height(50f)
                    cell.top().right().padTop(5f).padRight(5f)
                    onClick {
                        selectSfx.play(0.25f)
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
                alpha = 0f

                imageButton(ImageButtonSkin.PLAY.name) { cell ->
                    cell.width(100f).height(100f)
                    cell.padLeft(10f).padRight(10f)

                    onClick {
                        selectSfx.play(0.25f)
                        isPaused = false

                        rebuildActions()

                        pauseButton += showPauseButtonAction
                        pauseMenu += hidePauseMenuAction
                    }
                }

                imageButton(ImageButtonSkin.RESTART.name) { cell ->
                    cell.width(100f).height(100f)
                    cell.padLeft(10f).padRight(10f)

                    onClick {
                        selectSfx.play(0.25f)
                        while (!moveHistory.empty())
                            revertHistory()

                        isPaused = false

                        rebuildActions()

                        pauseButton += showPauseButtonAction
                        pauseMenu += hidePauseMenuAction
                    }
                }

                imageButton(ImageButtonSkin.LEVEL_SELECT.name) { cell ->
                    cell.width(100f).height(100f)
                    cell.padLeft(10f).padRight(10f)
                    onClick {
                        selectSfx.play(0.25f)
                        isFading = true
                        stage += alpha(1f) + fadeOut(0.5f) + Actions.run {
                            main.removeScreen<GameScreen>()
                            dispose()
                            main.addScreen(LevelSelectScreen(main))
                            main.setScreen<LevelSelectScreen>()
                        }
                    }
                }

                isVisible = false
                touchable = Touchable.disabled
                setFillParent(true)
                pack()
            }
            levelCompleteMenu = table {
                defaults().fillX().center()
                alpha = 0f

                if (id < TiledMapAssets.getLevelCount() - 1) {
                    imageButton(ImageButtonSkin.NEXT.name) { cell ->
                        cell.width(100f).height(100f)
                        cell.padLeft(10f).padRight(10f)

                        onClick {
                            selectSfx.play(0.25f)
                            isFading = true
                            stage += alpha(1f) + fadeOut(0.5f) + Actions.run {
                                main.removeScreen<GameScreen>()
                                dispose()
                                main.addScreen(GameScreen(main, id + 1))
                                main.setScreen<GameScreen>()
                            }
                        }
                    }
                }

                imageButton(ImageButtonSkin.LEVEL_SELECT.name) { cell ->
                    cell.width(100f).height(100f)
                    cell.padLeft(10f).padRight(10f)

                    onClick {
                        selectSfx.play(0.25f)
                        isFading = true
                        stage += alpha(1f) + fadeOut(0.5f) + Actions.run {
                            main.removeScreen<GameScreen>()
                            dispose()
                            main.addScreen(LevelSelectScreen(main))
                            main.setScreen<LevelSelectScreen>()
                        }
                    }
                }

                isVisible = false
                touchable = Touchable.disabled

                setFillParent(true)
                pack()
            }
        }
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
            is Move.SnakeDied -> {
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
                    it.containsProperty("text") -> {
                        val text = it.property("text", "sample text")
                        val rPosX = it.x / map.tileWidth.toFloat() * uiPixelScale - uiViewport.worldWidth * 0.5f
                        val rPosY = it.y / map.tileHeight.toFloat() * uiPixelScale - uiViewport.worldHeight * 0.5f
                        val rWidth = it.width / map.tileWidth.toFloat() * uiPixelScale
                        val rHeight = it.height / map.tileHeight.toFloat() * uiPixelScale

                        val label = Label(text, Scene2DSkin.defaultSkin.get(LabelSkin.MAP_TEXT.name, Label.LabelStyle::class.java)).apply {
                            setAlignment(Align.center)
                            setPosition(rPosX, rPosY)
                            setSize(rWidth, rHeight)
                            layout()
                        }
                        mapLabels += label
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
                    flipX = cell.flipHorizontally
                    flipY = cell.flipVertically
                    rotation = cell.rotation * 90f
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
    class SnakeMove(val snakeType: SnakeType, var position: Vector2, val reversed: Boolean, val lastDirection: com.gamewolves.bgj2021.ecs.systems.Facing): Move()
    class Recombination(var oldFirstSnake: Array<Vector2>, var oldSecondSnake: Array<Vector2>): Move()
    class Separation(var oldDoubleSnake: Array<Vector2>): Move()
    class ButtonChanged(val button: ButtonComponent, val pressed: Boolean): Move()
    class DoorChanged(val door: DoorComponent, val open: Boolean): Move()
    class PoweredChanged(val snake: SnakeComponent, val powered: Boolean): Move()
    class ChargeChanged(val battery: BatteryComponent, val charge: Int): Move()
    class SnakeDied(val snakeType: SnakeType, val snakeParts: Array<Vector2>, val lastDirection: com.gamewolves.bgj2021.ecs.systems.Facing): Move()
}