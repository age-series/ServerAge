package org.eln2.serverage.items

import net.minecraft.world.item.Item
import org.eln2.serverage.ServerAge

open class Server1u: Item(Properties().tab(ServerAge.serverAgeTab)) {
    open val name = "server_1u"
    override fun getDescriptionId() = "item.serverage.$name"

    init {
        setRegistryName(ServerAge.MODID, name)
    }
}

open class Server2u: Item(Properties().tab(ServerAge.serverAgeTab)) {
    val name = "server_2u"
    override fun getDescriptionId() = "item.serverage.$name"

    init {
        setRegistryName(ServerAge.MODID, name)
    }
}

class LambdaServer: Server1u() {
    override val name = "lambda_server"
}
