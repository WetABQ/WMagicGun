package me.hbj233.wmagicguns.gui

import cn.nukkit.Player
import cn.nukkit.form.element.ElementInput
import cn.nukkit.form.element.ElementToggle
import cn.nukkit.form.response.FormResponseCustom
import cn.nukkit.form.window.FormWindow
import me.hbj233.wmagicguns.WMagicGunsPlugin
import me.hbj233.wmagicguns.command.color
import me.hbj233.wmagicguns.data.GunsData
import moe.him188.gui.window.ResponsibleFormWindowCustom
import kotlin.math.min

class GunsSettingsGUI(parent: FormWindow, gunsData: GunsData, gunsId: String) : ResponsibleFormWindowCustom("设置枪械配置") {

    init {
        setParent(parent)
        addElement(ElementInput("枪械配置Id", gunsId, gunsId))
        gunsData.toTranslateMap().forEach { (elementName, value) ->
            if (value is Boolean) {
                addElement(ElementToggle(elementName, value))
            } else {
                addElement(ElementInput(elementName, value.toString(), value.toString()))
            }
        }
    }

    override fun onClicked(response: FormResponseCustom, player: Player) {
        response.responses[0]?.let { gunsId ->
            val params = arrayListOf<Any>()
            response.responses.also { it.remove(0) }.toSortedMap(Comparator { o1, o2 -> min(o1, o2) }).values.forEach { value -> params.add(value) }
            try {
                val gunsDataClazz = GunsData::class.java
                val arrayParam = arrayOfNulls<Any>(27)
                params.fold(0) { i, param -> arrayParam[i] = autoType(param.toString()); i + 1 }
                val gunsData = gunsDataClazz.constructors[0].newInstance(*arrayParam)
                if (gunsData is GunsData && gunsId is String) {
                    WMagicGunsPlugin.instance.gunsConfig.gunsData[gunsId] = gunsData
                    WMagicGunsPlugin.instance.gunsConfig.save()
                    player.sendMessage("&a修改成功！".color())
                } else {
                    player.sendMessage("&c在通过反射获取实例化对象时出现错误".color())
                }
            } catch (e: Exception) {
                e.printStackTrace()
                player.sendMessage("&c在通过反射获取实例化对象时出现错误".color())
            }
        }
    }

    override fun onClosed(player: Player) {
        goBack(player)
    }

    private fun autoType(value: String): Any {
        return when {
            value.contains(".") -> value.toFloat()
            value.contains(Regex("(true|false)")) -> value.toBoolean()
            value.contains(Regex("-?[0-9]\\d*")) -> value.toInt()
            else -> value
        }
    }

}