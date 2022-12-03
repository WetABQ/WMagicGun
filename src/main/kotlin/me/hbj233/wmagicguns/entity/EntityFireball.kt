package me.hbj233.wmagicguns.entity

import cn.nukkit.Player
import cn.nukkit.entity.Entity
import cn.nukkit.event.entity.EntityDamageByChildEntityEvent
import cn.nukkit.event.entity.EntityDamageEvent
import cn.nukkit.level.Location
import cn.nukkit.level.MovingObjectPosition
import cn.nukkit.level.Position
import cn.nukkit.level.Sound
import cn.nukkit.level.format.FullChunk
import cn.nukkit.level.particle.HugeExplodeSeedParticle
import cn.nukkit.math.NukkitMath
import cn.nukkit.math.SimpleAxisAlignedBB
import cn.nukkit.math.Vector3
import cn.nukkit.nbt.tag.CompoundTag
import cn.nukkit.nbt.tag.DoubleTag
import cn.nukkit.nbt.tag.FloatTag
import cn.nukkit.nbt.tag.ListTag
import cn.nukkit.network.protocol.AddEntityPacket

open class EntityFireball @JvmOverloads constructor(chunk: FullChunk?, nbt: CompoundTag?, private var shootingEntity: Player? = null) : Entity(chunk, nbt) {
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
        return 0.01f
    }

    public override fun getDrag(): Float {
        return 0.01f
    }

    private val damage: Double
        get() = 21.0

    constructor(loc: Location, shootingEntity: Player?, speed: Double) : this(loc.getLevel().getChunk(loc.x.toInt() shr 4, loc.z.toInt() shr 4),
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
            shootingEntity) {
    }

    override fun canCollideWith(entity: Entity): Boolean { //重要, 用于分队中可穿透玩家身体
        return super.canCollideWith(entity)
    }

    override fun onUpdate(currentTick: Int): Boolean {
        if (closed) {
            return false
        }
        timing.startTiming()
        val tickDiff = currentTick - lastUpdate
        if (tickDiff <= 0 && !justCreated) {
            return true
        }
        lastUpdate = currentTick
        val hasUpdate = entityBaseTick(tickDiff)
        if (isAlive) {
            val movingObjectPosition: MovingObjectPosition? = null
            if (!isCollided) {
                motionY -= this.gravity.toDouble()
            }
            val moveVector = Vector3(x + motionX, y + motionY, z + motionZ)
            val list = getLevel().getCollidingEntities(boundingBox.addCoord(motionX, motionY, motionZ).expand(1.0, 1.0, 1.0), this)
            var nearDistance = Int.MAX_VALUE.toDouble()
            var nearEntity: Entity? = null
            for (entity in list) {
                if (entity === shootingEntity && ticksLived < 5) {
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
                explode()
                kill()
            } else {
                move(motionX, motionY, motionZ)
                val friction = 1 - drag
                motionX *= friction.toDouble()
                motionY *= friction.toDouble()
                motionZ *= friction.toDouble()
                updateMovement()
                if (onGround || isCollided) {
                    explode()
                    kill()
                }
            }
        }
        timing.stopTiming()
        return hasUpdate || !onGround || Math.abs(motionX) > 0.00001 || Math.abs(motionY) > 0.00001 || Math.abs(motionZ) > 0.00001
    }

    private val source: Position = this
    fun explode() {
        val size = 4.0
        val what: Entity = this
        val explosionSize = size * 2.0
        val minX = NukkitMath.floorDouble(this.source.x - explosionSize - 1).toDouble()
        val maxX = NukkitMath.ceilDouble(this.source.x + explosionSize + 1).toDouble()
        val minY = NukkitMath.floorDouble(this.source.y - explosionSize - 1).toDouble()
        val maxY = NukkitMath.ceilDouble(this.source.y + explosionSize + 1).toDouble()
        val minZ = NukkitMath.floorDouble(this.source.z - explosionSize - 1).toDouble()
        val maxZ = NukkitMath.ceilDouble(this.source.z + explosionSize + 1).toDouble()
        val explosionBB = SimpleAxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ)
        val list = level.entities.filter { it.boundingBox.intersectsWith(explosionBB) }
        for (entity in list) {
            if (entity is Entity) {
                val distance = entity.distance(this.source) / explosionSize
                if (distance <= 1) {
                    val motion = entity.subtract(this.source).normalize()
                    val exposure = 1
                    val impact = (1 - distance) * exposure
                    entity.attack(EntityDamageByChildEntityEvent(shootingEntity, this, entity, EntityDamageEvent.DamageCause.ENTITY_EXPLOSION, (damage * impact).toFloat()))
                    entity.setMotion(motion.multiply(impact))
                }
            }
        }
        level.addParticle(HugeExplodeSeedParticle(Vector3(this.source.x, this.source.y, this.source.z)))
        level.addSound(this, Sound.RANDOM_EXPLODE)
    }

    override fun spawnTo(player: Player) {
        val pk = AddEntityPacket()
        pk.type = this.networkId
        pk.entityUniqueId = getId()
        pk.entityRuntimeId = getId()
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
        const val NETWORK_ID = 94
    }

    init {
        this.setImmobile()
    }
}