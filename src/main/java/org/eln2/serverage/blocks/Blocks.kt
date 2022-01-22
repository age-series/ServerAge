package org.eln2.serverage.blocks

import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraftforge.registries.RegistryObject
import org.eln2.serverage.ServerAge

fun blocksInit() {
    // This is literally just to make these constants registered.
}

const val CONDUIT_NAME = "conduit"
val CONDUIT_ITEM: RegistryObject<Item> = ServerAge.itemRegistry.register(CONDUIT_NAME, ::ConduitItem)
val CONDUIT_BLOCK: RegistryObject<Block> = ServerAge.blockRegistry.register(CONDUIT_NAME, ::ConduitBlock)

const val PYTHON_EVENT_COMPUTER_NAME = "python_event_computer"
val PYTHON_EVENT_COMPUTER_ITEM: RegistryObject<Item> = ServerAge.itemRegistry.register(PYTHON_EVENT_COMPUTER_NAME, ::PythonEventComputerItem)
val PYTHON_EVENT_COMPUTER_BLOCK: RegistryObject<Block> = ServerAge.blockRegistry.register(PYTHON_EVENT_COMPUTER_NAME, ::PythonEventComputerBlock)
val PYTHON_EVENT_COMPUTER_BLOCK_ENTITY: RegistryObject<BlockEntityType<*>> = ServerAge.blockEntityRegistry.register(PYTHON_EVENT_COMPUTER_NAME) {
    BlockEntityType.Builder.of(
        ::PythonEventComputerBlockEntity,
        PYTHON_EVENT_COMPUTER_BLOCK.get()
    ).build(null)
}

const val SERVER_RACK_NAME = "server_rack"
val SERVER_RACK_ITEM: RegistryObject<Item> = ServerAge.itemRegistry.register(SERVER_RACK_NAME, ::ServerRackItem)
val SERVER_RACK_BLOCK: RegistryObject<Block> = ServerAge.blockRegistry.register(SERVER_RACK_NAME, ::ServerRackBlock)

const val WORKSTATION_NAME = "workstation"
val WORKSTATION_ITEM: RegistryObject<Item> = ServerAge.itemRegistry.register(WORKSTATION_NAME, ::WorkstationItem)
val WORKSTATION_BLOCK: RegistryObject<Block> = ServerAge.blockRegistry.register(WORKSTATION_NAME, ::WorkstationBlock)
val WORKSTATION_BLOCK_ENTITY: RegistryObject<BlockEntityType<*>> = ServerAge.blockEntityRegistry.register(
    WORKSTATION_NAME) {
    BlockEntityType.Builder.of(
        ::WorkstationBlockEntity,
        WORKSTATION_BLOCK.get()
    ).build(null)
}
