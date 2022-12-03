package me.hbj233.wmagicguns.guns

import cn.nukkit.Player
import cn.nukkit.Server
import cn.nukkit.block.Block
import cn.nukkit.entity.Entity
import cn.nukkit.event.entity.EntityDamageEvent
import cn.nukkit.item.Item
import cn.nukkit.level.Position
import cn.nukkit.level.particle.DestroyBlockParticle
import cn.nukkit.level.particle.DustParticle
import cn.nukkit.level.particle.HeartParticle
import cn.nukkit.level.particle.Particle
import cn.nukkit.math.NukkitMath
import cn.nukkit.math.Vector3
import cn.nukkit.nbt.tag.CompoundTag
import cn.nukkit.network.protocol.PlaySoundPacket
import me.hbj233.wmagicguns.Munition
import me.hbj233.wmagicguns.WMagicGunsPlugin
import me.hbj233.wmagicguns.event.EntityDamageByWMagicGunEvent
import me.hbj233.wmagicguns.utils.EntityIterator
import me.hbj233.wmagicguns.utils.EntityIteratorOverflowException
import java.lang.Math.cos
import java.lang.Math.sin
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.floor

abstract class WMagicGuns(item: Item, open var maxBullet: Int, name: String) : Munition(item.id, item.damage, item.count, name) {

    open lateinit var gunId: String
    open var price: Int = 1000

    open var fireSpeed: Float = 1.0F //开火的冷却时间
    var nextAllow: Long = System.currentTimeMillis()

    val needButtonKeeper = true

    open var particleR = 64
    open var particleG = 64
    open var particleB = 64

    open var moveBallisticOffset: Float = 2.0F
    open var jumpBallisticOffset: Float = 4.0F
    private var random = Random()

    open var criticalDamage: Float = 6.0F
    open var generalDamage: Float = 2.0F
    protected var  shootDamage: Float = 0.0F
    open var bulletType: Int = 0

    open var reloading = false //是否正在换弹

    open var intermittent = false //能否打断换弹
    open var reloadSpeed: Float = 1.0F //每Tick装弹数量
    private var reloadingBulletCount: Double = 0.0

    var bindingPlayer: Player? = null
    private var bindingIndex = 0

    open var sneakRecoil: Float = 0.1F
    open var standRecoil: Float = 0.2F
    open var walkRecoil: Float = 1.0F
    open var sprintRecoil: Float = 1.75F
    open var jumpRecoil: Float = 2.5F
    open var recoilMaxOffset: Float = 12.0F //最大后坐力偏移
    protected var recoilOffset = 0.0 //目前的后坐力向上偏移

    open var bindingBullet: Item = get(SEEDS) //绑定的子弹物品，默认种子
    open var infiniteBullet = false //如果为true，使用绑定的物品换弹时不减少对应物品

    init {
        this.customName = name
        WMagicGunsPlugin.getMagicGuns(WMagicGunsPlugin.instance)[gunId] = this
    }

    open fun saveGunId(gunId: String) {
        this.namedTag.putString("gunId", gunId)
    }

    open fun setReloading() {
        this.setReloading(true)
    }

    open fun setReloading(reloading: Boolean): WMagicGuns {
        if (!this.reloading) {
            reloadingBulletCount = (if (getCount() == 1) 0 else getCount()).toDouble()
            if (getItemDurability() > 0) damage = 0
        }
        this.reloading = reloading
        return this
    }

    /*   private fun getGunId(): String {
         if (gunId < 0.toString()) {
              if (!this.namedTag.exist("GunId")) saveGunId(WMagicGunsPlugin.getNextGunId())
              gunId = this.namedTag.getString("GunId")
          }
        return gunId
    }*/

    open fun getItemDurability(): Int {
        return 0
    }

    open fun bindingToPlayer(player: Player, index: Int) {
        bindingPlayer = player
        bindingIndex = index
    }

    open fun getShootDamage(hitPoint: EntityDamageByWMagicGunEvent.HitPoint): Float {
        return if (hitPoint == EntityDamageByWMagicGunEvent.HitPoint.HEAD) criticalDamage else if (hitPoint == EntityDamageByWMagicGunEvent.HitPoint.BODY) generalDamage else 0F
    }

    class MathFireEvent(var player: Player, var direction: Vector3, var particle: Particle)

    open fun realFire(player: Player) {
        if (this.reloading && this.intermittent && getCount() > 1) {
            this.setReloading(false)
        } else if (this.reloading) { //没子弹啦！
            player.sendPopup("&a没子弹啦！")  //没子弹啦！
            playSound(player, "mob.armor_stand.break")
            return
        }
        if (System.currentTimeMillis() >= this.nextAllow) {
            fire(player)
            if (!player.onGround) {
                this.recoilOffset += jumpRecoil
            } else if (player.isSprinting) {
                this.recoilOffset += sprintRecoil
            } else if (player.movementSpeed == 0F) {
                this.recoilOffset += standRecoil
            } else if (player.isSneaking) {
                this.recoilOffset += sneakRecoil
            } else {
                this.recoilOffset += walkRecoil
            }
            val count = getCount() - 1
            if (count > 0) {
                setCount(count)
            } else {
                this.setReloading(true)
            }
            this.nextAllow = (System.currentTimeMillis() + this.fireSpeed).toLong()
        }
    }

    open fun mathFire(player: Player, direction: Vector3) {
        val event = MathFireEvent(player, direction, DustParticle(Vector3(),particleR,particleG,particleB))
        // this.getAccessories().values().forEach(a -> a.onMathFire(event));
        event.player.let {
            this.mathFire(it, event.direction, event.particle)
        }
    }

    open fun mathFire(player: Player, direction: Vector3, particle: Particle) {
        val pos = Vector3(player.getX(), player.getY() + player.eyeHeight - if (player.isSneaking) 0.14 else if (player.isSwimming) 1.5 else 0.0, player.getZ())
        val iterator = EntityIterator(player.getLevel(), pos, direction)
        iterator.setParticle(particle)
        var max = 50000
        while (iterator.hasNext()) {
            val entity: Entity? = iterator.next()
            if (entity != player) {
                val hitPoint: EntityDamageByWMagicGunEvent.HitPoint? = iterator.currentPosition.let { this.getHitPoint(it, entity) }
                val damage: Float? = hitPoint?.let { this.getShootDamage(hitPoint = it) }
                val ev: EntityDamageEvent = EntityDamageByWMagicGunEvent(player, entity, EntityDamageEvent.DamageCause.PROJECTILE, damage, 0.0F, hitPoint)
                entity?.attack(ev)
                entity?.level?.addParticle(DestroyBlockParticle(entity, Block.get(Block.REDSTONE_BLOCK)))
                ev.entity.motion = Vector3(0.0, (-2).toDouble(), 0.0)
                if (!ev.isCancelled) {
                    ev.entity.noDamageTicks = 0
                    //player.getLevel().addSound(new ExperienceOrbSound(player.getPosition().add(0, player.getEyeHeight(), 0)), player);
                }
            }
            if (max-- <= 0) {
                Server.getInstance().logger.logException(EntityIteratorOverflowException()) //DEBUG 防止死循环并寻找问题
                break
            }
        }

/*        if (this.canBreakBlock != null && this.canBreakBlock.size > 0) {
            try {
                val block: Array<Block> = this.getLineOfSight(player.getLevel(), pos, direction, 100, 0, null)
                val canBreak = Arrays.asList<Int>(*this.canBreakBlock)
                for (b in block) {
                    if (canBreak.contains(b.id)) {
                        b.onBreak(get(0))
                        b.getLevel().addParticle(DestroyBlockParticle(b, b))
                    }
                }
            } catch (e: IllegalStateException) { //ignore
            }
        }*/
    }

    private fun getHitPoint(bullet: Vector3, entity: Entity?): EntityDamageByWMagicGunEvent.HitPoint? {
        return if (entity is Player) {
            if (bullet.y >= entity.getY() + entity.eyeHeight - 0.1 &&
                    bullet.y <= entity.getY() + entity.eyeHeight + 0.1) {
                EntityDamageByWMagicGunEvent.HitPoint.HEAD
            } else {
                EntityDamageByWMagicGunEvent.HitPoint.BODY
            }
        } else {
            EntityDamageByWMagicGunEvent.HitPoint.BODY
        }
    }

    abstract fun fire(player: Player?)

    open fun onTick(tick: Int) {
        bindingPlayer?.let { bp ->
            if (!bp.isOnline || !bp.inventory.getItem(this.bindingIndex).equals(this, false, false)) {
                bindingPlayer = null
                this.bindingIndex = 0
            }
        }

        if (reloading) {
            if (bindingPlayer != null) {
                val inv = bindingPlayer!!.inventory
                //List<Item> bullets = inv.getContents().values().stream().filter(item -> item.equals(this.bindingBullet, false, false)).collect(Collectors.toList());
                val bullets: MutableMap<Int, Item> = HashMap()
                if (inv != null) {
                    for (index in 0 until inv.size) {
                        lateinit var item: Item
                        if (inv.getItem(index).also { item = it }.equals(bindingBullet, false, false)) {
                            item.let {bullets[index] = it}
                        }
                    }
                }
                var count = 0
                for (item in bullets.values) {
                    count += item.getCount()
                }
                if (count >= (if (this.reloadSpeed < 1) 1 else this.reloadSpeed.toInt())) { //是否已经正在换弹并且还有子弹进行下一tick的换弹
                    val old: Double = this.reloadingBulletCount
                    this.reloadingBulletCount += this.reloadSpeed
                    if (!infiniteBullet) {
                        var change = floor(this.reloadingBulletCount) - Math.floor(old)
                        if (change > 0) { //个位值变化，需要减去子弹物品
                            for (index in bullets.keys) {
                                val newItem = bullets[index]
                                if (change > newItem!!.getCount()) { //需要下一个物品进行减少
                                    change -= newItem.getCount().toDouble()
                                    newItem.setCount(0)
                                    inv?.setItem(index, newItem) //减少子弹数
                                } else {
                                    newItem.setCount(newItem.getCount() - change.toInt())
                                    inv?.setItem(index, newItem) //减少子弹数
                                    break
                                }
                            }
                        }
                    }
                    setCount(NukkitMath.floorDouble(this.reloadingBulletCount))
                    if (getItemDurability() > 0) {
                        val newDamage = NukkitMath.floorDouble(getItemDurability().toDouble() - this.reloadingBulletCount * getItemDurability().toDouble() / this.maxBullet.toDouble())
                        damage = newDamage
                    }
                    //if (this.bindingPlayer != null) this.bindingPlayer.getLevel().addSound(new ItemFrameItemAddedSound(this.bindingPlayer));
                    if (this.reloadingBulletCount >= this.maxBullet) {
                        setCount(this.maxBullet)
                        this.setReloading(false)
                    }
                } else if (this.reloadingBulletCount > 0) {
                    this.setReloading(false)
                    //卡在换弹状态，标记为无子弹
                }
            }
        }
        if (recoilOffset > 0) {
            recoilOffset -= 0.2
            if (recoilOffset < 0) recoilOffset = 0.0
        }
    }

    open fun getDirectionVector(yaw: Double, pitch: Double, speed: Vector3, inAirTicks: Int): Vector3? {
        var vYaw = yaw
        var vPitch = pitch
        val baseMove: Double = this.moveBallisticOffset * speed.length()
        vYaw += baseMove * this.random.nextDouble() * 2 - baseMove
        vPitch += baseMove * this.random.nextDouble() * 2 - baseMove
        val baseJump: Double = this.jumpBallisticOffset.toDouble() * (if (inAirTicks > 15) 15 else inAirTicks) / 10
        vYaw += baseJump * this.random.nextDouble() * 2 - baseJump
        vPitch += baseJump * this.random.nextDouble() * 2 - baseJump
        vPitch -= recoilOffset
        return getDirectionVector(vYaw, vPitch)
    }

    open fun getDirectionVector(yaw: Double, pitch: Double): Vector3? {
        val pitch0 = (pitch + 90) * Math.PI / 180
        val yaw0 = (yaw + 90) * Math.PI / 180
        val x = sin(pitch0) * cos(yaw0)
        val z = sin(pitch0) * sin(yaw0)
        val y = cos(pitch0)
        return Vector3(x, y, z).normalize()
    }

    override fun clone(): WMagicGuns {
        val item: WMagicGuns = super.clone() as WMagicGuns
        item.count = this.count
        item.namedTag = this.namedTag
        item.bindingPlayer = this.bindingPlayer
        item.bindingIndex = this.bindingIndex
        item.maxBullet = this.maxBullet
        item.price = this.price
        item.fireSpeed = this.fireSpeed
        item.reloadSpeed = this.reloadSpeed
        item.criticalDamage = this.criticalDamage
        item.generalDamage = this.generalDamage
        item.intermittent = this.intermittent
        item.sneakRecoil = this.sneakRecoil
        item.standRecoil = this.standRecoil
        item.walkRecoil = this.walkRecoil
        item.sprintRecoil = this.sprintRecoil
        item.jumpRecoil = this.jumpRecoil
        item.recoilMaxOffset = this.recoilMaxOffset
        item.infiniteBullet = this.infiniteBullet
        item.bindingBullet = this.bindingBullet
        return item
    }

    open fun gunPlaySound(pos: Position, sound: String?) {
        pos.getLevel().players.forEach { (id: Long?, p: Player) -> gunPlaySoundSmartAround(p, pos, sound, 1f, 1f, 200.0) }
    }

    open fun playSound(pos: Position, sound: String?) {
        pos.getLevel().players.forEach { (id: Long?, p: Player) -> playSoundSmartAround(p, pos, sound, 1f, 1f, 200.0) }
    }

    class SmartPlaySoundEvent(var player: Player, var soundPos: Vector3, var sound: String?, var maxVolume: Float, var pitch: Float, var maxDistance: Double)

    open fun gunPlaySoundSmartAround(player: Player, soundPos: Vector3, sound: String?, maxVolume: Float, pitch: Float, maxDistance: Double) {
        val event = SmartPlaySoundEvent(player, soundPos, sound, maxVolume, pitch, maxDistance)
//        this.getAccessories().values.forEach(Consumer<GunAccessory> { a: GunAccessory -> a.onPlaySoundSmartAround(event) })
        playSoundSmartAround(event.player, event.soundPos, event.sound, event.maxVolume, event.pitch, event.maxDistance)
    }

    open fun playSoundSmartAround(player: Player, soundPos: Vector3, sound: String?, maxVolume: Float, pitch: Float, maxDistance: Double) {
        val orientation = soundPos.subtract(player).normalize()
        val distance = player.distance(soundPos)
        if (distance <= maxDistance) {
            val pos = if (distance <= 5) soundPos else player.add(orientation.multiply(5.0))
            val pk = PlaySoundPacket()
            pk.name = sound
            pk.x = pos.floorX
            pk.y = pos.floorY
            pk.z = pos.floorZ
            pk.pitch = pitch
            pk.volume = (1 - distance / maxDistance).toFloat() * maxVolume
            player.dataPacket(pk)
            player.getLevel().addParticle(HeartParticle(pos), player.player)
        }
    }

    init {
        this.gunId = name
        val tag: CompoundTag = (if (!this.hasCompoundTag()) {
            CompoundTag()
        } else {
            this.namedTag
        }).also {
            it.putString("gunId", this.munitionId)
        }
        this.namedTag = tag
    }

}

