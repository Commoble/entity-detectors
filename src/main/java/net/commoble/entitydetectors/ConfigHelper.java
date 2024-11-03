/*
The MIT License (MIT)
Copyright (c) 2020 Joseph Bettendorff aka "Commoble"

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:
The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

// DataFixerUpper is Copyright (c) Microsoft Corporation. All rights reserved. Licensed under the MIT license.
*/

package net.commoble.entitydetectors;

import java.util.function.Function;

import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.neoforged.fml.ModList;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;


/**
 * Helpers for creating configs and defining complex objects in configs 
 */
public class ConfigHelper
{
	private ConfigHelper() {} // utility class
	
	static final Logger LOGGER = LogManager.getLogger();
	
	/**
	 * Register a config using a default config filename for your mod.
	 * @param <T> The class of your config implementation
	 * @param modid String of your modid
	 * @param configType Forge config type:
	 * <ul>
	 * <li>SERVER configs are defined by the server and synced to clients; individual configs are generated per-save. Filename will be modid-server.toml
	 * <li>COMMON configs are definable by both server and clients and not synced (they may have different values). Filename will be modid-client.toml
	 * <li>CLIENT configs are defined by clients and not used on the server. Filename will be modid-client.toml.
	 * </ul>
	 * @param configFactory A constructor or factory for your config class
	 * @return An instance of your config class
	 */
	public static <T> T register(
		final String modid,
		final ModConfig.Type configType,
		final Function<ModConfigSpec.Builder, T> configFactory)
	{
		return register(modid, configType, configFactory, null);
	}
	
	/**
	 * Register a config using a custom filename.
	 * @param <T> Your config class
	 * @param modid String of your modid
	 * @param configType Forge config type:
	 * <ul>
	 * <li>SERVER configs are defined by the server and synced to clients; individual configs are generated per-save.
	 * <li>COMMON configs are definable by both server and clients and not synced (they may have different values)
	 * <li>CLIENT configs are defined by clients and not used on the server
	 * </ul>
	 * @param configFactory A constructor or factory for your config class
	 * @param configName Name of your config file. Supports subfolders, e.g. "yourmod/yourconfig".
	 * @return An instance of your config class
	 */
	public static <T> T register(
		final String modid,
		final ModConfig.Type configType,
		final Function<ModConfigSpec.Builder, T> configFactory,
		final @Nullable String configName)
	{
		final var mod = ModList.get().getModContainerById(modid).get();
		final org.apache.commons.lang3.tuple.Pair<T, ModConfigSpec> entry = new ModConfigSpec.Builder()
			.configure(configFactory);
		final T config = entry.getLeft();
		final ModConfigSpec spec = entry.getRight();
		if (configName == null)
		{
			mod.registerConfig(configType,spec);
		}
		else
		{
			mod.registerConfig(configType, spec, configName + ".toml");
		}
		
		return config;
	}
}
