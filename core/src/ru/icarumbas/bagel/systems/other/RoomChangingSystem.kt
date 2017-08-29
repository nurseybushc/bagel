package ru.icarumbas.bagel.systems.other

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.physics.box2d.Body
import ru.icarumbas.REG_ROOM_HEIGHT
import ru.icarumbas.REG_ROOM_WIDTH
import ru.icarumbas.bagel.RoomManager
import ru.icarumbas.bagel.components.other.PlayerComponent
import ru.icarumbas.bagel.components.rendering.SizeComponent
import ru.icarumbas.bagel.utils.Mappers


class RoomChangingSystem : IteratingSystem {

    private val rm: RoomManager

    constructor(rm: RoomManager) : super(Family.all(PlayerComponent::class.java).get()) {
        this.rm = rm
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        checkRoomChange(Mappers.size[entity], Mappers.body[entity].body)
    }

    private fun checkRoomChange(pos: SizeComponent, body: Body) {

        if (body.position.x > rm.width() && body.position.y < REG_ROOM_HEIGHT)
            changeRoom("Right", 2, 1, 2, body, pos) else

        if (body.position.x < 0 && body.position.y < REG_ROOM_HEIGHT)
            changeRoom("Left", 0, 1, 0, body, pos) else

        if (body.position.y > rm.height() && body.position.x < REG_ROOM_WIDTH)
            changeRoom("Up", 0, 3, 1, body, pos) else

        if (body.position.y < 0 && body.position.x < REG_ROOM_WIDTH)
            changeRoom("Down", 0, 1, 3, body, pos) else

        if (body.position.x < 0 && body.position.y > REG_ROOM_HEIGHT)
            changeRoom("Left", 0, 3, 4, body, pos) else

        if (body.position.y < 0 && body.position.x > REG_ROOM_WIDTH)
            changeRoom("Down", 2, 1, 7, body, pos) else

        if (body.position.x > rm.width() && body.position.y > REG_ROOM_HEIGHT)
            changeRoom("Right", 2, 3, 6, body, pos) else

        if (body.position.y > rm.height() && body.position.x > REG_ROOM_WIDTH)
            changeRoom("Up", 2, 3, 5, body, pos)
    }

    private fun changeRoom(side: String, plX: Int, plY: Int, newIdLink: Int, body: Body, size: SizeComponent) {

        val newId = rm.pass(newIdLink)

        if (side == "Up" || side == "Down") {
            // Compare top-right parts of previous and current maps
            val X10 = rm.mesh(2, newId)
            val prevX = rm.mesh(plX)

            if (side == "Up") {
                if (prevX == X10) {
                    body.setTransform(rm.width(newId) - REG_ROOM_WIDTH / 2, 0f, 0f)
                } else body.setTransform(REG_ROOM_WIDTH / 2, 0f, 0f)
            }
            if (side == "Down") {
                if (prevX == X10) body.setTransform(rm.width(newId) - REG_ROOM_WIDTH / 2,
                        rm.height(newId), 0f)
                else body.setTransform(REG_ROOM_WIDTH / 2, rm.height(newId), 0f)
            }
        } else

            if (side == "Left" || side == "Right") {
                // Compare top parts of previous and current maps
                val Y11 = rm.mesh(3, newId)
                val prevY = rm.mesh(plY)

                if (side == "Left") {
                    if (prevY == Y11) body.setTransform(rm.width(newId),
                            rm.height(newId) - REG_ROOM_HEIGHT / 2 - size.height / 2, 0f)
                    else body.setTransform(rm.width(newId), REG_ROOM_HEIGHT / 2 - size.height / 2, 0f)
                }
                if (side == "Right") {
                    if (prevY == Y11)
                        body.setTransform(0f, rm.height(newId) - REG_ROOM_HEIGHT / 2 - size.height / 2, 0f)
                    else
                        body.setTransform(0f, REG_ROOM_HEIGHT / 2 - size.height / 2, 0f)
                }
            }

        rm.currentMapId = newId
    }
}