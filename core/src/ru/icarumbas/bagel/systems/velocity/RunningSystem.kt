package ru.icarumbas.bagel.systems.velocity

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import ru.icarumbas.bagel.components.other.ParametersComponent
import ru.icarumbas.bagel.components.physics.BodyComponent
import ru.icarumbas.bagel.components.velocity.RunComponent
import ru.icarumbas.bagel.screens.scenes.Hud
import ru.icarumbas.bagel.utils.Mappers


class RunningSystem : IteratingSystem {

    private val body = Mappers.body
    private val run = Mappers.run
    private val params = Mappers.params
    private val hud: Hud

    constructor(hud: Hud) : super(Family.all(
            RunComponent::class.java,
            BodyComponent::class.java,
            ParametersComponent::class.java).get()) {
        this.hud = hud
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        if (Math.abs(body[entity].body.linearVelocity.x) < params[entity].maxSpeed) {
            if (Mappers.player.has(entity)) {
                if (hud.isRightPressed()) {
                    applyImpulse(body[entity].body, params[entity].acceleration, 0f)
                    run[entity].lastRight = true
                }
                if (hud.isLeftPressed()) {
                    applyImpulse(body[entity].body, -params[entity].acceleration, 0f)
                    run[entity].lastRight = false
                }
            }
        }
    }

    fun applyImpulse(body: Body, speedX: Float, speedY: Float) =
            body.applyLinearImpulse(Vector2(speedX, speedY), body.worldCenter, true)
}