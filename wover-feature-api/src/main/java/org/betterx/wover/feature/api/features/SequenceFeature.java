package org.betterx.wover.feature.api.features;

import org.betterx.wover.feature.api.features.config.SequenceFeatureConfig;

import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

public class SequenceFeature extends Feature<SequenceFeatureConfig> {
    public SequenceFeature() {
        super(SequenceFeatureConfig.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<SequenceFeatureConfig> featurePlaceContext) {
        return featurePlaceContext.config().placeAll(featurePlaceContext);
    }
}
