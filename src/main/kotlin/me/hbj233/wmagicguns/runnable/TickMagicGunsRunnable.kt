package me.hbj233.wmagicguns.runnable

import cn.nukkit.Server
import me.hbj233.wmagicguns.WMagicGunsPlugin
import me.hbj233.wmagicguns.guns.WMagicGuns
import java.util.*

class TickMagicGunsRunnable(private val plugin: WMagicGunsPlugin) : Runnable {

    override fun run() {
        for (gun in ArrayList<WMagicGuns>(WMagicGunsPlugin.getMagicGuns(plugin).values)) {
            gun.onTick(Server.getInstance().tick)
        }
    }

}
