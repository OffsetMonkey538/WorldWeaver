package org.betterx.wover.config.impl;

import de.ambertation.wunderlib.configs.ConfigFile;
import org.betterx.wover.config.api.Configs;
import org.betterx.wover.core.api.ModCore;

import net.minecraft.resources.ResourceLocation;

import java.util.Hashtable;
import java.util.Map;
import java.util.function.Supplier;

public class ConfigsImpl {
    private static final Map<ResourceLocation, ConfigFile> CONFIGS = new Hashtable<>();

    public static <T extends ConfigFile> T register(
            ModCore mod,
            String category,
            Configs.ConfigSupplier<T> configSupplier
    ) {
        T config = configSupplier.create(mod, category);
        CONFIGS.put(config.location, config);
        return config;
    }

    public static <T extends ConfigFile> T register(
            Supplier<T> configSupplier
    ) {
        T config = configSupplier.get();
        CONFIGS.put(config.location, config);
        return config;
    }

    public static void saveConfigs() {
        CONFIGS.values().forEach(ConfigFile::save);
    }

    @SuppressWarnings("unchecked")
    public static <T extends ConfigFile> T get(ResourceLocation location) {
        return (T) CONFIGS.get(location);
    }
}
