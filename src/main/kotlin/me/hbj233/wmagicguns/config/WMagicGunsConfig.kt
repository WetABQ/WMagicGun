package me.hbj233.wmagicguns.config

import cn.nukkit.utils.Config
import cn.nukkit.utils.ConfigSection
import me.hbj233.wmagicguns.WMagicGunsPlugin

/**
 * QuickShop
*
* @author WetABQ Copyright (c) 2018.09
* @version 1.0
*/
abstract class WMagicGunsConfig(configname: String) : WMagicGunsConfigInterface {
    protected var config: Config = Config(  "${WMagicGunsPlugin.instance.dataFolder}/$configname.yml", Config.YAML)
    protected var configSection: ConfigSection

    init {
        this.configSection = config.rootSection
    }

    protected abstract fun init()

    protected abstract fun spawnDefaultConfig()

    abstract override fun save()

    fun isEmpty(): Boolean {
        return configSection.isEmpty()
    }
}