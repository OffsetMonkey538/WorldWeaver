package org.betterx.wover.feature.api.placed.modifiers;

import org.betterx.wover.feature.impl.placed.modifiers.PlacementModifiersImpl;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

import com.google.common.collect.Maps;

import java.util.Map;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;

public class Offset extends PlacementModifier {
    private static final Map<Direction, Offset> DIRECTIONS = Maps.newHashMap();
    public static final Codec<Offset> CODEC = RecordCodecBuilder.create((instance) -> instance
            .group(
                    Vec3i.CODEC
                            .fieldOf("blocks")
                            .forGetter(cfg -> cfg.offset)
            )
            .apply(instance, Offset::new));

    private final Vec3i offset;

    public Offset(Vec3i offset) {
        this.offset = offset;
    }

    public static Offset inDirection(Direction dir) {
        return DIRECTIONS.get(dir);
    }

    @Override
    public @NotNull Stream<BlockPos> getPositions(
            PlacementContext placementContext,
            RandomSource randomSource,
            BlockPos blockPos
    ) {
        return Stream.of(blockPos.offset(offset));
    }

    @Override
    public @NotNull PlacementModifierType<?> type() {
        return PlacementModifiersImpl.OFFSET;
    }

    static {
        for (Direction d : Direction.values())
            DIRECTIONS.put(d, new Offset(d.getNormal()));
    }
}
