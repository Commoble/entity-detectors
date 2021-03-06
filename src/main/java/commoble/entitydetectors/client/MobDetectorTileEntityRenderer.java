package commoble.entitydetectors.client;

import com.mojang.blaze3d.matrix.MatrixStack;

import commoble.entitydetectors.blocks.MobDetectorTileEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.World;

public class MobDetectorTileEntityRenderer extends TileEntityRenderer<MobDetectorTileEntity>
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

	public MobDetectorTileEntityRenderer(TileEntityRendererDispatcher p_i226006_1_)
	{
		super(p_i226006_1_);
	}

	@Override
	public void render(MobDetectorTileEntity te, float partialTicks, MatrixStack matrix, IRenderTypeBuffer buffer, int intA, int intB)
	{
		ClientPlayerEntity player = Minecraft.getInstance().player;
		if (player != null && te.getWorld().isAirBlock(te.getPos().up()))
		{
			World world = te.getWorld();
			FakeClientEntities.getOptionalFakeEntity(te.getFilteredEntityType(), world)
				.ifPresent(entity -> this.renderFakeEntity(te, player, entity, partialTicks, matrix, buffer, intA));
		}
	}
	
	public void renderFakeEntity(MobDetectorTileEntity te, ClientPlayerEntity player, Entity entity, float partialTicks, MatrixStack matrix, IRenderTypeBuffer buffer, int intA)
	{
		BlockPos pos = te.getPos();
		double teX = pos.getX() + 0.5D;
		double teY = pos.getY() + 0.5D;
		double teZ = pos.getZ() + 0.5D;
		
		// player position is only updated on game tick, add extra offset based on velocity for more smoothness
		Vector3d vel = player.getMotion();
		double extraX = vel.getX() * partialTicks;
		double extraY = vel.getY() * partialTicks;
		double extraZ = vel.getZ() * partialTicks;
		double playerDistanceSq = player.getDistanceSq(teX - extraX, teY - extraY, teZ - extraZ);
		if (playerDistanceSq < MAX_RENDER_DISTANCE_SQ)
		{
			double distFactor = 1 - (Math.min(playerDistanceSq - MIN_RENDER_DISTANCE_SQ, DISTANCE_DIFF) / DISTANCE_DIFF);
			
			long gameTicks = te.getWorld().getGameTime();
			
			float renderTicks = gameTicks + partialTicks;
			
			float rotation = renderTicks * DEGREES_PER_TICK % 360F;
			
			
//			// render slime TODO
//			matrix.func_227860_a_();	// push
//
//			float slimeScale = Math.min(0.999F, (float)distFactor);
//			
//			float translation = (1F-slimeScale)*0.5F;
//
//			matrix.func_227861_a_(translation, 0.999F, translation);	// translation
//			matrix.func_227862_a_(slimeScale, slimeScale, slimeScale);	// scale
//			
//			BlockRendererDispatcher blockDispatcher = Minecraft.getInstance().getBlockRendererDispatcher();
//			BlockState slimeState = BlockRegistrar.FAKE_SLIME.getDefaultState();
//			blockDispatcher.func_228791_a_(slimeState, matrix, buffer, intA, OverlayTexture.field_229196_a_);
//			
//			
//			matrix.func_227865_b_(); // pop
			
			
			// render entity
			// based on MobSpawnerTileEntityRenderer
			matrix.push();

			float entityScale = 0.53125F;
			
			entityScale *= 0.8F * distFactor;
			float scaleDivisor = Math.max(entity.getWidth(), entity.getHeight());
			if (scaleDivisor > 1.0D)
			{
				entityScale /= scaleDivisor;
			}
			
			EntityRendererManager renderManager = Minecraft.getInstance().getRenderManager();
			renderManager.setRenderShadow(false);

			entity.setPosition(teX, teY, teZ);
			
			matrix.translate(0.5D, 1.0F, 0.5D);
			matrix.rotate(Vector3f.YP.rotationDegrees(rotation));
			matrix.scale(entityScale, entityScale, entityScale);
			
			renderManager.renderEntityStatic(entity, 0.0D, 0.0D, 0.0D, 0.0F, partialTicks, matrix, buffer, intA);
			
			renderManager.setRenderShadow(true);

			matrix.pop();	// pop
		}
	}

}
