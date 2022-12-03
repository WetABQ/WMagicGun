package me.hbj233.wmagicguns

import cn.nukkit.Player
import cn.nukkit.event.EventHandler
import cn.nukkit.event.EventPriority
import cn.nukkit.event.Listener
import cn.nukkit.event.entity.EntityShootBowEvent
import cn.nukkit.event.player.PlayerEvent
import cn.nukkit.event.player.PlayerInteractEntityEvent
import cn.nukkit.event.player.PlayerInteractEvent
import cn.nukkit.event.player.PlayerItemHeldEvent
import cn.nukkit.event.server.DataPacketReceiveEvent
import cn.nukkit.inventory.transaction.data.UseItemOnEntityData
import cn.nukkit.item.Item
import cn.nukkit.network.protocol.InventoryTransactionPacket
import cn.nukkit.network.protocol.ProtocolInfo
import me.hbj233.wmagicguns.guns.WMagicGuns
import me.hbj233.wmagicguns.keep.PlayerButtonKeeper


class EventListener(plugin: WMagicGunsPlugin?) : Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerInteract(event: PlayerInteractEvent) = gunsInteract(event)

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerInteractEntity(event: PlayerInteractEntityEvent) = gunsInteract(event)

    private fun gunsInteract(event: PlayerEvent){
        if (!event.isCancelled) {
            val player = event.player
            val inv = player.inventory
            if (inv != null) {
                val item: Item = inv.itemInHand
                if (item.namedTag != null && item.namedTag.exist("gunId")) {
                    val gunId :String = item.namedTag.getString("gunId")
                    //println("gunId=$gunId")
                    val gun = WMagicGunsPlugin.getMagicGuns(WMagicGunsPlugin.instance)[gunId]
                    if (gun != null) {
                        gun.bindingToPlayer(player, inv.heldItemIndex)
                        gun.realFire(player)
                        event.setCancelled()
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onEntityShootBow(event: EntityShootBowEvent) {
        val bow = event.bow
        if (bow.namedTag != null && bow.namedTag.exist("gunId")) {
            if (!event.isCancelled && event.entity is Player) {
                event.setCancelled()
                val gunId = bow.namedTag.getString("gunId")
                val gun = WMagicGunsPlugin.getMagicGuns(WMagicGunsPlugin.instance)[gunId]
            }
        }
    }

    @EventHandler
    fun onPacketRcv(event: DataPacketReceiveEvent) {
        val player = event.player
        if (event.packet.pid() == ProtocolInfo.INVENTORY_TRANSACTION_PACKET) {
            val inv = player.inventory
            if (inv != null) {
                val item = inv.itemInHand
                if (item.namedTag != null && item.namedTag.exist("gunId")) {
                    val gunId = item.namedTag.getString("gunId")
                    val gun = WMagicGunsPlugin.getMagicGuns(WMagicGunsPlugin.instance)[gunId]
                    if (gun != null) {
                        val transactionPacket = event.packet as InventoryTransactionPacket
                        if (transactionPacket.transactionType == InventoryTransactionPacket.TYPE_USE_ITEM_ON_ENTITY) {
                            val useItemOnEntityData = transactionPacket.transactionData as UseItemOnEntityData
                            if (useItemOnEntityData.entityRuntimeId == PlayerButtonKeeper.BUTTON_KEEPER_EID) {
                                gun.bindingToPlayer(player, inv.heldItemIndex)
                                gun.realFire(player)
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    fun onItemHeld(event: PlayerItemHeldEvent){
        if(event.player.inventory.itemInHand is WMagicGuns){
            var gunBefore: WMagicGuns = event.player.inventory.itemInHand as WMagicGuns
            if (gunBefore.bindingPlayer == event.player) {
                val gunId: String = gunBefore.namedTag.getString("gunId")
                gunBefore = WMagicGunsPlugin.getMagicGuns(WMagicGunsPlugin.instance)[gunId] ?: error("")
                if (event.item.equals(gunBefore.bindingBullet, true, false)) {
                    if (gunBefore.getCount() != gunBefore.maxBullet) {
                        gunBefore.setReloading() //点击子弹物品手动换弹
                    }
                    event.setCancelled()
                    event.player.inventory.heldItemSlot = event.player.inventory.heldItemIndex
                }
            }
        }
        if(event.item is WMagicGuns){
            var gun: WMagicGuns = event.item as WMagicGuns
            if (gun.bindingPlayer === event.player){
                val gunId: String = gun.namedTag.getString("gunId")
                gun = WMagicGunsPlugin.instance.magicGuns[gunId]!!
                gun.bindingToPlayer(event.player, event.slot)
            }
        }
    }

    /* @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent){
        event.player.inventory.setItem(1, ConfigMagicGuns(Item.get(Item.FISHING_ROD), GunsData("Test",Item.FISHING_ROD,20,0.5F,false,114154,
                0.01F,8.0F,5.0F,0, Item.SEEDS ,1.2F,0.5F,
                1.3F,1.4F,1.5F,12F,"","",64,64,64,
                1.0,2.0,true, 10, 1)))
    }*/
}