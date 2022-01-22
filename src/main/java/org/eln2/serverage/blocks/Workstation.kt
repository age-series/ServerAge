package org.eln2.serverage.blocks

import net.minecraft.core.BlockPos
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.BlockItem
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.BaseEntityBlock
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.material.Material
import net.minecraft.world.phys.BlockHitResult
import org.eln2.serverage.ServerAge

class WorkstationBlockEntity(pos: BlockPos, state: BlockState): BlockEntity(WORKSTATION_BLOCK_ENTITY.get(), pos, state)

class WorkstationBlock: BaseEntityBlock(Properties.of(Material.STONE)) {
    override fun use(
        state: BlockState?,
        level: Level?,
        pos: BlockPos?,
        player: Player?,
        hand: InteractionHand?,
        result: BlockHitResult?
    ): InteractionResult {
        LOGGER.info("Did a thing! $state $level $pos $player $hand $result")
        return InteractionResult.PASS
    }

    override fun newBlockEntity(blockPos: BlockPos, blockState: BlockState): BlockEntity {
        return WorkstationBlockEntity(blockPos, blockState)
    }
}

class WorkstationItem: BlockItem(WORKSTATION_BLOCK.get(), Properties().tab(ServerAge.serverAgeTab))
