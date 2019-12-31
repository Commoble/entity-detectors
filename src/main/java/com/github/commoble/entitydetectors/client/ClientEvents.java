package com.github.commoble.entitydetectors.client;

import com.github.commoble.entitydetectors.blocks.BlockRegistrar;
import com.github.commoble.entitydetectors.blocks.TileEntityRegistrar;
import com.github.commoble.entitydetectors.items.ImprintedSlimeballItemColor;
import com.github.commoble.entitydetectors.items.ItemRegistrar;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ClientEvents
{
	public static void subscribeClientEvents(IEventBus modBus, IEventBus forgeBus)
	{
		modBus.addListener(ClientEvents::onClientSetup);
	}
	
	public static void onClientSetup(FMLClientSetupEvent event)
	{
		RenderTypeLookup.setRenderLayer(BlockRegistrar.FAKE_SLIME, RenderType.func_228645_f_());
		ClientRegistry.bindTileEntityRenderer(TileEntityRegistrar.MOB_DETECTOR, MobDetectorTileEntityRenderer::new);
		Minecraft.getInstance().getItemColors().register(new ImprintedSlimeballItemColor(), ItemRegistrar.IMPRINTED_SLIME_BALL);
	}
}