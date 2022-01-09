package org.eln2.serverage

import net.minecraft.client.gui.screens.MenuScreens
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.Block
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
import net.minecraftforge.registries.DeferredRegister
import net.minecraftforge.registries.ForgeRegistries
import net.minecraftforge.registries.RegistryObject
import org.eln2.serverage.items.LambdaServer
import org.eln2.serverage.items.SerialConsole
import org.eln2.serverage.items.SerialConsoleMenu
import org.eln2.serverage.items.SerialConsoleScreen
import thedarkcolour.kotlinforforge.forge.FORGE_BUS
import thedarkcolour.kotlinforforge.forge.MOD_BUS
import thedarkcolour.kotlinforforge.forge.addGenericListener

@Mod(ServerAge.MODID)
object ServerAge {
    const val MODID: String = "serverage"

    val serverAgeTab = object: CreativeModeTab("Server_Age") {
        override fun makeIcon(): ItemStack {
            return ItemStack(Items.BRICKS)
        }
    }

    @JvmStatic
    private var deferredContainerRegistry = DeferredRegister.create(ForgeRegistries.CONTAINERS, MODID)
    var serialConsoleMenuType: RegistryObject<MenuType<SerialConsoleMenu>> = deferredContainerRegistry.register(
        SerialConsole.name
    ) {
        MenuType<SerialConsoleMenu> { id, inv ->
            SerialConsoleMenu(id, inv)
        }
    }

    init {
        // Blocks
        registerBlocks()

        // Items
        registerItems()

        MOD_BUS.register(this)
        FORGE_BUS.register(this)

        deferredContainerRegistry.register(MOD_BUS)
    }

    val modItems = mutableMapOf<String, Item>()
    val modBlocks = mutableMapOf<String, Block>()

    @SubscribeEvent
    fun clientSetupEvent(event: FMLClientSetupEvent) {
        event.enqueueWork {
            MenuScreens.register(serialConsoleMenuType.get(), ::SerialConsoleScreen)
        }
    }

    private fun registerItems() {
        MOD_BUS.addGenericListener({ event: RegistryEvent.Register<Item> -> run {
            // Switch(), Server1u(), Server2u(), EthernetCable(), FiberCable(), ServerRackItem(), WorkstationItem(), ConduitItem()
            mutableListOf(LambdaServer(), SerialConsole()).forEach {
                    event.registry.register(it)
                modItems[it.descriptionId] = it
            }
        }})
    }

    private fun registerBlocks() {
        MOD_BUS.addGenericListener({event: RegistryEvent.Register<Block> -> run {
            /*
            mutableListOf(ServerRackBlock(), WorkstationBlock(), ConduitBlock()).forEach {
                event.registry.register(it)
                modBlocks[it.descriptionId] = it
            }*/
        }})
    }
}
