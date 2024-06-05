package org.betterx.wover.tag.impl;

import org.betterx.wover.entrypoint.LibWoverTag;
import org.betterx.wover.state.api.WorldState;
import org.betterx.wover.tag.api.TagRegistry;
import org.betterx.wover.tag.api.event.context.ItemTagBootstrapContext;
import org.betterx.wover.tag.api.event.context.TagBootstrapContext;

import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagLoader;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.ApiStatus;

public class TagManagerImpl {
    private static final Map<String, TagRegistryImpl<?, ?>> TYPES = Maps.newHashMap();

    public static TagRegistryImpl<Block, TagBootstrapContext<Block>> BLOCKS = registerType(BuiltInRegistries.BLOCK);
    public static TagRegistryImpl<Item, ItemTagBootstrapContext> ITEMS = registerItem();
    public static BiomeTagRegistryImpl BIOMES = registerBiome();

    public static <T, P extends TagBootstrapContext<T>> TagRegistryImpl<T, P> registerType(DefaultedRegistry<T> registry) {
        TagRegistrySimple<T> type = new TagRegistrySimple<>(registry);
        return (TagRegistryImpl<T, P>) TYPES.computeIfAbsent(type.directory, (dir) -> type);
    }

    public static <T, P extends TagBootstrapContext<T>> TagRegistryImpl<T, P> registerType(
            Registry<T> registry,
            String directory
    ) {
        return registerType(registry.key(), directory, (o) -> registry.getKey(o));
    }

    public static <T, P extends TagBootstrapContext<T>> TagRegistryImpl<T, P> registerType(
            ResourceKey<? extends Registry<T>> registry,
            String directory,
            TagRegistry.LocationProvider<T> locationProvider
    ) {
        return (TagRegistryImpl<T, P>) TYPES.computeIfAbsent(
                directory,
                (dir) -> new TagRegistryImpl<>(
                        registry,
                        dir,
                        locationProvider
                ) {
                    @Override
                    public TagBootstrapContext<T> createBootstrapContext(boolean initAll) {
                        return TagBootstrapContextImpl.create(this, initAll);
                    }
                }
        );
    }

    public static <T, P extends TagBootstrapContext<T>> TagRegistryImpl<T, P> registerType(
            ResourceKey<? extends Registry<T>> registryKey
    ) {
        return registerType(
                registryKey,
                Registries.tagsDirPath(registryKey),
                (preset) -> WorldState.registryAccess() != null
                        ? WorldState.registryAccess()
                                    .registryOrThrow(registryKey)
                                    .getKey(preset)
                        : null
        );
    }

    public static TagRegistryImpl<Item, ItemTagBootstrapContext> registerItem() {
        ItemTagRegistryImpl type = new ItemTagRegistryImpl();
        return (TagRegistryImpl<Item, ItemTagBootstrapContext>) TYPES.computeIfAbsent(type.directory, (dir) -> type);
    }

    static BiomeTagRegistryImpl registerBiome() {
        return (BiomeTagRegistryImpl) TYPES.computeIfAbsent(
                "tags/worldgen/biome",
                (dir) -> new BiomeTagRegistryImpl(
                        dir,
                        b -> WorldState.getBiomeID(b)
                )
        );
    }

    @ApiStatus.Internal
    public static Map<ResourceLocation, List<TagLoader.EntryWithSource>> didLoadTagMap(
            String directory,
            Map<ResourceLocation, List<TagLoader.EntryWithSource>> tagsMap
    ) {
        TagRegistryImpl<?, ?> type = TYPES.get(directory);
        if (type != null) {
            TagBootstrapContext<?> provider = type.emitLoadEvent(false);

            provider.forEach((tag, entries) -> {
                List<TagLoader.EntryWithSource> builder = tagsMap.computeIfAbsent(
                        tag.location(),
                        key -> Lists.newArrayList()
                );

                entries.forEach(wrapper -> {
                    builder.add(new TagLoader.EntryWithSource(wrapper.createTagEntry(), LibWoverTag.C.namespace));
                });
            });
        }

        return tagsMap;
    }

    final static private ResourceLocation NO_TAG = LibWoverTag.C.mk("no_tag");

    public static <T> StreamCodec<RegistryFriendlyByteBuf, TagKey<T>> streamCodec(ResourceKey<Registry<T>> registry) {
        return new StreamCodec<RegistryFriendlyByteBuf, TagKey<T>>() {

            public TagKey<T> decode(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
                ResourceLocation location = ResourceLocation.STREAM_CODEC.decode(registryFriendlyByteBuf);
                return TagKey.create(registry, location.equals(NO_TAG) ? null : location);
            }

            public void encode(RegistryFriendlyByteBuf registryFriendlyByteBuf, TagKey<T> tag) {
                ResourceLocation.STREAM_CODEC.encode(registryFriendlyByteBuf, tag == null ? NO_TAG : tag.location());
            }
        };
    }
}
