package me.hbj233.wmagicguns.gui

import cn.nukkit.Player
import cn.nukkit.Server
import cn.nukkit.form.element.ElementDropdown
import cn.nukkit.form.response.FormResponseCustom
import cn.nukkit.form.window.FormWindow
import me.hbj233.wmagicguns.WMagicGunsPlugin
import me.hbj233.wmagicguns.command.color
import me.hbj233.wmagicguns.data.GunsData
import me.hbj233.wmagicguns.guns.ConfigMagicGuns
import moe.him188.gui.window.ResponsibleFormWindowCustom

class GunsGiveGUI(parent: FormWindow) : ResponsibleFormWindowCustom("给予枪械面板") {

    init {
        setParent(parent)
        addElement(ElementDropdown("选择玩家", Server.getInstance().onlinePlayers.values
                .fold<Player, ArrayList<String>>(arrayListOf()) {
                    playerList, player -> playerList.add(player.name); playerList
                }.also {
                    it.add(0, "自己")
                }, 0))
        addElement(ElementDropdown("选择枪械", WMagicGunsPlugin.instance.gunsConfig.gunsData.keys.toList()))
    }

    override fun onClicked(response: FormResponseCustom, player: Player) {
        val targetPlayer = when(val playerName = response.getDropdownResponse(0).elementContent) {
            "自己" -> player
            else -> Server.getInstance().getPlayerExact(playerName)?:null
        }
        val targetGunData = WMagicGunsPlugin.instance.gunsConfig.gunsData[response.getDropdownResponse(1).elementContent]
        if (targetPlayer is Player) {
            if (targetGunData is GunsData) {
                targetPlayer.inventory.addItem(ConfigMagicGuns(targetGunData))
                player.sendMessage("&a成功给予玩家添加 Id 为 ${response.getDropdownResponse(1).elementContent} 的枪械".color())
            } else {
                player.sendMessage("&c找不到配置".color())
            }
        } else {
            player.sendMessage("&c找不到玩家!".color())
        }
        response.getDropdownResponse(1)
    }

    override fun onClosed(player: Player) {
        goBack(player)
    }

}