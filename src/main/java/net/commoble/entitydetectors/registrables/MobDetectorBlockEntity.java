package net.commoble.entitydetectors.registrables;

import java.util.Optional;

import javax.annotation.Nullable;

import net.commoble.entitydetectors.EntityDetectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;

public class MobDetectorBlockEntity extends EntityDetectorBlockEntity<Mob>
{
	public static final String FILTER_KEY = "filter";
	
	public static final BlockEntityTicker<MobDetectorBlockEntity> TICKER = (level,pos,state,be) -> be.tick(level, pos, state);

	private ItemStack slimeStack = ItemStack.EMPTY;
	
	private final IItemHandler itemHandler = new MobDetectorItemHandler(this);
	
	public static MobDetectorBlockEntity create(BlockPos pos, BlockState state)
	{
		return new MobDetectorBlockEntity(EntityDetectors.MOB_DETECTOR_BET.get(), pos, state, Mob.class);
	}

	public MobDetectorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, Class<? extends Mob> mobClass)
	{
		super(type, pos, state, mobClass);
	}

	public ItemStack getSlimeStack()
	{
		return this.slimeStack;
	}
	
	public void setSlimeStack(ItemStack slimeStack)
	{
		this.slimeStack = slimeStack;
		this.setChanged();
		this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 2);
	}
	
	public boolean isEntityDetectable(Mob ent)
	{
		return ImprintedSlimeballItem.getEntityType(this.slimeStack).map(type -> type.value() == ent.getType()).orElse(true);
	}
	
	public Optional<Holder<EntityType<?>>> getFilteredEntityType()
	{
		return ImprintedSlimeballItem.getEntityType(this.slimeStack);
	}
	
	public void onRightClickWithSlime(Player player, ItemStack stack, Level level, BlockPos pos)
	{
		this.dropSlime(level, pos);
		
		ItemStack remainder = this.itemHandler.insertItem(0, stack, false);
		
		stack.setCount(remainder.getCount());
	}
	
	private void dropSlime(Level level, BlockPos pos)
	{
		if (!level.isClientSide)
		{
			ItemStack extractedStack = this.itemHandler.extractItem(0, 1, false);
			if (!extractedStack.isEmpty())
			{
				level.playSound(null, pos, SoundEvents.SLIME_SQUISH_SMALL, SoundSource.BLOCKS,
					0.1F + level.random.nextFloat()*0.3F,
					level.random.nextFloat()*1.5F + 1F);

				double d0 = (double) (level.random.nextFloat() * 0.7F) + (double) 0.15F;
				double d1 = (double) (level.random.nextFloat() * 0.7F) + (double) 0.060000002F + 0.6D;
				double d2 = (double) (level.random.nextFloat() * 0.7F) + (double) 0.15F;
				ItemEntity itementity = new ItemEntity(level, pos.getX() + d0, pos.getY() + d1, pos.getZ() + d2, extractedStack.copy());
				itementity.setDefaultPickUpDelay();
				level.addFreshEntity(itementity);
			}
		}
	}
	
	public IItemHandler getItemHandler(@Nullable Direction side)
	{
		return this.getItemHandler(side);
	}

	@Override
	public void loadAdditional(CompoundTag compound, HolderLookup.Provider registries)
	{
		super.loadAdditional(compound, registries);
		if (compound.contains(FILTER_KEY))
		{
			CompoundTag itemTag = compound.getCompound(FILTER_KEY);
			this.slimeStack = ItemStack.parseOptional(registries, itemTag);
		}
	}
	
	@Override
	public void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries)
	{
		super.saveAdditional(nbt, registries);
		Tag itemNBT = this.slimeStack.isEmpty() ? new CompoundTag() : this.slimeStack.save(registries);
		nbt.put(FILTER_KEY, itemNBT);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries)
	{
		return this.saveWithoutMetadata(registries);
	}

	@Override
	@Nullable
	public ClientboundBlockEntityDataPacket getUpdatePacket()
	{
		return ClientboundBlockEntityDataPacket.create(this);
	}
}
