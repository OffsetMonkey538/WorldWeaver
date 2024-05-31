package org.betterx.wover.biome.impl.modification.predicates;

import org.betterx.wover.biome.api.modification.predicates.BiomePredicate;
import org.betterx.wover.biome.api.modification.predicates.BiomePredicateRegistry;
import org.betterx.wover.core.api.registry.BuiltInRegistryManager;
import org.betterx.wover.entrypoint.LibWoverBiome;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.KeyDispatchDataCodec;

import org.jetbrains.annotations.ApiStatus;

public class BiomePredicateRegistryImpl {
    public static final Registry<MapCodec<? extends BiomePredicate>> BIOME_PREDICATES = BuiltInRegistryManager.createRegistry(
            BiomePredicateRegistry.BIOME_PREDICATE_REGISTRY,
            BiomePredicateRegistryImpl::onBootstrap
    );

    public static MapCodec<? extends BiomePredicate> register(
            Registry<MapCodec<? extends BiomePredicate>> registry,
            ResourceLocation location,
            KeyDispatchDataCodec<? extends BiomePredicate> keyDispatchDataCodec
    ) {
        return Registry.register(registry, location, keyDispatchDataCodec.codec());
    }

    @ApiStatus.Internal
    public static void initialize() {
        onBootstrap(BIOME_PREDICATES);

//        BiomeModifications.addFeature(
//                ctx -> ctx.canGenerateIn(LevelStem.OVERWORLD),
//                GenerationStep.Decoration.LAKES,
//                MiscOverworldPlacements.BLUE_ICE
//        );
    }

    private static MapCodec<? extends BiomePredicate> onBootstrap(Registry<MapCodec<? extends BiomePredicate>> registry) {
        final var all = LibWoverBiome.C.id("all");
        if (registry.containsKey(all)) {
            return registry.get(all);
        }
        register(registry, LibWoverBiome.C.id("not"), Not.CODEC);
        register(registry, LibWoverBiome.C.id("and"), And.CODEC);
        register(registry, LibWoverBiome.C.id("or"), Or.CODEC);

        register(registry, LibWoverBiome.C.id("is_biome"), IsBiome.CODEC);
        register(registry, LibWoverBiome.C.id("has_tag"), HasTag.CODEC);
        register(registry, LibWoverBiome.C.id("in_dimension"), InDimension.CODEC);
        register(registry, LibWoverBiome.C.id("is_namespace"), IsNamespace.CODEC);
        register(registry, LibWoverBiome.C.id("spawns"), Spawns.CODEC);
        register(registry, LibWoverBiome.C.id("has_structure"), HasStructure.CODEC);
        register(registry, LibWoverBiome.C.id("has_placed_feature"), HasPlacedFeature.CODEC);
        register(registry, LibWoverBiome.C.id("has_configured_feature"), HasConfiguredFeature.CODEC);
        register(registry, LibWoverBiome.C.id("config_is"), ConfigIs.CODEC);

        return register(registry, all, Always.CODEC);
    }


}
