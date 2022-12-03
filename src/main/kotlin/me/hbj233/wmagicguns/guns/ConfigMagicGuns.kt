package me.hbj233.wmagicguns.guns

import cn.nukkit.Player
import cn.nukkit.item.Item
import cn.nukkit.level.Location
import me.hbj233.wmagicguns.WMagicGunsPlugin.Companion.instance
import me.hbj233.wmagicguns.command.color
import me.hbj233.wmagicguns.data.GunsData
import me.hbj233.wmagicguns.entity.EntityFireball

class ConfigMagicGuns(g: GunsData) : QueueFireGun(get(g.itemId), g.maxBullets,g.name) {
    override var munitionId = g.name
    override var gunId = g.name
    override var maxBullet = g.maxBullets
    override var reloadSpeed = g.reloadSpeed
    override var infiniteBullet = g.infiniteBullet
    override var price = g.price
    override var fireSpeed = g.fireSpeed
    override var criticalDamage = g.criticalDamage
    override var generalDamage = g.generalDamage
    override var bulletType = g.bulletType
    override var bindingBullet: Item = get(g.bindingBulletId)
    override var standRecoil = g.standRecoil
    override var sneakRecoil = g.sneakRecoil
    override var walkRecoil = g.walkRecoil
    override var sprintRecoil = g.sprintRecoil
    override var jumpRecoil = g.jumpRecoil
    override var recoilMaxOffset = g.recoilMaxOffset
    //override var muzzleParticle = g.muzzleParticle
    //override var hitParticle = g.hitParticle
    override var particleR = g.particleR
    override var particleG = g.particleG
    override var particleB = g.particleB
    override var moveBallisticOffset = g.moveBallisticOffset
    override var jumpBallisticOffset = g.jumpBallisticOffset
    override var intermittent = g.intermittent
    override var queueFireCount: Int = g.queueFireCount
    override val fireEachTicks: Int = g.fireEachTicks

    private var displayBullet : String = ""
        get() {
            if(infiniteBullet){
                 field = "&4无限&r".color()
            } else {//子弹数量条
                 field = ""
                 repeat(getCount()){
                     field += "&r&e&l|"
                 }
                 repeat(maxBullet-getCount()){
                     field += "&r&3&l|"
                 }
            }
            return field
        }

    private var displayFireSpeed : String = ""
        get() {
            field = "&e" + String.format("%.2f",(1000 / fireSpeed)).toString() + "&r&1/&r&6s"
            return field
        }

    private var displayRecoilOffset : String = ""
        get() {
            field = "&e" + String.format("%.2f",recoilOffset).toString()
            return field
        }

    override fun fire(player: Player?){
        if (this.bulletType == 0){
            //player.getLevel().addSound(new DoorBumpSound(player, 0.1f));
            gunPlaySound(player!!, "m249")
            this.getDirectionVector(player.getYaw(), player.getPitch(), player.speed, player.inAirTicks)?.let {
                this.mathFire(player, it)
            }
        }
        if (this.bulletType == 1 && player != null) {
            queueFireCount = 1
            val loc = Location.fromObject(player, player.getLevel(), player.getYaw(), player.getPitch())
            loc.y += player.eyeHeight.toDouble()* 2
            loc.add(player.directionVector?.multiply(1.5))
            //player.getLevel().addSound(new FizzSound(loc));
            //player.getLevel().addSound(new FizzSound(loc));
            EntityFireball(loc, player, 2.0).spawnToAll()
        }
    }

    override fun onTick(tick: Int) {
        super.onTick(tick)
        /*if(tick % 1 == 0)*/ if (bindingPlayer?.inventory?.itemInHand?.namedTag?.getString("gunId") == gunId){
            bindingPlayer?.sendPopup((
                    "&e&l$gunId" +
                    "\n&r&e弹药:$displayBullet" +
//                        ", &r&e射速:$displayFireSpeed" +
                    "\n&r&e伤害:$generalDamage&r&1/&r&4$criticalDamage"
//                        +
//                        ", &r&e连射:$queueFireCount&r&1/&r&e$fireEachTicks" +
//                        "\n&r&e后坐力:$sneakRecoil&r&1/&r&e$standRecoil&r&1/&r&e$walkRecoil&r&1/&r&e$sprintRecoil&r&1/&r&e$jumpRecoil&r&1/&r&6$recoilMaxOffset" +
//                        "\n&r&e当前后坐力:$displayRecoilOffset" +
//                        "\n&r&e弹道偏移:$moveBallisticOffset&r&1/&r&6$jumpBallisticOffset"
                    ).color())
        }
    }

    init {
        instance.addMagicGuns(this)
    }
}
