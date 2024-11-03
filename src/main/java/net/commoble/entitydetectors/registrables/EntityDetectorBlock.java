package net.commoble.entitydetectors.registrables;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;

public abstract class EntityDetectorBlock extends Block implements EntityBlock
{
	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
	public static final IntegerProperty LEVEL = IntegerProperty.create("level", 0, 3);

	public EntityDetectorBlock(Properties properties)
	{
		super(properties);
		this.registerDefaultState(this.defaultBlockState().setValue(POWERED, false).setValue(LEVEL, 0));
	}
	
	public abstract <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type);

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder)
	{
		builder.add(POWERED, LEVEL);
	}

	public static int getLightValue(BlockState state)
	{
		return state.getValue(POWERED) ? 7 : 0;
	}

	@Override
	public boolean isSignalSource(BlockState state)
	{
		return true;
	}
	
	@Override
	public int getDirectSignal(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side)
	{
		return blockState.getSignal(blockAccess, pos, side);
	}

	@Override
	public int getSignal(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side)
	{
		return blockState.getValue(POWERED) ? 15 : 0;
	}

	@Override
	public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit)
	{
		if (state.hasProperty(LEVEL))
		{
			int oldLevel = state.getValue(LEVEL);
			int newLevel = (oldLevel + 1) % 4; // cycle forward
			level.setBlock(pos, state.setValue(LEVEL, newLevel), Block.UPDATE_ALL);
			this.onUpdate(state, level, pos);
			float volume = 0.25F + level.random.nextFloat() * 0.1F;
			float pitch = 0.5F + level.random.nextFloat() * 0.1F;
			level.playSound(player, pos, SoundEvents.STONE_BUTTON_CLICK_ON, SoundSource.BLOCKS, volume, pitch);

			return InteractionResult.SUCCESS;
		}
		else
		{
			return super.useWithoutItem(state, level, pos, player, hit);
		}
	}

	@Override
	public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving)
	{
		if (state.getBlock() != oldState.getBlock())
		{
			this.onUpdate(state, level, pos);
		}
	}

	public void onUpdate(BlockState state, Level level, BlockPos pos)
	{
		if (!level.isClientSide && level.getBlockEntity(pos) instanceof EntityDetectorBlockEntity<?> entityDetector)
		{
			entityDetector.onUpdate();
		}
	}
}
