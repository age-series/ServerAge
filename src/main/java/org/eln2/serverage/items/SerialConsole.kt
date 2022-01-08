package org.eln2.serverage.items

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.Tesselator
import com.mojang.blaze3d.vertex.VertexFormat
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.screens.inventory.MenuAccess
import net.minecraft.client.renderer.GameRenderer
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.TextComponent
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.*
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.level.Level
import net.minecraftforge.network.NetworkHooks
import org.eln2.serverage.LOGGER
import org.eln2.serverage.ServerAge
import org.eln2.serverage.ServerAge.serialConsoleMenuType

class SerialConsole: Item(Properties().tab(ServerAge.serverAgeTab)) {
    private val name: String  = "serial_console"
    override fun getDescriptionId() = "item.serverage.$name"

    init {
        setRegistryName(ServerAge.MODID, name)
    }

    override fun use(level: Level, player: Player, hand: InteractionHand): InteractionResultHolder<ItemStack> {
        LOGGER.info("Got use call $level $player $hand")
        if (!level.isClientSide) {
            val serverPlayer = player as ServerPlayer
            NetworkHooks.openGui(serverPlayer, SerialConsoleGui(level, if (hand == InteractionHand.MAIN_HAND) serverPlayer.mainHandItem else serverPlayer.offhandItem, serverPlayer.inventory))
        }
        return InteractionResultHolder.success(
            if (hand == InteractionHand.MAIN_HAND) player.mainHandItem else player.offhandItem
        )
    }

    override fun useOn(context: UseOnContext): InteractionResult {
        if (!context.level.isClientSide) {
            val serverPlayer = context.player as ServerPlayer
            NetworkHooks.openGui(serverPlayer, SerialConsoleGui(context.level, if (context.hand == InteractionHand.MAIN_HAND) serverPlayer.mainHandItem else serverPlayer.offhandItem, serverPlayer.inventory))
        }
        return InteractionResult.SUCCESS
    }
}

class SerialConsoleGui(val level: Level, heldItem: ItemStack, private val inventory: Inventory): MenuProvider {
    override fun createMenu(guiId: Int, localInventory: Inventory, player: Player): AbstractContainerMenu? {
        LOGGER.info("creating menu in serial console GUI")
        return SerialConsoleMenu(guiId, localInventory )
    }

    override fun getDisplayName(): Component {
        return TextComponent("Hello")
    }
}

class SerialConsoleMenu(int_of_some_kind: Int, val inventory: Inventory): AbstractContainerMenu(serialConsoleMenuType.get(), int_of_some_kind) {
    override fun stillValid(player: Player): Boolean {
        return true
    }
}

class SerialConsoleScreen(val scm: SerialConsoleMenu, val inv: Inventory, component: Component) : Screen(component), MenuAccess<SerialConsoleMenu>{
    override fun getMenu(): SerialConsoleMenu {
        LOGGER.info("Getting menu")
        return scm
    }

    companion object {
        val BACKGROUND = ResourceLocation(ServerAge.MODID, "textures/gui/container/serialconsolegui.png")
        const val backgroundWidth = 300
        const val backgroundHeight = 300
    }

    override fun render(poseStack: PoseStack, mouseX: Int, mouseY: Int, delta: Float) {
        //TODO: Implement textures
        RenderSystem.disableDepthTest()
        RenderSystem.setShader(GameRenderer::getPositionTexShader)
        RenderSystem.setShaderTexture(0, BACKGROUND)
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f)
        val ps = RenderSystem.getModelViewStack()
        ps.pushPose()
        ps.clear()
        RenderSystem.applyModelViewMatrix()

        with(Tesselator.getInstance()) {
            with(builder) {
                begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX)
                val w = width.toDouble()
                val h = height.toDouble()
                val bw = backgroundWidth.toDouble()
                val bh = backgroundHeight.toDouble()
                val lx = w / 2 - bw / 2
                val hx = w / 2 + bw / 2
                val ly = h / 2 - bh / 2
                val hy = h / 2 + bh / 2
                vertex(lx, ly, 0.0).uv(0f, 0f).endVertex()
                vertex(hx, ly, 0.0).uv(bw.toFloat(), 0f).endVertex()
                vertex(hx, hy, 0.0).uv(bw.toFloat(), bh.toFloat()).endVertex()
                vertex(lx, hy, 0.0).uv(0f, bh.toFloat()).endVertex()
            }
            end()
        }

        ps.popPose()
        RenderSystem.applyModelViewMatrix()
        RenderSystem.enableDepthTest()

        super.render(poseStack, mouseX, mouseY, delta)
    }
}
