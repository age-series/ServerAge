package org.eln2.serverage.blocks

import net.minecraft.world.item.BlockItem
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.material.Material
import org.eln2.serverage.ServerAge.MODID
import org.eln2.serverage.ServerAge.serverAgeTab

class ConduitBlock: Block(Properties.of(Material.STONE)) {
    val name = "conduit"

    override fun getDescriptionId(): String {
        return "block.serverage.$name"
    }

    init {
        setRegistryName(MODID, name)
    }
}

class ConduitItem: BlockItem(ConduitBlock(), Properties().tab(serverAgeTab)) {
    val name = "conduit"

    override fun getDescriptionId(): String {
        return "item.serverage.$name"
    }

    init {
        setRegistryName(MODID, name)
    }
}
