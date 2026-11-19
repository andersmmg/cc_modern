package com.andersmmg.cc_modern.block;

import com.sashafiesta.ccterminals.TerminalBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class WallTerminalBlockEntity extends TerminalBlockEntity {
    public WallTerminalBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, boolean colour) {
        super(type, pos, state, colour);
    }

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }
}
