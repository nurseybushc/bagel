package ru.icarumbas.bagel.screens.scenes

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.utils.viewport.StretchViewport
import ru.icarumbas.bagel.RoomManager
import ru.icarumbas.bagel.utils.Mappers.Mappers.damage


class Hud(private val playerEntity: Entity,
          rm: RoomManager,
          assetManager: AssetManager){

    val stage: Stage
    val touchpad: Touchpad
    val minimap: Minimap
    val hp: RegularBar
    val mana: RegularBar

    // Temporary
    private val currentRoom: Label
    private val fps: Label

    private val uiAtlas = assetManager.get("Packs/UI.pack", TextureAtlas::class.java)

    val openButton = Image(uiAtlas.findRegion("open"))
    val attackButton = Image(uiAtlas.findRegion("attackButton"))


    init {

        val width = Gdx.graphics.width.toFloat()
        val height = Gdx.graphics.height.toFloat()

        stage = when {
            (width / height) == (16 / 10f) -> Stage(StretchViewport(800f, 480f))
            width / height == 4 / 3f -> Stage(StretchViewport(800f, 600f))
            width / height == 16 / 9f -> Stage(StretchViewport(854f, 480f))
            width / height == 3 / 2f -> Stage(StretchViewport(960f, 640f))
            else -> Stage(StretchViewport(800f, 480f))
        }

        touchpad = Touchpad(stage, uiAtlas)
        minimap = Minimap(stage, rm, assetManager, playerEntity)

        val lStyle = Label.LabelStyle(BitmapFont(), Color.RED)
        fps = Label("FPS: ", lStyle)
        currentRoom = Label("Current room: ", lStyle)

        currentRoom.setPosition(10f, 10f)
        currentRoom.setSize(stage.width/10, stage.height/10)
        stage.addActor(currentRoom)

        hp = RegularBar(
                Image(uiAtlas.findRegion("healthBar")),
                Image(uiAtlas.findRegion("barForeground")),
                Image(uiAtlas.findRegion("barBackground")),
                stage)

        hp.setSize(stage.width / 5, stage.height / 10)
        hp.setPosition(0f, stage.height - hp.height)

        mana = RegularBar(
                Image(uiAtlas.findRegion("manaBar")),
                Image(uiAtlas.findRegion("barForeground")),
                Image(uiAtlas.findRegion("barBackground")),
                stage)

        mana.setSize(stage.width / 5, stage.height / 10)
        mana.setPosition(0f, stage.height - hp.height - mana.height)

        fps.setPosition(3f, mana.posY - 15)
        stage.addActor(fps)

        attackButton.setSize(stage.width / 10, stage.width / 10)
        attackButton.setPosition(stage.width - attackButton.width - 20, stage.height/10f)
        stage.addActor(attackButton)

        openButton.setSize(stage.width / 15, stage.width / 15)
        openButton.setPosition(stage.width - openButton.width - attackButton.width * 1.5f, stage.height/10f)
        openButton.isVisible = false
        stage.addActor(openButton)

    }

    fun draw(rm: RoomManager) {
        stage.draw()
        update(rm)
    }

    private fun update(rm: RoomManager) {
        currentRoom.setText("${rm.currentMapId}")
        fps.setText("FPS: ${Gdx.graphics.framesPerSecond}")
        hp.setValue(damage[playerEntity].HP / 100f)
        touchpad.getDirection()
        minimap.update()
    }

    fun setOpenButtonVisible(visible: Boolean) {
        openButton.isVisible = visible
    }
}