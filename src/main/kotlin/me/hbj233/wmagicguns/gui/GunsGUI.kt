package me.hbj233.wmagicguns.gui

import cn.nukkit.Player
import cn.nukkit.form.window.FormWindow
import cn.nukkit.item.Item
import me.hbj233.wmagicguns.WMagicGunsPlugin
import me.hbj233.wmagicguns.command.color
import me.hbj233.wmagicguns.data.GunsData
import moe.him188.gui.utils.Backable
import moe.him188.gui.window.ResponsibleFormWindowSimple

internal class GunsGUI : ResponsibleFormWindowSimple("WMG主面板", "&e&l您需要进行什么操作?".color()) {

    init {
        addButton("枪械配置") { player -> player.showFormWindow(GunsConfigGUI(this))}
        addButton("给予物品") { player -> player.showFormWindow(GunsGiveGUI(this))}
    }
}

class GunsConfigGUI(parent: FormWindow): ResponsibleFormWindowSimple("WMG 配置面板", "&e&l请选择一个配置以修改或添加一个配置".color()), Backable {

    init {
        setParent(parent)
        this.addButton("新建配置") { player -> player.showFormWindow(GunsSettingsGUI(this, GunsData("新建枪械",Item.FISHING_ROD,20,0.5F,false,114154,
                0.01F,8.0F,5.0F,0, Item.SEEDS ,1.2F,0.5F,
                1.3F,1.4F,1.5F,12F,"","",64,64,64,
                1.0F,2.0F,true, 10, 1), "newGun")) }
        WMagicGunsPlugin.instance.gunsConfig.gunsData.forEach { (gunId, gunData) ->
            this.addButton(gunId) { player -> player.showFormWindow(GunsSettingsGUI(this, gunData, gunId)) }
        }
        this.addButton("返回") { player -> goBack(player) }
    }

    override fun onClosed(player: Player) {
        goBack(player)
    }

}