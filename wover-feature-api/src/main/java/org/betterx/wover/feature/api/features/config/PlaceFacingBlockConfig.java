package org.betterx.wover.feature.api.features.config;

import org.betterx.wover.block.api.BlockHelper;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.WeightedStateProvider;

import java.util.List;

public class PlaceFacingBlockConfig extends PlaceBlockFeatureConfig {
    public static final Codec<PlaceFacingBlockConfig> CODEC = RecordCodecBuilder.create(instance -> instance
            .group(
                    blockStateCodec(),
                    ExtraCodecs.nonEmptyList(Direction.CODEC.listOf())
                               .fieldOf("dir")
                               .orElse(List.of(Direction.NORTH))
                               .forGetter(a -> a.directions)
            ).apply(instance, PlaceFacingBlockConfig::new)
    );

    private final List<Direction> directions;

    public PlaceFacingBlockConfig(Block block, List<Direction> dir) {
        this(block.defaultBlockState(), dir);
    }

    public PlaceFacingBlockConfig(BlockState state, List<Direction> dir) {
        this(BlockStateProvider.simple(state), dir);
    }

    public PlaceFacingBlockConfig(List<BlockState> states, List<Direction> dir) {
        this(buildWeightedList(states), dir);
    }

    public PlaceFacingBlockConfig(SimpleWeightedRandomList<BlockState> blocks, List<Direction> dir) {
        this(new WeightedStateProvider(blocks), dir);
    }

    public PlaceFacingBlockConfig(BlockStateProvider blocks, List<Direction> dir) {
        super(blocks);
        directions = dir;
    }

    @Override
    public boolean placeBlock(
            FeaturePlaceContext<? extends PlaceBlockFeatureConfig> ctx,
            WorldGenLevel level,
            BlockPos pos,
            BlockState targetState
    ) {
        BlockState lookupState;
        BlockPos testPos;
        for (Direction dir : directions) {
            testPos = pos.relative(dir);
            lookupState = targetState.setValue(HorizontalDirectionalBlock.FACING, dir);
            if (level.getBlockState(testPos).isAir() && lookupState.canSurvive(level, testPos)) {
                lookupState.canSurvive(level, testPos);
                level.setBlock(testPos, lookupState, BlockHelper.SET_SILENT);
                return true;
            }
        }

        return false;
    }
}
