package org.eln2.serverage

import net.minecraft.world.inventory.MenuType
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.registries.DeferredRegister
import net.minecraftforge.registries.ForgeRegistries
import org.eln2.serverage.blocks.SERVER_RACK_ITEM
import org.eln2.serverage.blocks.blocksInit
import org.eln2.serverage.items.itemsInit
import thedarkcolour.kotlinforforge.forge.FORGE_BUS
import thedarkcolour.kotlinforforge.forge.MOD_BUS

@Mod(ServerAge.MODID)
object ServerAge {
    const val MODID: String = "serverage"

    val serverAgeTab = object: CreativeModeTab("Server_Age") {
        override fun makeIcon(): ItemStack {
            return ItemStack(SERVER_RACK_ITEM.get())
        }
    }

    @JvmStatic
    val blockRegistry: DeferredRegister<Block> = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID)
    @JvmStatic
    val itemRegistry: DeferredRegister<Item> = DeferredRegister.create(ForgeRegistries.ITEMS, MODID)
    @JvmStatic
    val blockEntityRegistry: DeferredRegister<BlockEntityType<*>> = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, MODID)
    @JvmStatic
    val containerRegistry: DeferredRegister<MenuType<*>> = DeferredRegister.create(ForgeRegistries.CONTAINERS, MODID)

    init {
        MOD_BUS.register(this)
        FORGE_BUS.register(this)

        containerRegistry.register(MOD_BUS)
        blockRegistry.register(MOD_BUS)
        itemRegistry.register(MOD_BUS)
        blockEntityRegistry.register(MOD_BUS)

        blocksInit()
        itemsInit()
    }
}
