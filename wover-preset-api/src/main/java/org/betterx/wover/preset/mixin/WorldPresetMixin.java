package org.betterx.wover.preset.mixin;

import org.betterx.wover.preset.impl.SortableWorldPresetImpl;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.presets.WorldPreset;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@Mixin(WorldPreset.class)
public class WorldPresetMixin {
    @ModifyArg(method = "<clinit>", at = @At(value = "INVOKE", target = "Lcom/mojang/serialization/codecs/RecordCodecBuilder;create(Ljava/util/function/Function;)Lcom/mojang/serialization/Codec;"))
    private static Function<RecordCodecBuilder.Instance<WorldPreset>, ? extends App<RecordCodecBuilder.Mu<WorldPreset>, WorldPreset>> wover_extendCodec(
            Function<RecordCodecBuilder.Instance<WorldPreset>, ? extends App<RecordCodecBuilder.Mu<WorldPreset>, WorldPreset>> builder
    ) {
        final Function<RecordCodecBuilder.Instance<WorldPreset>, App<RecordCodecBuilder.Mu<WorldPreset>, WorldPreset>> CODEC_FUNCTION = builderInstance -> {
            RecordCodecBuilder<WorldPreset, Map<ResourceKey<LevelStem>, LevelStem>> dimensionsBuilder = Codec
                    .unboundedMap(
                            ResourceKey.codec(Registries.LEVEL_STEM),
                            LevelStem.CODEC
                    )
                    .fieldOf("dimensions")
                    .forGetter((wp) -> (wp instanceof WorldPresetAccessor)
                            ? ((WorldPresetAccessor) wp).wover_getDimensions()
                            : null);

            RecordCodecBuilder<WorldPreset, Optional<Integer>> sortBuilder = Codec.INT
                    .optionalFieldOf("sort_order")
                    .forGetter(wp -> (wp instanceof SortableWorldPresetImpl)
                            ? Optional.of(((SortableWorldPresetImpl) wp).sortOrder)
                            : Optional.empty());

            return builderInstance
                    .group(dimensionsBuilder, sortBuilder)
                    .apply(builderInstance, SortableWorldPresetImpl::new);
        };

        return CODEC_FUNCTION;
    }
}
