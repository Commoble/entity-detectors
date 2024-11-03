package net.commoble.entitydetectors.registrables;

import net.commoble.entitydetectors.EntityDetectors;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

public class MobDetectorItemHandler implements IItemHandler
{
	public final MobDetectorBlockEntity mobDetector;
	
	public static boolean isItemValidFilter(ItemStack stack)
	{
		return stack.is(EntityDetectors.Tags.Items.MOB_DETECTOR_FILTERS);
	}
	
	public MobDetectorItemHandler(MobDetectorBlockEntity mobDetector)
	{
		this.mobDetector = mobDetector;
	}

	@Override
	public int getSlots()
	{
		return 1;
	}

	@Override
	public ItemStack getStackInSlot(int slot)
	{
		return this.mobDetector.getSlimeStack();
	}

	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
	{
		ItemStack existingStack = this.mobDetector.getSlimeStack();
		if (existingStack.getCount() > 0)	// already has item
		{
			return stack.copy();
		}
		else	// doesn't already have item
		{
			if (!simulate)
			{
				ItemStack newStackInInventory = stack.copy();
				newStackInInventory.setCount(1);
				this.mobDetector.setSlimeStack(newStackInInventory);;
			}
			
			ItemStack remainder = stack.copy();
			remainder.shrink(1);
			return remainder;
		}
	}

	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate)
	{
		ItemStack outStack = this.mobDetector.getSlimeStack().copy();
		if (!simulate)
		{
			this.mobDetector.setSlimeStack(ItemStack.EMPTY);
		}
		return outStack;
	}

	@Override
	public int getSlotLimit(int slot)
	{
		return 1;
	}

	@Override
	public boolean isItemValid(int slot, ItemStack stack)
	{
		return isItemValidFilter(stack);
	}

}
