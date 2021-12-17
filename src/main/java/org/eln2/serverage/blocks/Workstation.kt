package org.eln2.serverage.blocks

import net.minecraft.core.BlockPos
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.BlockItem
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.BaseEntityBlock
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.material.Material
import net.minecraft.world.phys.BlockHitResult
import org.eln2.serverage.ServerAge
import org.eln2.serverage.ServerAge.MODID

class WorkstationBlockEntity(pos: BlockPos, state: BlockState): BlockEntity(WorkstationBlock.blktype, pos, state) {

}

class WorkstationBlock: BaseEntityBlock(Properties.of(Material.STONE)) {
    val name = "workstation"

    override fun getDescriptionId(): String {
        return "block.serverage.$name"
    }

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

    companion object {
        val blktype: BlockEntityType<WorkstationBlockEntity> = BlockEntityType.Builder.of(
            { blockPos: BlockPos, blockState: BlockState -> WorkstationBlockEntity(blockPos, blockState) },
            WorkstationBlock()
        ).build(null)
    }

    init {
        setRegistryName(MODID, name)
    }

    override fun newBlockEntity(blockPos: BlockPos, blockState: BlockState): BlockEntity? {
        return WorkstationBlockEntity(blockPos, blockState)
    }
}

class WorkstationItem: BlockItem(ServerAge.modBlocks["block.serverage.workstation"]!!, Properties().tab(ServerAge.serverAgeTab)) {
    val name = "workstation"

    override fun getDescriptionId(): String {
        return "item.serverage.$name"
    }

    init {
        setRegistryName(MODID, name)
    }
}
