package me.hbj233.wmagicguns.keep

import cn.nukkit.Player
import me.hbj233.wmagicguns.WMagicGunsPlugin
import java.util.*

class TickButtonKeeperRunnable : Runnable {
    override fun run() {
        val remove: MutableList<Player> = ArrayList()
        for (keeper in ArrayList(WMagicGunsPlugin.instance.getButtonKeepers().values)) {
            if (!keeper.player.isOnline) {
                remove.add(keeper.player)
            } else {
                keeper.onTick()
            }
        }
        for (player in remove) {
            WMagicGunsPlugin.instance.removeButtonKeeper(player)
        }
    }
}