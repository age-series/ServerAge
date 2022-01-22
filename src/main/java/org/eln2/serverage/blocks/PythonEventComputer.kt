package org.eln2.serverage.blocks

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.TranslatableComponent
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.Container
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.MenuProvider
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.item.BlockItem
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.BaseEntityBlock
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.material.Material
import net.minecraft.world.phys.BlockHitResult
import net.minecraftforge.network.NetworkHooks
import org.eln2.serverage.ServerAge
import org.eln2.serverage.ServerAge.MODID

class PythonEventComputerScreen(container: PythonEventComputerContainer, inv: Inventory, name: Component): AbstractContainerScreen<PythonEventComputerContainer>(container, inv, name) {
    private val gui: ResourceLocation = ResourceLocation(MODID, "textures/gui/serialconsolegui.png")

    override fun render(matrixStack: PoseStack, mouseX: Int, mouseY: Int, partialTicks: Float) {
        this.renderBackground(matrixStack)
        super.render(matrixStack, mouseX, mouseY, partialTicks)
        this.renderTooltip(matrixStack, mouseX, mouseY)
    }

    override fun renderLabels(matrixStack: PoseStack, mouseX: Int, mouseY: Int) {
        drawString(matrixStack, Minecraft.getInstance().font, "Hello, world!", 10, 10, 0xffffff)
    }

    override fun renderBg(matrixStack: PoseStack, partialTicks: Float, mouseX: Int, mouseY: Int) {
        RenderSystem.setShaderTexture(0, gui)
        val relX = (this.width - this.imageWidth) / 2
        val relY = (this.height - this.imageHeight) / 2
        this.blit(matrixStack, relX, relY, 0, 0, this.imageWidth, this.imageHeight)
    }
}

class PythonEventComputerContainer(windowId: Int, playerInventory: Inventory, player: Player): AbstractContainerMenu(
    PYTHON_EVENT_COMPUTER_CONTAINER.get(), windowId) {
    override fun stillValid(p_38874_: Player): Boolean {
        return true
    }
}

class PythonEventComputerBlockEntity(var pos: BlockPos, var state: BlockState): BlockEntity(PYTHON_EVENT_COMPUTER_BLOCK_ENTITY.get(), pos, state)

class PythonEventComputerBlock: BaseEntityBlock(Properties.of(Material.STONE)) {
    override fun newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity {
        return PythonEventComputerBlockEntity(pos, state)
    }

    override fun use(state: BlockState, level: Level, pos: BlockPos, player: Player, hand: InteractionHand, hit: BlockHitResult): InteractionResult {

        if (!level.isClientSide) {
            val blockEntity = level.getBlockEntity(pos)
            if (blockEntity is PythonEventComputerBlockEntity) {
                val containerProvider = object: MenuProvider {
                    override fun getDisplayName(): Component {
                        return TranslatableComponent("hello.world")
                    }

                    override fun createMenu(
                        windowId: Int,
                        playerInventory: Inventory,
                        playerEntity: Player
                    ): AbstractContainerMenu? {
                        return PythonEventComputerContainer(windowId, playerInventory, playerEntity)
                    }
                }
                NetworkHooks.openGui(player as ServerPlayer, containerProvider, blockEntity.pos)
            }
        }

        return InteractionResult.SUCCESS
    }

    override fun canConnectRedstone(
        state: BlockState?,
        world: BlockGetter?,
        pos: BlockPos?,
        direction: Direction?
    ): Boolean {
        return true
    }
}

class PythonEventComputerItem: BlockItem(PYTHON_EVENT_COMPUTER_BLOCK.get(), Properties().tab(ServerAge.serverAgeTab))
