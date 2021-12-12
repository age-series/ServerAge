package org.eln2.serverage.items

import net.minecraft.world.item.Item
import org.eln2.serverage.ServerAge

class SerialConsole: Item(Properties().tab(ServerAge.serverAgeTab)) {
    private val name: String  = "serial_console"
    override fun getDescriptionId() = "item.serverage.$name"

    init {
        setRegistryName(ServerAge.MODID, name)
    }
}
