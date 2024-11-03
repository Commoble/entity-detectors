package net.commoble.entitydetectors.client;

import java.util.Optional;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class FakeClientEntities
{
	private static final LoadingCache<EntityContext<?>, Entity> ENTITY_LOADER = CacheBuilder.newBuilder()
		.build(
			new CacheLoader<EntityContext<?>, Entity>()
			{
				@Override
				public Entity load(EntityContext<?> context)
				{
					return context.createEntity();
				}
			}
		);
	
	public static <T extends Entity> Entity getFakeEntity(EntityType<T> type, Level level)
	{
		if (type == null)
			return null;
		return ENTITY_LOADER.getUnchecked(EntityContext.of(type, level));
	}
	
	public static Optional<Entity> getOptionalFakeEntity(Optional<Holder<EntityType<?>>> maybeType, Level level)
	{
		return maybeType.map(type -> ENTITY_LOADER.getUnchecked(EntityContext.of(type.value(), level)));
	}
	
	public static record EntityContext<T extends Entity>(EntityType<T> type, Level level)
	{		
		public static <T extends Entity> EntityContext<T> of(EntityType<T> type, Level level)
		{
			return new EntityContext<T>(type, level);
		}
		
		public T createEntity()
		{
			return this.type.create(this.level);
		}
	}
}
