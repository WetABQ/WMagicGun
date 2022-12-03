package me.hbj233.wmagicguns.guns

import cn.nukkit.Player
import cn.nukkit.item.Item


abstract class QueueFireGun(item: Item, maxBullet: Int, name: String) : WMagicGuns(item, maxBullet, name) {

    private var fireCount = 0
    abstract val queueFireCount: Int
    abstract val fireEachTicks: Int

    override fun realFire(player: Player) { //if (this.isReloading()) return;
        val a = System.nanoTime()
        if (a >= nextAllow) {
            fireCount = this.queueFireCount
            nextAllow = a + this.fireSpeed.toLong()*1000000//MS --> NS
        }
    }

    override fun setReloading(reloading: Boolean): WMagicGuns {
        if (reloading) fireCount = 0
        return super.setReloading(reloading)
    }

    override fun onTick(tick: Int) {
        super.onTick(tick)
        if (this.bindingPlayer != null  && !this.reloading) {
            if (tick % fireEachTicks == 0) {
                if (fireCount > 0) {
                    this.nextAllow = System.currentTimeMillis()
                    super.realFire(this.bindingPlayer!!)
                    fireCount--
                }
            }
        }
    }
}