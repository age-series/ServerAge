package org.eln2.serverage.blocks

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.HorizontalDirectionalBlock
import net.minecraft.world.level.block.Mirror
import net.minecraft.world.level.block.Rotation
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.material.Material
import net.minecraft.world.phys.BlockHitResult
import org.eln2.serverage.ServerAge
import org.eln2.serverage.ServerAge.MODID
import org.eln2.serverage.ServerAge.modBlocks

class ServerRackBlock: Block(Properties.of(Material.STONE)) {
    val blockName = "server_rack"

    override fun getDescriptionId(): String {
        return "block.serverage.$blockName"
    }

    override fun use(
        state: BlockState?,
        level: Level,
        pos: BlockPos?,
        player: Player,
        hand: InteractionHand?,
        result: BlockHitResult?
    ): InteractionResult {
        return if (level.isClientSide) {
            InteractionResult.SUCCESS
        } else {
            /*val menuprovider = getMenuProvider(p_51531_, p_51532_, p_51533_)
            if (menuprovider != null) {
                p_51534_.openMenu(menuprovider)
            }*/
            LOGGER.info("${state?.getValue(HorizontalDirectionalBlock.FACING)}")
            InteractionResult.CONSUME
        }
    }

    override fun getStateForPlacement(context: BlockPlaceContext): BlockState? {
        val direction: Direction = context.horizontalDirection.opposite
        return defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, direction)
    }

    override fun rotate(state: BlockState, rotation: Rotation): BlockState? {
        return state.setValue(
            HorizontalDirectionalBlock.FACING,
            rotation.rotate(state.getValue(HorizontalDirectionalBlock.FACING))
        )
    }

    override fun mirror(state: BlockState, mirror: Mirror): BlockState? {
        return state.rotate(mirror.getRotation(state.getValue(HorizontalDirectionalBlock.FACING)))
    }

    override fun createBlockStateDefinition(definition: StateDefinition.Builder<Block?, BlockState?>) {
        definition.add(HorizontalDirectionalBlock.FACING)
    }

    init {
        setRegistryName(MODID, blockName)
        /*registerDefaultState(
            stateDefinition.any().setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH)
        )*/
    }
}

class ServerRackItem: BlockItem(modBlocks["block.serverage.server_rack"]!!, Properties().tab(ServerAge.serverAgeTab)) {
    val itemName = "server_rack"

    override fun getDescriptionId(): String {
        return "item.serverage.$itemName"
    }

    init {
        setRegistryName(MODID, itemName)
    }
}
