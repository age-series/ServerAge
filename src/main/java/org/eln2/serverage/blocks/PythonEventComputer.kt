package org.eln2.serverage.blocks

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.item.BlockItem
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.block.BaseEntityBlock
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.material.Material
import org.eln2.serverage.ServerAge

class PythonEventComputerBlockEntity(var pos: BlockPos, var state: BlockState): BlockEntity(PYTHON_EVENT_COMPUTER_BLOCK_ENTITY.get(), pos, state)

class PythonEventComputerBlock: BaseEntityBlock(Properties.of(Material.STONE)) {
    override fun newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity {
        return PythonEventComputerBlockEntity(pos, state)
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
