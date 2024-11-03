package net.commoble.entitydetectors.registrables;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import net.commoble.entitydetectors.EntityDetectors;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ImprintedSlimeballItem extends Item
{
	public ImprintedSlimeballItem(Properties properties)
	{
		super(properties);
	}

	@Override
	public Component getName(ItemStack stack)
	{
		return getEntityType(stack)
			.<Component>map(holder -> Component.translatable(this.getDescriptionId())
				.append(Component.literal(" ("))
				.append(Component.translatable(holder.value().getDescriptionId()))
				.append(Component.literal(")")))
			.orElseGet(() -> super.getName(stack));
	}

	public static ItemStack createItemStackForEntityType(EntityType<?> entityType)
	{
		var id = BuiltInRegistries.ENTITY_TYPE.getResourceKey(entityType).orElse(null);
		return createItemStackForEntityType(id);
	}
	
	public static ItemStack createItemStackForEntityType(ResourceKey<EntityType<?>> id)
	{
		ItemStack stack = new ItemStack(EntityDetectors.IMPRINTED_SLIME_BALL.get());
		if (id != null)
		{
			BuiltInRegistries.ENTITY_TYPE.getHolder(id)
				.ifPresent(holder -> stack.set(EntityDetectors.IMPRINTED_ENTITY.get(), holder));
		}
		return stack;
	}

	public static Optional<Holder<EntityType<?>>> getEntityType(ItemStack stack)
	{
		if (stack.getCount() < 0)
			return Optional.empty();
		return Optional.ofNullable(stack.get(EntityDetectors.IMPRINTED_ENTITY));
	}

	/**
	 * Called to fill the creative tab with the needed item variants
	 */
	public Collection<ItemStack> getVariants()
	{
		List<ItemStack> items = new ArrayList<>();
		BuiltInRegistries.ENTITY_TYPE.holders().filter(holder -> ImprintedSlimeballItem.isEntityTypeValid(holder.value()))
			.forEachOrdered(holder -> items.add(createItemStackForEntityType(holder.key())));
		return items;

	}

	public static boolean isEntityTypeValid(EntityType<?> entityType)
	{
		return entityType.getCategory() != MobCategory.MISC;
	}
}
