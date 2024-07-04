package org.betterx.wover.biome.api.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;

import java.util.List;
import org.jetbrains.annotations.Nullable;

public record BiomeGenerationDataContainer(List<Climate.ParameterPoint> parameterPoints,
                                           @Nullable TagKey<Biome> intendedPlacement) {
    public static final BiomeGenerationDataContainer EMPTY = new BiomeGenerationDataContainer(List.of(), null);
    public static Codec<BiomeGenerationDataContainer> CODEC = RecordCodecBuilder.create(instance -> instance
            .group(
                    Climate.ParameterPoint.CODEC
                            .listOf()
                            .optionalFieldOf("parameter_points", List.of())
                            .forGetter(BiomeGenerationDataContainer::parameterPoints),
                    TagKey
                            .codec(Registries.BIOME)
                            .optionalFieldOf("intended_placement", null)
                            .forGetter(BiomeGenerationDataContainer::intendedPlacement)
            ).apply(instance, BiomeGenerationDataContainer::new));
}
