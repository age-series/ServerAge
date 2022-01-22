package org.eln2.serverage.items

import net.minecraft.world.inventory.MenuType
import net.minecraft.world.item.Item
import net.minecraftforge.registries.RegistryObject
import org.eln2.serverage.ServerAge

fun itemsInit() {
    // This is literally just to make these constants registered.
}

const val ETHERNET_CABLE_NAME = "ethernet_cable"
val ETHERNET_CABLE_ITEM: RegistryObject<Item> = ServerAge.itemRegistry.register(ETHERNET_CABLE_NAME, ::EthernetCable)

const val FIBER_CABLE_NAME = "fiber_cable"
val FIBER_CABLE_ITEM: RegistryObject<Item> = ServerAge.itemRegistry.register(FIBER_CABLE_NAME, ::FiberCable)

const val SERIAL_CONSOLE_NAME = "serial_console"
val SERIAL_CONSOLE_ITEM: RegistryObject<Item> = ServerAge.itemRegistry.register(SERIAL_CONSOLE_NAME, ::SerialConsole)
var SERIAL_CONSOLE_MENU_TYPE: RegistryObject<MenuType<SerialConsoleMenu>> = ServerAge.containerRegistry.register(SERIAL_CONSOLE_NAME) {
    MenuType<SerialConsoleMenu> { id, inv ->
        SerialConsoleMenu(id, inv)
    }
}

const val SERVER_1U_NAME = "server_1u"
val SERVER_1U_ITEM: RegistryObject<Item> = ServerAge.itemRegistry.register(SERVER_1U_NAME, ::Server1u)

const val SERVER_2U_NAME = "server_2u"
val SERVER_2U_ITEM: RegistryObject<Item> = ServerAge.itemRegistry.register(SERVER_2U_NAME, ::Server2u)

const val SWITCH_1U_NAME = "switch_1u"
val SWITCH_1U_ITEM: RegistryObject<Item> = ServerAge.itemRegistry.register(SWITCH_1U_NAME, ::Switch)

