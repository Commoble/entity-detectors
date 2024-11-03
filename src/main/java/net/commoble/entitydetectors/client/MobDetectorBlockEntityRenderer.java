package net.commoble.entitydetectors.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.commoble.entitydetectors.registrables.MobDetectorBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class MobDetectorBlockEntityRenderer implements BlockEntityRenderer<MobDetectorBlockEntity>
{
	private static final float DEGREES_PER_ROTATION = 360F;
	private static final float ROTATIONS_PER_SECOND = 0.5F;
	private static final float SECONDS_PER_TICK = 0.05F;
	private static final float DEGREES_PER_TICK = DEGREES_PER_ROTATION * ROTATIONS_PER_SECOND * SECONDS_PER_TICK;
	
	private static final double MIN_RENDER_DISTANCE = 2F;
	private static final double MAX_RENDER_DISTANCE = 5F;
	
	private static final double MIN_RENDER_DISTANCE_SQ = MIN_RENDER_DISTANCE * MIN_RENDER_DISTANCE;
	private static final double MAX_RENDER_DISTANCE_SQ = MAX_RENDER_DISTANCE * MAX_RENDER_DISTANCE;
	private static final double DISTANCE_DIFF = MAX_RENDER_DISTANCE_SQ - MIN_RENDER_DISTANCE_SQ;

	public MobDetectorBlockEntityRenderer(BlockEntityRendererProvider.Context context)
	{
	}

	@Override
	public void render(MobDetectorBlockEntity be, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int lightUV, int overlayUV)
	{
		@SuppressWarnings("resource")
		LocalPlayer player = Minecraft.getInstance().player;
		Level level = be.getLevel();
		if (player != null && level.isEmptyBlock(be.getBlockPos().above()))
		{
			FakeClientEntities.getOptionalFakeEntity(be.getFilteredEntityType(), level)
				.ifPresent(entity -> this.renderFakeEntity(be, player, entity, partialTicks, poseStack, buffer, lightUV));
		}
	}
	
	public void renderFakeEntity(MobDetectorBlockEntity be, LocalPlayer player, Entity entity, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int lightUV)
	{
		BlockPos pos = be.getBlockPos();
		double teX = pos.getX() + 0.5D;
		double teY = pos.getY() + 0.5D;
		double teZ = pos.getZ() + 0.5D;
		
		// player position is only updated on game tick, add extra offset based on velocity for more smoothness
		Vec3 vel = player.getDeltaMovement();
		double extraX = vel.x * partialTicks;
		double extraY = vel.y * partialTicks;
		double extraZ = vel.z * partialTicks;
		double playerDistanceSq = player.distanceToSqr(teX - extraX, teY - extraY, teZ - extraZ);
		if (playerDistanceSq < MAX_RENDER_DISTANCE_SQ)
		{
			double distFactor = 1 - (Math.min(playerDistanceSq - MIN_RENDER_DISTANCE_SQ, DISTANCE_DIFF) / DISTANCE_DIFF);
			
			long gameTicks = be.getLevel().getGameTime();
			
			float renderTicks = gameTicks + partialTicks;
			
			float rotation = renderTicks * DEGREES_PER_TICK % 360F;

			// render entity
			// based on MobSpawnerTileEntityRenderer
			poseStack.pushPose();

			float entityScale = 0.53125F;
			
			entityScale *= 0.8F * distFactor;
			float scaleDivisor = Math.max(entity.getBbWidth(), entity.getBbHeight());
			if (scaleDivisor > 1.0D)
			{
				entityScale /= scaleDivisor;
			}
			
			EntityRenderDispatcher renderManager = Minecraft.getInstance().getEntityRenderDispatcher();
			renderManager.setRenderShadow(false);

			entity.setPos(teX, teY, teZ);
			
			poseStack.translate(0.5D, 1.0F, 0.5D);
			poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
			poseStack.scale(entityScale, entityScale, entityScale);
			
			renderManager.render(entity, 0.0D, 0.0D, 0.0D, 0.0F, partialTicks, poseStack, buffer, 0xFFFFFF);
			
			renderManager.setRenderShadow(true);

			poseStack.popPose();
		}
	}

}
