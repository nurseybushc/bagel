package ru.icarumbas.bagel.engine.systems.velocity

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.math.Vector2
import ru.icarumbas.bagel.engine.components.velocity.JumpComponent
import ru.icarumbas.bagel.engine.controller.PlayerController
import ru.icarumbas.bagel.engine.systems.other.StateSystem
import ru.icarumbas.bagel.engine.world.RoomWorld
import ru.icarumbas.bagel.utils.Mappers
import ru.icarumbas.bagel.utils.inView


class JumpingSystem : IteratingSystem {

    private val playerController: PlayerController
    private val rm: RoomWorld

    constructor(playerController: PlayerController, rm: RoomWorld) : super(Family.all(JumpComponent::class.java).get()) {
        this.playerController = playerController
        this.rm = rm
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        if (entity.inView(rm)) {
            if (jump[entity].maxJumps > jump[entity].jumps && body[entity].body.linearVelocity.y < 3.5f) {
                if (Mappers.player.has(entity) && playerController.isUpPressed()) {
                    jump(entity)
                }
            }

            relax(entity)
        }
    }

    //TODO("Entities except player climb on walls")

    private fun relax(e: Entity){
        // Here i took only player's situation

        if (state[e].currentState != StateSystem.JUMPING && state[e].currentState != StateSystem.JUMP_ATTACKING) {
            if (player.has(e)) {
                if (!playerController.isUpPressed() || !player[e].collidingWithGround){
                    jump[e].jumps = 0
                }
            } else {
                jump[e].jumps = 0
            }
        }
    }

    private fun jump(e: Entity) {
        if (jump[e].jumps == 0)
            body[e].body.applyLinearImpulse(Vector2(0f, jump[e].jumpVelocity), body[e].body.worldCenter, true)
        else
            body[e].body.applyLinearImpulse(Vector2(0f, jump[e].jumpVelocity / 2), body[e].body.worldCenter, true)

        jump[e].jumps++
    }
}