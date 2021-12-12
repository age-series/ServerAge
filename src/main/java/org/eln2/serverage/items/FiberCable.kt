package org.eln2.serverage.items

import net.minecraft.world.item.Item
import org.eln2.serverage.ServerAge

class FiberCable: Item(Properties().tab(ServerAge.serverAgeTab)) {
    private val name: String  = "fiber_cable"
    override fun getDescriptionId() = "item.serverage.$name"

    init {
        setRegistryName(ServerAge.MODID, name)
    }
}
