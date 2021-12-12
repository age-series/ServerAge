package org.eln2.serverage

import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.Block
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.fml.common.Mod
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.eln2.serverage.blocks.*
import org.eln2.serverage.items.*
import thedarkcolour.kotlinforforge.forge.FORGE_BUS
import thedarkcolour.kotlinforforge.forge.MOD_BUS
import thedarkcolour.kotlinforforge.forge.addGenericListener

@Mod(ServerAge.MODID)
object ServerAge {
    const val MODID: String = "serverage"
    val LOGGER: Logger = LogManager.getLogger()

    val serverAgeTab = object: CreativeModeTab("Server_Age") {
        override fun makeIcon(): ItemStack {
            return ItemStack(Items.BRICKS)
        }
    }

    init {
        // Blocks
        registerBlocks()

        // Items
        registerItems()

        MOD_BUS.register(this)
        FORGE_BUS.register(this)
    }

    val modItems = mutableMapOf<String, Item>()
    val modBlocks = mutableMapOf<String, Block>()

    private fun registerItems() {
        MOD_BUS.addGenericListener({ event: RegistryEvent.Register<Item> -> run {
            mutableListOf(Switch(), Server1u(), Server2u(), SerialConsole(), EthernetCable(), FiberCable(),
                ServerRackItem(), WorkstationItem(), ConduitItem()).forEach {
                    event.registry.register(it)
                modItems[it.descriptionId] = it
            }
        }})
    }

    private fun registerBlocks() {
        MOD_BUS.addGenericListener({event: RegistryEvent.Register<Block> -> run {
            mutableListOf(ServerRackBlock(), WorkstationBlock(), ConduitBlock()).forEach {
                event.registry.register(it)
                modBlocks[it.descriptionId] = it
            }
        }})
    }
}
