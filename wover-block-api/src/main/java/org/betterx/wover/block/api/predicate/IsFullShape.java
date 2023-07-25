package org.betterx.wover.block.api.predicate;

import org.betterx.wover.block.impl.predicate.BlockPredicatesImpl;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicateType;

import org.jetbrains.annotations.NotNull;

public class IsFullShape implements BlockPredicate {
    public static final IsFullShape HERE = new IsFullShape();
    public static final Codec<IsFullShape> CODEC = RecordCodecBuilder.create(
            instance -> instance
                    .group(
                            Vec3i.offsetCodec(16).optionalFieldOf("offset", Vec3i.ZERO).forGetter((p) -> p.offset)
                    ).apply(instance, IsFullShape::new));

    protected final Vec3i offset;

    private IsFullShape() {
        this(Vec3i.ZERO);
    }

    public IsFullShape(Vec3i offset) {
        super();
        this.offset = offset;
    }

    @Override
    public @NotNull BlockPredicateType<IsFullShape> type() {
        return BlockPredicatesImpl.FULL_SHAPE;
    }

    @Override
    public boolean test(WorldGenLevel worldGenLevel, BlockPos blockPos) {
        BlockState state = worldGenLevel.getBlockState(blockPos.offset(this.offset));
        return state.isCollisionShapeFullBlock(worldGenLevel, blockPos);
    }
}