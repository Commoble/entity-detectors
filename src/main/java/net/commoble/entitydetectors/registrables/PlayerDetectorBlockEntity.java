package net.commoble.entitydetectors.registrables;

import net.commoble.entitydetectors.EntityDetectors;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class PlayerDetectorBlockEntity extends EntityDetectorBlockEntity<Player>
{
	public static final BlockEntityTicker<PlayerDetectorBlockEntity> TICKER = (level, pos, state, be) -> be.tick(level, pos, state);
		
	public static PlayerDetectorBlockEntity create(BlockPos pos, BlockState state)
	{
		return new PlayerDetectorBlockEntity(EntityDetectors.PLAYER_DETECTOR_BET.get(), pos, state, Player.class);
	}

	public PlayerDetectorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, Class<? extends Player> entityClass)
	{
		super(type, pos, state, entityClass);
	}

	@Override
	public boolean isEntityDetectable(Player entity)
	{
		return true;
	}
}
