package net.commoble.entitydetectors;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import net.commoble.entitydetectors.client.ClientProxy;
import net.commoble.entitydetectors.registrables.EntityDetectorBlock;
import net.commoble.entitydetectors.registrables.ImprintedSlimeballItem;
import net.commoble.entitydetectors.registrables.MobDetectorBlock;
import net.commoble.entitydetectors.registrables.MobDetectorBlockEntity;
import net.commoble.entitydetectors.registrables.PlayerDetectorBlock;
import net.commoble.entitydetectors.registrables.PlayerDetectorBlockEntity;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(EntityDetectors.MODID)
public class EntityDetectors
{
	public static final String MODID = "entitydetectors";
	
	public static final class Tags
	{
		private Tags() {}
		public static final class Items
		{
			private Items() {}
			public static final TagKey<Item> MOB_DETECTOR_FILTERS = TagKey.create(Registries.ITEM, id("mob_detector_filters"));
			public static final TagKey<Item> IMPRINTABLE = TagKey.create(Registries.ITEM, id("imprintable"));
		}
	}
	
	public static final CommonConfig COMMON_CONFIG = ConfigHelper.register(MODID, ModConfig.Type.COMMON, CommonConfig::create);
	
	private static final DeferredRegister<Block> BLOCKS = defreg(Registries.BLOCK);
	private static final DeferredRegister<Item> ITEMS = defreg(Registries.ITEM);
	private static final DeferredRegister<CreativeModeTab> TABS = defreg(Registries.CREATIVE_MODE_TAB);
	private static final DeferredRegister<BlockEntityType<?>> BETS = defreg(Registries.BLOCK_ENTITY_TYPE);
	private static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS = defreg(Registries.DATA_COMPONENT_TYPE);
	
	public static final DeferredHolder<Block, EntityDetectorBlock> PLAYER_DETECTOR = registerBlockItem(Names.PLAYER_DETECTOR, BLOCKS, ITEMS,
		() -> BlockBehaviour.Properties.of().strength(3.0F).lightLevel(EntityDetectorBlock::getLightValue).noOcclusion(),
		PlayerDetectorBlock::new);
	public static final DeferredHolder<Block, MobDetectorBlock> MOB_DETECTOR = registerBlockItem(Names.MOB_DETECTOR, BLOCKS, ITEMS,
		() -> BlockBehaviour.Properties.of().strength(3.0F).lightLevel(EntityDetectorBlock::getLightValue).noOcclusion(),
		MobDetectorBlock::new);
	
	public static final DeferredHolder<Item, ImprintedSlimeballItem> IMPRINTED_SLIME_BALL = registerItem(Names.IMPRINTED_SLIME_BALL, ITEMS, ImprintedSlimeballItem::new);
	
	public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = TABS.register(MODID, () -> CreativeModeTab.builder()
		.icon(() -> new ItemStack(MOB_DETECTOR.get()))
		.title(Component.translatable("itemGroup." + MODID))
		.displayItems((params, output) -> {
			output.accept(PLAYER_DETECTOR.get());
			output.accept(MOB_DETECTOR.get());
			output.acceptAll(IMPRINTED_SLIME_BALL.get().getVariants());
		})
		.build());

	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PlayerDetectorBlockEntity>> PLAYER_DETECTOR_BET = BETS.register(Names.PLAYER_DETECTOR, () -> BlockEntityType.Builder.of(PlayerDetectorBlockEntity::create, PLAYER_DETECTOR.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MobDetectorBlockEntity>> MOB_DETECTOR_BET = BETS.register(Names.MOB_DETECTOR, () -> BlockEntityType.Builder.of(MobDetectorBlockEntity::create, MOB_DETECTOR.get()).build(null));
	public static final DeferredHolder<DataComponentType<?>, DataComponentType<Holder<EntityType<?>>>> IMPRINTED_ENTITY = DATA_COMPONENTS.register(Names.IMPRINTED_ENTITY, () -> DataComponentType.<Holder<EntityType<?>>>builder()
		.persistent(BuiltInRegistries.ENTITY_TYPE.holderByNameCodec())
		.networkSynchronized(ByteBufCodecs.holderRegistry(Registries.ENTITY_TYPE))
		.build());
	
	public EntityDetectors(IEventBus modBus)
	{		
		IEventBus forgeBus = NeoForge.EVENT_BUS;
		
		forgeBus.addListener(EntityDetectors::onPlayerInteractWithEntity);
		forgeBus.addListener(EntityDetectors::onPlayerAttackEntity);
		
		if (FMLEnvironment.dist == Dist.CLIENT)
		{
			ClientProxy.subscribeClientEvents(modBus, forgeBus);
		}
	}
	
	public static void onPlayerInteractWithEntity(PlayerInteractEvent.EntityInteract event)
	{
		Player player = event.getEntity();
		ItemStack oldStack = event.getItemStack();
		EntityType<?> entityType = event.getTarget().getType();
		if (player.isCrouching() && onPlayerAttackedOrInteractedWithEntity(player, oldStack, entityType))
		{
			event.setCanceled(true);
			event.setCancellationResult(InteractionResult.SUCCESS);
		}
	}
	
	public static void onPlayerAttackEntity(AttackEntityEvent event)
	{
		Player player = event.getEntity();
		ItemStack oldStack = player.getMainHandItem();
		EntityType<?> entityType = event.getTarget().getType();
		onPlayerAttackedOrInteractedWithEntity(player, oldStack, entityType);
	}
	
	@SuppressWarnings("resource")
	public static boolean onPlayerAttackedOrInteractedWithEntity(Player player, ItemStack oldStack, EntityType<?> entityType)
	{
		
		if (oldStack.is(Tags.Items.IMPRINTABLE) && ImprintedSlimeballItem.isEntityTypeValid(entityType))
		{
			if (!player.level().isClientSide)
			{
				oldStack.shrink(1);
				ItemStack newSlimeStack = ImprintedSlimeballItem.createItemStackForEntityType(entityType);
				if (!player.addItem(newSlimeStack))
				{
					player.drop(newSlimeStack, false);
				}
			}
			
			return true;
		}
		
		return false;
	}

	private static <T> DeferredRegister<T> defreg(ResourceKey<Registry<T>> key)
	{
		DeferredRegister<T> defreg = DeferredRegister.create(key, MODID);
		defreg.register(ModList.get().getModContainerById(MODID).get().getEventBus());
		return defreg;
	}
	
	private static <BLOCK extends Block, ITEM extends BlockItem> DeferredHolder<Block, BLOCK> registerBlockItem(
		String name,
		DeferredRegister<Block> blocks,
		DeferredRegister<Item> items,
		Supplier<Block.Properties> blockProperties,
		Function<Block.Properties, BLOCK> blockFactory,
		Supplier<Item.Properties> itemProperties,
		BiFunction<? super BLOCK, Item.Properties, ITEM> itemFactory)
	{
//		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(items.getNamespace(), name));
		DeferredHolder<Block, BLOCK> blockHolder = blocks.register(name, () -> blockFactory.apply(blockProperties.get()));
		items.register(name, () -> itemFactory.apply(blockHolder.get(), itemProperties.get()));
		return blockHolder;
	}
	
	private static <BLOCK extends Block, ITEM extends BlockItem> DeferredHolder<Block, BLOCK> registerBlockItem(
		String name,
		DeferredRegister<Block> blocks,
		DeferredRegister<Item> items,
		Supplier<Block.Properties> blockProperties,
		Function<Block.Properties, BLOCK> blockFactory)
	{
		return registerBlockItem(name, blocks, items, blockProperties, blockFactory, Item.Properties::new, BlockItem::new);
	}
	
	private static <ITEM extends Item> DeferredHolder<Item, ITEM> registerItem(
		String name,
		DeferredRegister<Item> items,
		Supplier<Item.Properties> itemProperties,
		Function<Item.Properties, ITEM> itemFactory)
	{
//		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(items.getNamespace(), name));
		return items.register(name, () -> itemFactory.apply(itemProperties.get()));
	}
	
	private static <ITEM extends Item> DeferredHolder<Item, ITEM> registerItem(
		String name,
		DeferredRegister<Item> items,
		Function<Item.Properties, ITEM> itemFactory)
	{
		return registerItem(name, items, Item.Properties::new, itemFactory);
	}
	
	public static ResourceLocation id(String path)
	{
		return ResourceLocation.fromNamespaceAndPath(MODID, path);
	}
}
