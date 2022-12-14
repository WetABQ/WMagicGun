package me.hbj233.wmagicguns.command

import cn.nukkit.Player
import cn.nukkit.Server
import cn.nukkit.command.Command
import cn.nukkit.command.CommandSender
import cn.nukkit.command.data.CommandParameter
import cn.nukkit.utils.TextFormat
import me.hbj233.wmagicguns.WMagicGunsPlugin
import me.hbj233.wmagicguns.gui.GunsGUI
import me.hbj233.wmagicguns.guns.ConfigMagicGuns
import java.util.*

fun String.color() : String = TextFormat.colorize(this)

class WMagicGunsCommand : Command("wmg") {

    init {
        this.setDescription("WMagicGuns Command")
        this.aliases = arrayOf("wmg", "wmagicguns", "wmagicgun")
        this.usage = "/wmg <sub-command> [args]"
        this.setCommandParameters(object : HashMap<String, Array<CommandParameter>>() {
            init {
                put("1arg", arrayOf(CommandParameter("help(h)", false, arrayOf("help", "h"))))
                put("2arg", arrayOf(CommandParameter("version(v)", false, arrayOf("version", "v"))))
                put("3arg", arrayOf(CommandParameter("give(g)", false, arrayOf("give", "g"))))
            }
        })
    }


    override fun execute(sender: CommandSender, label: String, args: Array<out String>): Boolean {
        if (args.isEmpty()) {
            sendHelp(sender)
            return true
        }

        when (args[0]) {
            "help", "h" -> {
                sendHelp(sender)
            }
            "gui" -> {
                if (sender is Player) {
                    val gunsMainTemplate = GunsGUI()
                    sender.showFormWindow(gunsMainTemplate)
                    //sender.showFormWindow(GunsSettingsWindow(GunsSettingsTemplate()))
                }
            }
            "give", "g" -> {
                if (sender.isOp) {

                    if (args.size == 2) {
                        if (sender is Player) {
                            WMagicGunsPlugin.instance.gunsConfig.gunsData[args[1]]?.let {
                                sender.inventory.addItem(ConfigMagicGuns(it))
                            }

                        } else {
                            sender.sendMessage("??????????????????????????????????????????.")
                        }
                    } else if (args.size == 3) {
                        Server.getInstance().getPlayer(args[1])?.let { player ->
                            WMagicGunsPlugin.instance.gunsConfig.gunsData[args[2]]?.let {
                                player.inventory.addItem(ConfigMagicGuns(it))
                            }
                        }
                    } else {
                        sender.sendMessage("&b ????????????????????????".color())
                        sendHelp(sender)
                    }

                } else {
                    sender.sendMessage("&b?????????????????????".color())
                }

            }
            "version", "v" -> {
                sender.sendMessage(TextFormat.colorize(WMagicGunsPlugin.TITLE + "&l&eWMagic&6Guns &r&c- &a${WMagicGunsPlugin.VERSION} &dMade by HBJ & WetABQ\n${WMagicGunsPlugin.TITLE}?????????????????????????????????, ??????????????? wetabq@gmail.com ???????????????"))
            }
        }
        return true
    }

    private fun sendHelp(sender: CommandSender) {
        sender.sendMessage("&6----WMagicGuns Command----".color())
        sender.sendMessage("&b/wmg help(h) - ??????????????????".color())
        sender.sendMessage("&b/wmg gui - ?????? WMagicGuns GUI ??????".color())
        sender.sendMessage("&b/wmg give(g)  [player: Player]  <gunId: String>- ?????????????????????????????????".color())
        sender.sendMessage("&b/wmg version(v) - ?????? WMagicGuns ??????".color())
    }

}