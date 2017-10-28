package ru.icarumbas.bagel.systems.other

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.World
import ru.icarumbas.bagel.RoomManager
import ru.icarumbas.bagel.components.other.HealthComponent
import ru.icarumbas.bagel.utils.Mappers.Mappers.animation
import ru.icarumbas.bagel.utils.Mappers.Mappers.body
import ru.icarumbas.bagel.utils.Mappers.Mappers.damage
import ru.icarumbas.bagel.utils.Mappers.Mappers.state
import ru.icarumbas.bagel.utils.Mappers.Mappers.texture
import ru.icarumbas.bagel.utils.inView


class HealthSystem : IteratingSystem {

    private val rm: RoomManager
    private val world: World
    private val deleteList: ArrayList<Entity>
    private val coins: ArrayList<Body>

    constructor(rm: RoomManager, world: World,
                coins: ArrayList<Body>,
                deleteList: ArrayList<Entity>) : super(Family.all(
            HealthComponent::class.java).get()) {

        this.rm = rm
        this.world = world
        this.coins = coins
        this.deleteList = deleteList
    }

    override fun processEntity(e: Entity, deltaTime: Float) {
        if (e.inView(rm)) {

            damage[e].hitTimer += deltaTime

            if (damage[e].hitTimer > .1f){
                texture[e].color = Color.WHITE
            }

            if (damage[e].HP <= 0 &&
                    !(animation.has(e) && animation[e].animations.contains(StateSystem.DEAD) &&
                            !animation[e].animations[StateSystem.DEAD]?.isAnimationFinished(state[e].stateTime)!!)){
                deleteList.add(e)
            }

            if ((damage[e].damage != 0 || !damage[e].knockback.isZero)) {
                damage[e].HP -= damage[e].damage
                damage[e].damage = 0
                body[e].body.applyLinearImpulse(damage[e].knockback, body[e].body.worldCenter, true)
                damage[e].knockback.set(0f, 0f)
                damage[e].hitTimer = 0f
                texture[e].color = Color.RED
            }
        }
    }

}