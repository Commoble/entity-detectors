package net.commoble.entitydetectors.client;

import java.util.Optional;

import net.commoble.entitydetectors.EntityDetectors;
import net.commoble.entitydetectors.registrables.ImprintedSlimeballItem;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

public class ClientProxy
{
	public static void subscribeClientEvents(IEventBus modBus, IEventBus forgeBus)
	{
		modBus.addListener(ClientProxy::onRegisterEntityRenderers);
		modBus.addListener(ClientProxy::onRegisterItemColors);
	}
	
	private static void onRegisterEntityRenderers(EntityRenderersEvent.RegisterRenderers event)
	{
		event.registerBlockEntityRenderer(EntityDetectors.MOB_DETECTOR_BET.get(), MobDetectorBlockEntityRenderer::new);
	}
	
	private static void onRegisterItemColors(RegisterColorHandlersEvent.Item event)
	{
		event.register(ClientProxy::getImprintedSlimeballColor, EntityDetectors.IMPRINTED_SLIME_BALL.get());
	}
	
	private static int getImprintedSlimeballColor(ItemStack stack, int tintIndex)
	{
		return ImprintedSlimeballItem.getEntityType(stack)
			.flatMap(holder -> Optional.ofNullable(SpawnEggItem.byId(holder.value())))
			.map(egg -> FastColor.ARGB32.opaque(egg.getColor(tintIndex)))
			.orElse(0xFFFFFFFF); // no tint
	}
}
