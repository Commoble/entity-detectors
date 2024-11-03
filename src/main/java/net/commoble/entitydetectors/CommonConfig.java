package net.commoble.entitydetectors;

import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.IntValue;

public record CommonConfig(IntValue refreshRate)
{
	public static CommonConfig create(ModConfigSpec.Builder builder)
	{
		return new CommonConfig(
			builder.comment("How often entity detectors will check for entities (in ticks per update)")
				.defineInRange("refresh_rate", 10, 1, Integer.MAX_VALUE)
		);
	}
}
