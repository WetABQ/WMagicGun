package me.hbj233.wmagicguns.event

import cn.nukkit.entity.Entity
import cn.nukkit.event.entity.EntityDamageByEntityEvent

class EntityDamageByWMagicGunEvent : EntityDamageByEntityEvent {

    private lateinit var hitPoint: HitPoint

    constructor(damager: Entity?, entity: Entity?, cause: DamageCause?, damage: Float?, knockBack: Float?, hitPoint: HitPoint?) : super(damager, entity, cause, damage!!, knockBack!!) {
        if (hitPoint != null) {
            this.hitPoint = hitPoint
        }
    }

    constructor(damager: Entity?, entity: Entity?, cause: DamageCause?, modifiers: Map<DamageModifier?, Float?>?, knockBack: Float, hitPoint: HitPoint) : super(damager, entity, cause, modifiers, knockBack) {
        this.hitPoint = hitPoint
    }

    enum class HitPoint {
        BODY, HEAD
    }

}