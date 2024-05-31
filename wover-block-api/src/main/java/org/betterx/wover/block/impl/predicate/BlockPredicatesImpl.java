package org.betterx.wover.block.impl.predicate;

import org.betterx.wover.block.api.predicate.IsFullShape;
import org.betterx.wover.entrypoint.LibWoverBlockAndItem;
import org.betterx.wover.legacy.api.LegacyHelper;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicateType;

public class BlockPredicatesImpl {
    public static final BlockPredicateType<IsFullShape> FULL_SHAPE = register(
            LibWoverBlockAndItem.C.id("full_shape"),
            IsFullShape.CODEC
    );

    public static <P extends BlockPredicate> BlockPredicateType<P> register(
            ResourceLocation location,
            MapCodec<P> codec
    ) {
        return Registry.register(BuiltInRegistries.BLOCK_PREDICATE_TYPE, location, () -> codec);
    }

    public static void ensureStaticInitialization() {

    }

    static {
        if (LegacyHelper.isLegacyEnabled()) {
            final BlockPredicateType<IsFullShape> FULL_SHAPE_LEGACY = register(
                    LegacyHelper.BCLIB_CORE.id("full_shape"),
                    IsFullShape.CODEC
            );
        }
    }
}
