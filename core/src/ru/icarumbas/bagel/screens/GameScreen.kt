package ru.icarumbas.bagel.screens

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.utils.viewport.FitViewport
import ru.icarumbas.*
import ru.icarumbas.bagel.*
import ru.icarumbas.bagel.screens.scenes.Hud
import ru.icarumbas.bagel.systems.other.HealthSystem
import ru.icarumbas.bagel.systems.other.RoomChangingSystem
import ru.icarumbas.bagel.systems.other.StateSwapSystem
import ru.icarumbas.bagel.systems.physics.AwakeSystem
import ru.icarumbas.bagel.systems.physics.ContactSystem
import ru.icarumbas.bagel.systems.physics.WeaponSystem
import ru.icarumbas.bagel.systems.rendering.AnimationSystem
import ru.icarumbas.bagel.systems.rendering.MapRenderer
import ru.icarumbas.bagel.systems.rendering.RenderingSystem
import ru.icarumbas.bagel.systems.rendering.ViewportSystem
import ru.icarumbas.bagel.systems.velocity.JumpingSystem
import ru.icarumbas.bagel.systems.velocity.RunningSystem
import ru.icarumbas.bagel.EntityCreator


class GameScreen(newWorld: Boolean, game: Bagel): ScreenAdapter() {

    private val world = World(Vector2(0f, -9.8f), true)
    private val hud = Hud()
    private val debugRenderer: DebugRenderer
    private val engine = Engine()
    private val mapRenderer: MapRenderer
    private val worldCleaner: B2DWorldCleaner
    private val entityCeator: EntityCreator
    private val rm: RoomManager

    init {

        val b2DWorldCreator = B2DWorldCreator(game.assetManager, world)
        val animationCreator = AnimationCreator(game.assetManager)
        val worldCreator = WorldCreator(game.assetManager)
        rm = RoomManager(ArrayList())

        if (newWorld) rm.createNewWorld(worldCreator, b2DWorldCreator, game.assetManager, engine) else rm.continueWorld()

        val viewport = FitViewport(REG_ROOM_WIDTH, REG_ROOM_HEIGHT, OrthographicCamera(REG_ROOM_WIDTH, REG_ROOM_HEIGHT))
        val orthoRenderer = OrthogonalTiledMapRenderer(game.assetManager.get(rm.path()), 0.01f)

        mapRenderer = MapRenderer(orthoRenderer, rm, game.assetManager, viewport)
        debugRenderer = DebugRenderer(Box2DDebugRenderer(), world, viewport)
        entityCeator = EntityCreator()

        val playerBody = b2DWorldCreator.createPlayerBody()
        val coins = ArrayList<Body>()
        val entityDeleteList = ArrayList<Entity>()
        val bodyDeleteList = ArrayList<Body>()

        worldCleaner = B2DWorldCleaner(entityDeleteList, bodyDeleteList, engine, world)

        val contactSystem = ContactSystem(hud, bodyDeleteList)
        world.setContactListener(contactSystem)

        // Other
        engine.addSystem(RoomChangingSystem(rm))
        engine.addSystem(StateSwapSystem(rm))
        engine.addSystem(HealthSystem(rm, world, coins, entityDeleteList))

        // Velocity
        engine.addSystem(RunningSystem(hud))
        engine.addSystem(JumpingSystem(hud))

        // Physic
        engine.addSystem(AwakeSystem(rm))
        engine.addSystem(contactSystem)
        engine.addSystem(WeaponSystem(hud))

        // Rendering
        engine.addSystem(AnimationSystem(rm))
        engine.addSystem(ViewportSystem(viewport, rm))
        engine.addSystem(RenderingSystem(rm, orthoRenderer.batch))

        val weaponEntityLeft = entityCeator.createSwingWeaponEntity(
                path = "Sword2",
                atlas = game.assetManager["Packs/GuyKnight.pack", TextureAtlas::class.java],
                width = 45,
                height = 200,
                playerBody = playerBody,
                b2DWorldCreator = b2DWorldCreator,
                anchorA = Vector2(-.1f, -.3f),
                anchorB = Vector2(0f, -.75f))

        val weaponEntityRight = entityCeator.createSwingWeaponEntity(
                path = "Sword2",
                atlas = game.assetManager["Packs/GuyKnight.pack", TextureAtlas::class.java],
                width = 45,
                height = 200,
                playerBody = playerBody,
                b2DWorldCreator = b2DWorldCreator,
                anchorA = Vector2(.1f, -.3f),
                anchorB = Vector2(0f, -.75f))

        engine.addEntity(weaponEntityRight)
        engine.addEntity(weaponEntityLeft)
        engine.addEntity(entityCeator.createPlayerEntity(
                animationCreator,
                game.assetManager["Packs/GuyKnight.pack", TextureAtlas::class.java],
                playerBody,
                weaponEntityLeft,
                weaponEntityRight))


        (0..TILED_MAPS_TOTAL).forEach {
            b2DWorldCreator.loadGround("Maps/Map$it.tmx", "ground", GROUND_BIT, engine)
            b2DWorldCreator.loadGround("Maps/Map$it.tmx", "platform", PLATFORM_BIT, engine)
        }

        Gdx.input.inputProcessor = hud.stage

        println("Size of rooms: ${rm.size()}")
        println("World bodies count: ${world.bodyCount}")
    }

    override fun render(delta: Float) {
        world.step(1/45f, 6, 2)
        worldCleaner.update()
        mapRenderer.render()
        engine.update(delta)
        debugRenderer.render()
        hud.draw(rm)
    }

    override fun pause() {
        /*game.worldIO.writeRoomsToJson("roomsFile.Json", rooms, false)

        with(game.worldIO.preferences) {
            putFloat("PlayerPositionX", player.playerBody.position.x)
            putFloat("PlayerPositionY", player.playerBody.position.y)
            putInteger("Money", player.money)
            putInteger("CurrentMap", currentMap)
            putInteger("HP", player.HP)
            flush()
        }

    game.screen = MainMenuScreen(game)
*/
    }

    override fun resize(width: Int, height: Int) {
        hud.stage.viewport.update(width, height, true)
    }

    override fun dispose() {
        world.dispose()
        debugRenderer.dispose()
    }

}
