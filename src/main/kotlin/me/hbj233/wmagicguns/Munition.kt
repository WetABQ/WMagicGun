package me.hbj233.wmagicguns

import cn.nukkit.item.Item
import cn.nukkit.nbt.tag.CompoundTag

abstract class Munition(id: Int, meta: Int = 0, count: Int = 1, name: String = "Unknown") : Item(id, meta, count, name) {

    open lateinit var munitionId: String

    override fun clone(): Munition {
        val item = super.clone()
        return item as Munition
    }

    init {
        this.munitionId = name
        val tag: CompoundTag = (if (!this.hasCompoundTag()) {
            CompoundTag()
        } else {
            this.namedTag
        }).also {
            it.putString("Munition", this.munitionId)
        }
        this.namedTag = tag
    }

}