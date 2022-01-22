package org.eln2.serverage

import net.minecraft.client.gui.screens.MenuScreens
import net.minecraftforge.eventbus.api.EventPriority
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
import org.eln2.serverage.blocks.PYTHON_EVENT_COMPUTER_CONTAINER
import org.eln2.serverage.blocks.PythonEventComputerScreen
import thedarkcolour.kotlinforforge.forge.MOD_BUS

object ClientInit {
    fun init() {
        LOGGER.info("This is client")
        try {
            // sadly, this fails. Suggested code using annotated events doesn't work either. :/ Can't be in main mod class because it would crash due to server side not having client classes in classpath
            MOD_BUS.addGenericListener(FMLClientSetupEvent::class.java, EventPriority.NORMAL) {event: FMLClientSetupEvent ->
                event.enqueueWork {
                    LOGGER.info("Created screen")
                    MenuScreens.register(
                        PYTHON_EVENT_COMPUTER_CONTAINER.get(),
                        ::PythonEventComputerScreen
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
