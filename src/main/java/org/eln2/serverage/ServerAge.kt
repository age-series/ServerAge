package org.eln2.serverage

import net.minecraft.client.gui.screens.MenuScreens
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.core.Registry
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.Block
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn
import net.minecraftforge.client.ConfigGuiHandler
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.IExtensionPoint
import net.minecraftforge.fml.ModLoadingContext
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
import net.minecraftforge.registries.ForgeRegistries
import net.minecraftforge.registries.ForgeRegistry
import net.minecraftforge.registries.ObjectHolder
import net.minecraftforge.registries.RegistryObject
import net.minecraftforge.registries.DeferredRegister
import org.eln2.serverage.blocks.*
import org.eln2.serverage.items.*
import software.amazon.awssdk.services.cloudformation.model.RegistryType
import thedarkcolour.kotlinforforge.forge.FORGE_BUS
import thedarkcolour.kotlinforforge.forge.MOD_BUS
import thedarkcolour.kotlinforforge.forge.addGenericListener
import java.io.Serial
import kotlin.math.E

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
