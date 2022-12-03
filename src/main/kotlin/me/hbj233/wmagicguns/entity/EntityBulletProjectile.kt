package me.hbj233.wmagicguns.entity

import cn.nukkit.Player
import cn.nukkit.entity.Entity
import cn.nukkit.entity.data.LongEntityData
import cn.nukkit.entity.projectile.EntityProjectile
import cn.nukkit.event.entity.*
import cn.nukkit.level.MovingObjectPosition
import cn.nukkit.level.format.FullChunk
import cn.nukkit.math.Vector3
import cn.nukkit.nbt.tag.CompoundTag

abstract class EntityBulletProjectile(chunk: FullChunk?, nbt: CompoundTag?, shootEntity: Entity? = null) : EntityProjectile(chunk, nbt) {

    var shootEntity: Entity? = null

    override fun getDamage(): Double {
        return 5.0
    }

    protected val headDamage: Double
        get() = 20.0


    var hadCollision1 = false

    override fun onUpdate(currentTick: Int): Boolean {
        if (closed) {
            return false
        }
        val tickDiff = currentTick - lastUpdate
        if (tickDiff <= 0 && !justCreated) {
            return true
        }
        lastUpdate = currentTick
        var hasUpdate = this.entityBaseTick(tickDiff)
        if (this.isAlive) {
            var movingObjectPosition: MovingObjectPosition? = null
            if (!isCollided) {
                motionY -= this.gravity.toDouble()
            }
            val moveVector = Vector3(x + motionX, y + motionY, z + motionZ)
            val list = getLevel().getCollidingEntities(boundingBox.addCoord(motionX, motionY, motionZ).expand(1.0, 1.0, 1.0), this)
            var nearDistance = Int.MAX_VALUE.toDouble()
            var nearEntity: Entity? = null
            for (entity in list) {
                if (entity === shootEntity && ticksLived < 5) {
                    continue
                }
                val axisalignedbb = entity.boundingBox.grow(0.2, 0.2, 0.2)
                val ob = axisalignedbb.calculateIntercept(this, moveVector) ?: continue
                val distance = distanceSquared(ob.hitVector)
                if (distance < nearDistance) {
                    nearDistance = distance
                    nearEntity = entity
                }
            }
            if (nearEntity != null) {
                movingObjectPosition = getMovingObjectPositionFromEntity(this, nearEntity)
            }
            if (movingObjectPosition != null) {
                if (movingObjectPosition.entityHit != null) {
                    var hitEvent: ProjectileHitEvent? = null
                    server.pluginManager.callEvent(ProjectileHitEvent(this, movingObjectPosition).also { hitEvent = it })
                    if (!hitEvent?.isCancelled!!) {
                        movingObjectPosition = hitEvent?.movingObjectPosition
                        val damage = if (isHeadShoot(movingObjectPosition)) headDamage else getDamage()
                        val ev: EntityDamageEvent
                        ev = if (this.shootEntity == null) {
                            EntityDamageByEntityEvent(this, movingObjectPosition?.entityHit, EntityDamageEvent.DamageCause.PROJECTILE, damage.toFloat())
                        } else {
                            EntityDamageByChildEntityEvent(this.shootEntity, this, movingObjectPosition?.entityHit, EntityDamageEvent.DamageCause.PROJECTILE, damage.toFloat())
                        }
                        movingObjectPosition?.entityHit?.attack(ev)
                        ev.entity.motion = Vector3(0.0, (-2).toDouble(), 0.0)
                        if (!ev.isCancelled && ev is EntityDamageByChildEntityEvent && this.shootEntity is Player) {
                            ev.getEntity().noDamageTicks = 0
                        }
                        //this.hadCollision1 = true;
                        if (fireTicks > 0) {
                            val ev2 = EntityCombustByEntityEvent(this, movingObjectPosition?.entityHit, 5)
                            server.pluginManager.callEvent(ev2)
                            if (!ev2.isCancelled) {
                                movingObjectPosition?.entityHit?.setOnFire(ev2.duration)
                            }
                        }
                        kill()
                        return true
                    }
                }
            }
            var move = move(motionX, motionY, motionZ)
            if (isCollided || this.hadCollision1) {
                this.hadCollision1 = true
                motionX = 0.0
                motionY = 0.0
                motionZ = 0.0
                hasUpdate = false
                kill()
            }
            if (!onGround || Math.abs(motionX) > 0.00001 || Math.abs(motionY) > 0.00001 || Math.abs(motionZ) > 0.00001) {
                val f = Math.sqrt(motionX * motionX + motionZ * motionZ)
                yaw = Math.atan2(motionX, motionZ) * 180 / Math.PI
                pitch = Math.atan2(motionY, f) * 180 / Math.PI
                hasUpdate = true
            }
            updateMovement()
        }
        return hasUpdate
    }

    fun isHeadShoot(obj: MovingObjectPosition?): Boolean {
        if (this.shootEntity != null && this.shootEntity?.distanceSquared(this)!! <= 2.25) return false
        if (obj!!.typeOfHit == 1) {
            if (obj.entityHit is Player) {
                val player = obj.entityHit as Player
                val hitVector = obj.hitVector
                if (hitVector.y >= player.getY() + player.eyeHeight - 0.1 &&
                        hitVector.y <= player.getY() + player.eyeHeight + 0.1) {
                    return true
                }
            }
        }
        return false
    }

    companion object {
        const val DATA_SHOOTER_ID = 17
        fun getMovingObjectPositionFromEntity(projectile: EntityProjectile, entity: Entity?): MovingObjectPosition {
            val objectPosition = MovingObjectPosition()
            objectPosition.typeOfHit = 1
            objectPosition.entityHit = entity
            objectPosition.hitVector = Vector3(projectile.x, projectile.y, projectile.z)
            return objectPosition
        }
    }

    init {
        this.shootEntity = shootEntity
        if (shootEntity != null) {
            this.setDataProperty(LongEntityData(DATA_SHOOTER_ID, shootEntity.id))
        }
        this.setImmobile()
    }
}