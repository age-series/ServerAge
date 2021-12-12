package org.eln2.serverage.items

import net.minecraft.world.item.Item
import org.eln2.serverage.ServerAge

class Server1u: Item(Properties().tab(ServerAge.serverAgeTab)) {
    val name = "server_1u"
    override fun getDescriptionId() = "item.serverage.$name"

    init {
        setRegistryName(ServerAge.MODID, name)
    }
}

class Server2u: Item(Properties().tab(ServerAge.serverAgeTab)) {
    val name = "server_2u"
    override fun getDescriptionId() = "item.serverage.$name"

    init {
        setRegistryName(ServerAge.MODID, name)
    }
}
