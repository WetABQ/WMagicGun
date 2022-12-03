package me.hbj233.wmagicguns

import cn.nukkit.Player
import cn.nukkit.Server
import cn.nukkit.plugin.PluginBase
import me.hbj233.wmagicguns.command.WMagicGunsCommand
import me.hbj233.wmagicguns.config.GunsConfig
import me.hbj233.wmagicguns.guns.WMagicGuns
import me.hbj233.wmagicguns.keep.PlayerButtonKeeper
import me.hbj233.wmagicguns.keep.TickButtonKeeperRunnable
import me.hbj233.wmagicguns.runnable.TickMagicGunsRunnable
import java.util.*
import kotlin.collections.HashMap


class WMagicGunsPlugin : PluginBase() {

    lateinit var gunsConfig : GunsConfig
    lateinit var gunId : String

    private val buttonKeepers: HashMap<Player, PlayerButtonKeeper> = HashMap()
    var magicGuns: HashMap<String, WMagicGuns> = HashMap()

    override fun onLoad() {
        instance = this
        logger.notice("WMagicGuns for Nukkit(X) by HBJ233 $VERSION is loading" )
    }

    override fun onEnable() {

        loadConfig()
        server.pluginManager.registerEvents(EventListener(this), this)

        Server.getInstance().commandMap.register( "", WMagicGunsCommand())
        server.scheduler.scheduleRepeatingTask(TickMagicGunsRunnable(this), 1)
        server.scheduler.scheduleRepeatingTask(TickButtonKeeperRunnable(), 1)
        logger.notice("WMagicGuns Enabled! Version:$VERSION  Author:HBJ233 & WetABQ")
    }

    override fun onDisable() {
        saveAllConfig()
        logger.warning("WMagicGuns Disabled!")
    }

    private fun loadConfig() {
        gunsConfig = GunsConfig()
    }

    private fun saveAllConfig() {
        gunsConfig.save()
    }

    private val munitions: MutableMap<String, Munition> = HashMap()

    fun registerMunition(munition: Munition) {
        munition.munitionId.let { munitions.put(it, munition) }
    }

    fun addMagicGuns(wmagicGuns: WMagicGuns) {
        this.magicGuns[wmagicGuns.gunId] = wmagicGuns
    }

    fun getButtonKeepers(): Map<Player, PlayerButtonKeeper> {
        return buttonKeepers
    }

    fun addButtonKeeper(player: Player?): PlayerButtonKeeper? {
        return if (buttonKeepers.containsKey(player)) {
            buttonKeepers[player]
        } else {
            val keeper = PlayerButtonKeeper(player!!)
            toList(player, keeper)
            keeper
        }
    }

    fun removeButtonKeeper(player: Player?): Boolean {
        return if (buttonKeepers.containsKey(player)) {
            buttonKeepers.remove(player)
            //buttonKeepers.remove(player)
            true
        } else false
    }

    companion object {
        var TITLE = ""
        lateinit var instance : WMagicGunsPlugin
        var magicGuns: HashMap<Int, WMagicGuns> = HashMap()
        const val VERSION = "v1.0.0"
        private var nextGunId = 0

        fun getMagicGuns(wMagicGunsPlugin: WMagicGunsPlugin): HashMap<String, WMagicGuns> {
            return wMagicGunsPlugin.magicGuns
        }
    }
}

private fun <K, V> toList(player: K, keeper: V) {

}
