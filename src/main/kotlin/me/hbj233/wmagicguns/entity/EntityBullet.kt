package me.hbj233.wmagicguns.entity

import cn.nukkit.Player
import cn.nukkit.entity.Entity
import cn.nukkit.entity.data.FloatEntityData
import cn.nukkit.level.Location
import cn.nukkit.level.format.FullChunk
import cn.nukkit.level.particle.DustParticle
import cn.nukkit.math.NukkitMath
import cn.nukkit.math.NukkitRandom
import cn.nukkit.nbt.tag.CompoundTag
import cn.nukkit.nbt.tag.DoubleTag
import cn.nukkit.nbt.tag.FloatTag
import cn.nukkit.nbt.tag.ListTag
import cn.nukkit.network.protocol.AddEntityPacket

/**
 * MagicGun for Nukkit by boybook
 */
class EntityBullet(chunk: FullChunk?, nbt: CompoundTag?, shootingEntity: Entity? = null) : EntityBulletProjectile(chunk, nbt, shootingEntity) {
    var damage1 = 5.0
    override fun getNetworkId(): Int {
        return NETWORK_ID
    }

    override fun getWidth(): Float {
        return 0.2f
    }

    override fun getLength(): Float {
        return 0.2f
    }

    override fun getHeight(): Float {
        return 0.2f
    }

    public override fun getGravity(): Float {
        return 0.001f
    }

    public override fun getDrag(): Float {
        return 0.01f
    }

    public override fun getDamage(): Double {
        return this.damage1
    }

    fun setDamage(damage: Double): EntityBullet {
        this.damage1 = damage
        return this
    }

    constructor(loc: Location, shootingEntity: Entity?, speed: Double) : this(loc.getLevel().getChunk(loc.x.toInt() shr 4, loc.z.toInt() shr 4),
            CompoundTag()
                    .putList(ListTag<DoubleTag>("Pos")
                            .add(DoubleTag("", loc.x))
                            .add(DoubleTag("", loc.y))
                            .add(DoubleTag("", loc.z)))
                    .putList(ListTag<DoubleTag>("Motion")
                            .add(DoubleTag("", -Math.sin(loc.yaw / 180 * Math.PI) * Math.cos(loc.pitch / 180 * Math.PI) * speed))
                            .add(DoubleTag("", -Math.sin(loc.pitch / 180 * Math.PI) * speed))
                            .add(DoubleTag("", Math.cos(loc.yaw / 180 * Math.PI) * Math.cos(loc.pitch / 180 * Math.PI) * speed)))
                    .putList(ListTag<FloatTag>("Rotation")
                            .add(FloatTag("", loc.yaw.toFloat()))
                            .add(FloatTag("", loc.pitch.toFloat()))),
            shootingEntity)

/*    override fun canCollideWith(entity: Entity): Boolean { //重要, 用于分队中可穿透玩家身体
        return super.canCollideWith(entity)
    }*/

    override fun onUpdate(currentTick: Int): Boolean {
        if (closed) {
            return false
        }
        timing.startTiming()
        var hasUpdate = super.onUpdate(currentTick)
        if (!hadCollision) {
            val random = NukkitRandom()
            level.addParticle(DustParticle(this.add(
                    this.width / 2 + NukkitMath.randomRange(random, -100, 100).toDouble() / 500,
                    this.height / 2 + NukkitMath.randomRange(random, -100, 100).toDouble() / 500,
                    this.width / 2 + NukkitMath.randomRange(random, -100, 100).toDouble() / 500),
                    220, 220, 220, 255))
        } else {
            kill()
        }
        if (age > 40) {
            kill()
            hasUpdate = true
        }
        timing.stopTiming()
        return hasUpdate
    }

    override fun spawnTo(player: Player) {
        val pk = AddEntityPacket()
        pk.type = this.networkId
        pk.entityRuntimeId = getId()
        pk.entityUniqueId = getId()
        pk.x = x.toFloat()
        pk.y = y.toFloat()
        pk.z = z.toFloat()
        pk.speedX = motionX.toFloat()
        pk.speedY = motionY.toFloat()
        pk.speedZ = motionZ.toFloat()
        pk.metadata = dataProperties
        player.dataPacket(pk)
        super.spawnTo(player)
    }

    companion object {
        const val NETWORK_ID = 77
    }

    init {
        this.setDataProperty(FloatEntityData(Entity.DATA_SCALE, 0.05f))
    }
}