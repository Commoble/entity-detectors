package net.commoble.entitydetectors.registrables;

import net.commoble.entitydetectors.EntityDetectors;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class PlayerDetectorBlock extends EntityDetectorBlock
{
	public PlayerDetectorBlock(Properties properties)
	{
		super(properties);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
	{
		return PlayerDetectorBlockEntity.create(pos, state);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type)
	{
		return !level.isClientSide && type == EntityDetectors.PLAYER_DETECTOR_BET.get()
			? (BlockEntityTicker<T>)PlayerDetectorBlockEntity.TICKER
			: null;
	}
	
	
}
