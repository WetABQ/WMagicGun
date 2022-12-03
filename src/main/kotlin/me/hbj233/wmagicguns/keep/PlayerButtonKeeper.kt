package me.hbj233.wmagicguns.keep


import cn.nukkit.Player
import cn.nukkit.entity.Entity
import cn.nukkit.entity.data.EntityMetadata
import cn.nukkit.entity.data.LongEntityData
import cn.nukkit.network.protocol.AddEntityPacket
import cn.nukkit.network.protocol.RemoveEntityPacket
import me.hbj233.wmagicguns.guns.WMagicGuns


class PlayerButtonKeeper(val player: Player) {
    private var spawned = false

    fun spawnTo() {
        if (!spawned) {
            player.buttonText = "     开火     "
            //player.checkInteractNearby = false;
            val metadata = EntityMetadata()
            metadata.putLong(Entity.DATA_FLAGS, 0)
                    .putShort(Entity.DATA_AIR, 400)
                    .putShort(Entity.DATA_MAX_AIR, 400)
                    .putLong(Entity.DATA_LEAD_HOLDER_EID, -1)
                    .putFloat(Entity.DATA_SCALE, 5f)
            var flags: Long = 0
            flags = flags xor (1 shl Entity.DATA_FLAG_INVISIBLE).toLong()
            //flags ^= (long)(1 << Entity.DATA_FLAG_IMMOBILE);
            metadata.put(LongEntityData(Entity.DATA_FLAGS, flags))
            val pk = AddEntityPacket()
            pk.entityRuntimeId = BUTTON_KEEPER_EID
            pk.entityUniqueId = BUTTON_KEEPER_EID
            pk.type = 37
            pk.x = player.getX().toFloat()
            pk.y = player.getY().toFloat()
            pk.z = player.getZ().toFloat()
            pk.speedX = 0f
            pk.speedY = 0f
            pk.speedZ = 0f
            pk.yaw = 0f
            pk.pitch = 0f
            pk.metadata = metadata
            player.dataPacket(pk)
            spawned = true
        }
    }

    /*    public void updatePosition() {
        if (this.spawned) {
            MoveEntityPacket pk = new MoveEntityPacket();
            pk.eid = BUTTON_KEEPER_EID;
            pk.x = (float) player.getX();
            pk.y = (float) player.getY();
            pk.z = (float) player.getZ();
            pk.yaw = 0;
            pk.pitch = 0;
            pk.headYaw = 0;
            player.dataPacket(pk);
        }
    }*/
    fun despawnFrom() {
        if (spawned) {
            player.buttonText = ""
            //player.checkInteractNearby = true;
            val pk = RemoveEntityPacket()
            pk.eid = BUTTON_KEEPER_EID
            player.dataPacket(pk)
            spawned = false
        }
    }

    fun onTick() {
        if (!player.isOnline || !player.spawned) return
        //  if (player instanceof SynapsePlayer && ((SynapsePlayer) player).isLevelChange()) return;
        val inv = player.inventory
        if (inv != null) {
            val item = inv.itemInHand
            if (item is WMagicGuns) {
                spawnTo() //Auto if (this.spawned) {}
                // this.updatePosition();
                player.setButtonText("     Fire     ");
                return
            }
        }
        despawnFrom()
    }

    companion object {
        const val BUTTON_KEEPER_EID = Long.MAX_VALUE - 2345
    }

}