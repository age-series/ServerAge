package org.eln2.serverage.blocks

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelReader
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BlockStateProperties.*
import net.minecraft.world.level.material.Material
import net.minecraft.world.phys.BlockHitResult
import org.eln2.serverage.ServerAge.serverAgeTab

class ConduitBlock: Block(Properties.of(Material.STONE)) {

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
            LOGGER.info("${
                listOf(UP, DOWN, EAST, WEST, NORTH, SOUTH).map {
                    "${it.name}:${state?.getValue(it)}"
                }
            }")
            InteractionResult.CONSUME
        }
    }

    override fun createBlockStateDefinition(definition: StateDefinition.Builder<Block, BlockState>) {
        definition.add(UP, DOWN, EAST, WEST, NORTH, SOUTH)
    }

    override fun getStateForPlacement(context: BlockPlaceContext): BlockState? {
        val world: Level = context.level
        val pos: BlockPos = context.clickedPos
        val state: BlockState = defaultBlockState()
        return setConnections(world, pos, state)
    }

    private fun setConnections(level: LevelReader, pos: BlockPos, state: BlockState): BlockState? {
        return state
            .setValue(UP, canConnectConduit(level, pos, Direction.UP))
            .setValue(DOWN, canConnectConduit(level, pos, Direction.DOWN))
            .setValue(WEST, canConnectConduit(level, pos, Direction.WEST))
            .setValue(EAST, canConnectConduit(level, pos, Direction.EAST))
            .setValue(NORTH, canConnectConduit(level, pos, Direction.NORTH))
            .setValue(SOUTH, canConnectConduit(level, pos, Direction.SOUTH))
    }

    private fun canConnectConduit(level: LevelReader, pos: BlockPos, direction: Direction): Boolean {
        val testPos: BlockPos = pos.offset(direction.normal)
        val testState: BlockState = level.getBlockState(testPos)
        val testBlock: Block = testState.block

        if (testBlock == CONDUIT_BLOCK.get()) {
            val friend = testBlock as org.eln2.serverage.blocks.ConduitBlock
            friend.setConnections(level, testPos, testState)
        }

        return testBlock in listOf(
            SERVER_RACK_BLOCK.get(),
            WORKSTATION_BLOCK.get(),
            CONDUIT_BLOCK.get()
        )
    }

    override fun onNeighborChange(state: BlockState?, world: LevelReader?, pos: BlockPos?, neighbor: BlockPos?) {
        LOGGER.info("Neighbor Change Initiated")
        setConnections(world!!, pos!!, state!!)
    }

    init {
        this.registerDefaultState(
            this.stateDefinition.any()
            .setValue(UP, false).setValue(DOWN, false)
            .setValue(EAST, false).setValue(WEST, false)
            .setValue(NORTH, false).setValue(SOUTH, false)
        )
    }
}

class ConduitItem: BlockItem(CONDUIT_BLOCK.get(), Properties().tab(serverAgeTab))
