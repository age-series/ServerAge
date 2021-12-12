package org.eln2.serverage.items

import net.minecraft.world.item.Item
import org.eln2.serverage.ServerAge

class Switch: Item(Properties().tab(ServerAge.serverAgeTab)) {
    private val name: String  = "switch_1u"
    override fun getDescriptionId() = "item.serverage.$name"

    init {
        setRegistryName(ServerAge.MODID, name)
    }
}
