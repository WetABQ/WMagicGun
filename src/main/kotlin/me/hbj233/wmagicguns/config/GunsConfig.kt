package me.hbj233.wmagicguns.config

import cn.nukkit.item.Item
import me.hbj233.wmagicguns.WMagicGunsPlugin
import me.hbj233.wmagicguns.data.GunsData
import me.hbj233.wmagicguns.guns.ConfigMagicGuns

/**
 * QuickShop
 *
 * @author WetABQ Copyright (c) 2018.09
 * @version 1.0
 */
class GunsConfig : WMagicGunsConfig("guns") {

    var gunsData = hashMapOf<String, GunsData>()

    init {
        init()
    }

    override fun init() {
        if (!isEmpty()) {
            try {
/*                if (!configSection.containsKey("guns")){
                    spawnDefaultConfig()
                }*/
                val gunsMapString = configSection["guns"] as Map<*, *>
                gunsMapString.forEach { (k, value) ->
                    val v = value as Map<*,*>
                    gunsData[k.toString()] = GunsData(
                            v["name"].toString(),
                            v["itemId"].toString().toInt(),
                            v["maxBullets"].toString().toInt(),
                            v["reloadSpeed"].toString().toFloat(),v["infiniteBullet"].toString().toBoolean(),
                            v["price"].toString().toInt(),
                            v["fireSpeed"].toString().toFloat(),
                            v["criticalDamage"].toString().toFloat(),
                            v["generalDamage"].toString().toFloat(),
                            v["bulletType"].toString().toInt(),
                            v["bindingBulletId"].toString().toInt(),
                            v["standRecoil"].toString().toFloat(),v["sneakRecoil"].toString().toFloat(), v["walkRecoil"]!!.toString().toFloat(), v["sprintRecoil"].toString().toFloat(), v["jumpRecoil"].toString().toFloat(), v["recoilMaxOffset"].toString().toFloat(),
                            v["muzzleParticle"].toString(), v["hitParticle"].toString(),
                            v["particleR"].toString().toInt(),
                            v["particleG"].toString().toInt(),
                            v["particleB"].toString().toInt(),
                            v["moveBallisticOffset"].toString().toFloat(),
                            v["jumpBallisticOffset"].toString().toFloat(),
                            v["intermittent"].toString().toBoolean(),
                            v["queueFireCount"].toString().toInt(),
                            v["fireEachTicks"].toString().toInt()
                    )
                }
                gunsData.forEach{(_,v) ->
                    WMagicGunsPlugin.instance.addMagicGuns(ConfigMagicGuns(v))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                WMagicGunsPlugin.instance.logger.error("WMagicGunsPlugin Config(shop.yml) while loading config")
            }

        } else {
            spawnDefaultConfig()
        }
    }

    override fun spawnDefaultConfig() {
        if (isEmpty()) {
            configSection["guns"] = hashMapOf<String,Map<String,String>>()
            spawnDefaultGunsConfig()
        }
        init()
        save()
    }

    private fun spawnDefaultGunsConfig(){
        addGuns("Example",GunsData("Example",Item.FISHING_ROD,20,0.5F,false,114154,
                0.5F,8.0F,5.0F,0, Item.SEEDS,1.2F,0.5F,
                1.3F,1.4F,1.5F,12F,"","",128,64,32,
                1.0F,2.0F,true, 5, 1))

        save()
    }

    fun addGuns(gunId: String, gunData: GunsData) {
        gunsData[gunId] = gunData
    }

    override fun save() {
        if (!isEmpty()) {
            try {
                configSection.clear()
                val gunsMapString = hashMapOf<String,Map<String,*>>()
                gunsData.forEach{ (k, v) -> gunsMapString[k] = v.toMap()}
                configSection["guns"] = gunsMapString
                config.setAll(configSection)
                config.save()
            } catch (e: Exception) {
                e.printStackTrace()
                WMagicGunsPlugin.instance.logger.error("Shop Config(shop.yml) has an error while saving config")
            }

        } else {
            spawnDefaultConfig()
        }
    }

}