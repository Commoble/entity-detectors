package net.commoble.entitydetectors.registrables;

import net.commoble.entitydetectors.EntityDetectors;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;

public abstract class EntityDetectorBlockEntity<ENTITY extends Entity> extends BlockEntity
{
	public static final AABB BOX = Shapes.block().bounds();
	
	private final Class<? extends ENTITY> entityClass;
	
	public EntityDetectorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, Class<? extends ENTITY> entityClass)
	{
		super(type, pos, state);
		this.entityClass = entityClass;
	}

	public abstract boolean isEntityDetectable(ENTITY entity);
	
	public void onUpdate()
	{
		BlockState actualState = this.getBlockState();
		if (actualState.hasProperty(EntityDetectorBlock.LEVEL) && actualState.hasProperty(EntityDetectorBlock.POWERED))
		{
			BlockPos pos = this.worldPosition;
			int detectionLevel = actualState.getValue(EntityDetectorBlock.LEVEL);
			double radius = 2D * Math.pow(2D, detectionLevel);
			double radiusSquared = radius * radius;
			AABB aabb = BOX.move(pos).inflate(radius);
			Vec3 center = new Vec3(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
			boolean areEntitiesNear = !this.level.getEntitiesOfClass(
					this.entityClass,
					aabb,
					entity -> this.isEntityDetectable(entity) && entity.distanceToSqr(center) < radiusSquared)
				.isEmpty();
			
			if (areEntitiesNear != actualState.getValue(EntityDetectorBlock.POWERED))
			{
				level.setBlock(pos, actualState.setValue(EntityDetectorBlock.POWERED, areEntitiesNear), 3);
			}
		}
	}
	
	protected void tick(Level level, BlockPos pos, BlockState state)
	{
		if (level.getGameTime() % EntityDetectors.COMMON_CONFIG.refreshRate().get() == 0)
		{
			this.onUpdate();
		}
	}
}
