package org.betterx.wover.biome.api.builder;

import de.ambertation.wunderlib.ui.ColorHelper;
import org.betterx.wover.biome.api.BiomeKey;
import org.betterx.wover.biome.api.data.BiomeData;
import org.betterx.wover.biome.impl.builder.BiomeSurfaceRuleBuilderImpl;
import org.betterx.wover.biome.mixin.BiomeGenerationSettingsAccessor;
import org.betterx.wover.feature.api.placed.BasePlacedFeatureKey;
import org.betterx.wover.feature.api.placed.PlacedFeatureManager;
import org.betterx.wover.structure.api.StructureKey;
import org.betterx.wover.surface.api.AssignedSurfaceRule;
import org.betterx.wover.tag.api.event.context.TagBootstrapContext;
import org.betterx.wover.tag.api.predefined.CommonBiomeTags;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BiomeDefaultFeatures;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.biome.OverworldBiomes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class BiomeBuilder<B extends BiomeBuilder<B>> {
    public final BiomeKey<B> key;
    public final BiomeBootstrapContext bootstrapContext;

    public static int calculateSkyColor(float temperature) {
        return OverworldBiomes.calculateSkyColor(temperature);
    }

    public static int DEFAULT_WATER_FOG_COLOR = 0x050533;
    public static int DEFAULT_WATER_COLOR = 0x3F76E4;
    public static int DEFAULT_NETHER_WATER_COLOR = DEFAULT_WATER_COLOR;
    public static int DEFAULT_END_WATER_COLOR = DEFAULT_WATER_COLOR;
    public static int DEFAULT_NETHER_WATER_FOG_COLOR = 0x050533;
    public static int DEFAULT_END_WATER_FOG_COLOR = DEFAULT_NETHER_WATER_FOG_COLOR;
    public static int DEFAULT_FOG_COLOR = 0xC0D8FF;
    public static int DEFAULT_END_FOG_COLOR = 0xA080A0;
    public static int DEFAULT_END_SKY_COLOR = 0x000000;
    public static float DEFAULT_NETHER_TEMPERATURE = 2.0f;
    public static float DEFAULT_END_TEMPERATURE = 0.5f;
    public static float DEFAULT_NETHER_WETNESS = 0.0f;
    public static float DEFAULT_END_WETNESS = 0.5f;

    protected final List<Climate.ParameterPoint> parameters = new ArrayList<>(1);

    protected @Nullable TagKey<Biome> intendedPlacement = null;
    protected float fogDensity;
    protected final List<TagKey<Biome>> biomeTags = new ArrayList<>(2);

    private @Nullable BiomeSurfaceRuleBuilderImpl<B> surfaceBuilder;


    protected BiomeBuilder(BiomeBootstrapContext context, BiomeKey<B> key) {
        this.key = key;
        this.bootstrapContext = context;
        this.fogDensity = 1.0f;
    }

    public B addClimate(Climate.ParameterPoint point) {
        parameters.add(point);
        return (B) this;
    }

    public B addNetherClimate(float temperature, float humidity, float offset) {
        return addClimate(Climate.parameters(temperature, humidity, 0, 0, 0, 0, offset));
    }

    public B addNetherClimate(float temperature, float humidity) {
        return addNetherClimate(temperature, humidity, 0);
    }

    public B fogDensity(float density) {
        this.fogDensity = density;
        return (B) this;
    }

    public B structure(StructureKey<?, ?, ?> structure) {
        return tag(structure.biomeTag());
    }

    public B structure(TagKey<Biome> structureTag) {
        return tag(structureTag);
    }

    /**
     * Adds a biome tag to the biome and sets it as the intended placement for this biome.
     * <p>
     * The intended placement is used to determine where a Biome is placed in a dimension.
     *
     * @param tag The tag to add and set as the intended placement.
     * @return The builder.
     */
    protected B biomeTypeTag(TagKey<Biome> tag) {
        if (intendedPlacement == null && tag != null) {
            intendedPlacement = tag;
        }
        return tag(tag);
    }

    @SafeVarargs
    protected final B tag(TagKey<Biome>... tags) {
        for (TagKey<Biome> biomeTag : tags) {
            if (biomeTag != null && !biomeTags.contains(biomeTag))
                biomeTags.add(biomeTag);
        }

        return (B) this;
    }

    public BiomeSurfaceRuleBuilder<B> startSurface() {
        surfaceBuilder = new BiomeSurfaceRuleBuilderImpl<>(key, (B) this);
        return surfaceBuilder;
    }

    public B surface(BlockState state) {
        return startSurface().surface(state).finishSurface();
    }

    public B surface(Block block) {
        return startSurface().surface(block).finishSurface();
    }

    public B surface(BlockState top, BlockState under) {
        return startSurface().surface(top).subsurface(under, 3).finishSurface();
    }

    public B surface(Block top, Block under) {
        return startSurface().surface(top).subsurface(under, 3).finishSurface();
    }

    public B intendedPlacement(TagKey<Biome> biome) {
        this.intendedPlacement = biome;
        return (B) this;
    }

    public void register() {
        bootstrapContext.register(this);
    }

    public abstract void registerBiome(BootstrapContext<Biome> biomeContext);

    public abstract void registerBiomeData(BootstrapContext<BiomeData> dataContext);

    public void registerBiomeTags(TagBootstrapContext<Biome> context) {
        for (TagKey<Biome> biomeTag : biomeTags) {
            context.add(biomeTag, key.key);
        }
    }

    public void registerSurfaceRule(@NotNull BootstrapContext<AssignedSurfaceRule> context) {
        if (surfaceBuilder != null) {
            surfaceBuilder.register(context);
        }
    }

    public abstract static class VanillaBuilder<B extends VanillaBuilder<B>> extends BiomeBuilder<B> {
        private Biome.TemperatureModifier temperatureModifier;
        private float downfall;
        private float temperature;
        private boolean hasPrecipitation;
        private final BiomeSpecialEffects.Builder fx = new BiomeSpecialEffects.Builder();
        private final BiomeGenerationSettings.Builder generationSettings;
        private final MobSpawnSettings.Builder mobSpawnSettings = new MobSpawnSettings.Builder();

        protected VanillaBuilder(BiomeBootstrapContext context, BiomeKey<B> key) {
            super(context, key);

            this.temperatureModifier = Biome.TemperatureModifier.NONE;
            this.downfall = 0.f;
            this.temperature = 0.5f;
            this.hasPrecipitation = false;

            generationSettings = new BiomeGenerationSettings.Builder(
                    bootstrapContext.lookup(Registries.PLACED_FEATURE),
                    bootstrapContext.lookup(Registries.CONFIGURED_CARVER)
            );

            fx.fogColor(DEFAULT_FOG_COLOR);
            fx.waterFogColor(DEFAULT_WATER_FOG_COLOR);
            fx.waterColor(DEFAULT_WATER_COLOR);
            fx.skyColor(calculateSkyColor(temperature));
        }


        public B hasPrecipitation(boolean bl) {
            this.hasPrecipitation = bl;
            return (B) this;
        }

        public B temperature(float f) {
            this.temperature = f;
            return (B) this;
        }

        public B downfall(float f) {
            this.downfall = f;
            return (B) this;
        }

        public B temperatureAdjustment(Biome.TemperatureModifier temperatureModifier) {
            this.temperatureModifier = temperatureModifier;
            return (B) this;
        }

        public B temperatureFrozen() {
            return this.temperatureAdjustment(Biome.TemperatureModifier.FROZEN);
        }

        public B temperatureRegular() {
            return this.temperatureAdjustment(Biome.TemperatureModifier.NONE);
        }

        public B feature(BasePlacedFeatureKey<?> feature) {
            generationSettings.addFeature(
                    feature.getDecoration(),
                    feature.getHolder(bootstrapContext.lookup(Registries.PLACED_FEATURE))
            );
            return (B) this;
        }

        public B feature(GenerationStep.Decoration decoration, ResourceKey<PlacedFeature> feature) {
            generationSettings.addFeature(
                    decoration,
                    PlacedFeatureManager.getHolder(bootstrapContext.lookup(Registries.PLACED_FEATURE), feature)
            );
            return (B) this;
        }

        public B feature(GenerationStep.Decoration decoration, Holder<PlacedFeature> feature) {
            generationSettings.addFeature(decoration, feature);
            return (B) this;
        }

        /**
         * Will add features into biome, used for vanilla feature adding functions.
         *
         * @param featureAdd {@link Consumer} with {@link BiomeGenerationSettings.Builder}.
         * @return same builder.
         */
        public B feature(Consumer<BiomeGenerationSettings.Builder> featureAdd) {
            featureAdd.accept(generationSettings);
            return (B) this;
        }

        /**
         * Adds vanilla Mushrooms.
         *
         * @return same builder.
         */
        public B defaultMushrooms() {
            return feature(BiomeDefaultFeatures::addDefaultMushrooms);
        }

        /**
         * Adds vanilla Nether Ores.
         *
         * @return same builder.
         */
        public B netherDefaultOres() {
            return feature(BiomeDefaultFeatures::addNetherDefaultOres);
        }

        public B carver(GenerationStep.Carving step, ResourceKey<ConfiguredWorldCarver<?>> carver) {
            generationSettings.addCarver(
                    step,
                    bootstrapContext.lookup(Registries.CONFIGURED_CARVER).getOrThrow(carver)
            );
            return (B) this;
        }

        public B carver(GenerationStep.Carving step, Holder<ConfiguredWorldCarver<?>> carver) {
            generationSettings.addCarver(step, carver);
            return (B) this;
        }

        public B fogColor(int color) {
            fx.fogColor(color);
            return (B) this;
        }

        public B fogColor(int r, int g, int b) {
            fx.fogColor(ColorHelper.color(r, g, b));
            return (B) this;
        }

        public B waterColor(int r, int g, int b) {
            return waterColor(ColorHelper.color(r, g, b));
        }

        public B waterColor(int color) {
            fx.waterColor(color);
            return (B) this;
        }

        public B waterFogColor(int r, int g, int b) {
            return waterFogColor(ColorHelper.color(r, g, b));
        }

        public B waterFogColor(int color) {
            fx.waterFogColor(color);
            return (B) this;
        }

        public B skyColor(int r, int g, int b) {
            return skyColor(ColorHelper.color(r, g, b));
        }

        public B skyColor(int color) {
            fx.skyColor(color);
            return (B) this;
        }

        public B foliageColorOverride(int r, int g, int b) {
            return foliageColorOverride(ColorHelper.color(r, g, b));
        }

        public B foliageColorOverride(int color) {
            fx.foliageColorOverride(color);
            return (B) this;
        }

        public B grassColorOverride(int r, int g, int b) {
            return grassColorOverride(ColorHelper.color(r, g, b));
        }

        public B grassColorOverride(int color) {
            fx.grassColorOverride(color);
            return (B) this;
        }

        public B grassColorModifier(BiomeSpecialEffects.GrassColorModifier grassColorModifier) {
            fx.grassColorModifier(grassColorModifier);
            return (B) this;
        }

        public B waterAndFogColor(int color) {
            return waterColor(color).waterFogColor(color);
        }

        public B waterAndFogColor(int r, int g, int b) {
            return waterAndFogColor(ColorHelper.color(r, g, b));
        }

        public B plantsColor(int r, int g, int b) {
            return plantsColor(ColorHelper.color(r, g, b));
        }

        public B plantsColor(int color) {
            return grassColorOverride(color).foliageColorOverride(color);
        }


        /**
         * Adds ambient particles .
         *
         * @param particle    {@link ParticleOptions} particles (or {@link net.minecraft.core.particles.ParticleType}).
         * @param probability particle spawn probability, should have low value (example: 0.01F).
         * @return this builder.
         */
        public B particles(ParticleOptions particle, float probability) {
            particles(new AmbientParticleSettings(particle, probability));
            return (B) this;
        }

        public B particles(AmbientParticleSettings ambientParticleSettings) {
            fx.ambientParticle(ambientParticleSettings);
            return (B) this;
        }


        public B loop(Holder<SoundEvent> holder) {
            fx.ambientLoopSound(holder);
            return (B) this;
        }

        public B mood(AmbientMoodSettings ambientMoodSettings) {
            fx.ambientMoodSound(ambientMoodSettings);
            return (B) this;
        }

        public B mood(Holder<SoundEvent> mood) {
            return mood(mood, 6000, 8, 2.0F);
        }

        public B mood(Holder<SoundEvent> mood, int tickDelay, int blockSearchExtent, float soundPositionOffset) {
            return mood(new AmbientMoodSettings(mood, tickDelay, blockSearchExtent, soundPositionOffset));
        }

        public B additions(AmbientAdditionsSettings ambientAdditionsSettings) {
            fx.ambientAdditionsSound(ambientAdditionsSettings);
            return (B) this;
        }

        public B additions(Holder<SoundEvent> additions, float intensity) {
            return additions(new AmbientAdditionsSettings(additions, intensity));
        }

        public B additions(Holder<SoundEvent> additions) {
            return additions(additions, 0.0111F);
        }

        public B music(@Nullable Music music) {
            fx.backgroundMusic(music);
            return (B) this;
        }

        public B music(Holder<SoundEvent> music) {
            return music(music, 600, 2400, true);
        }

        public B music(Holder<SoundEvent> music, int minDelay, int maxDelay, boolean replaceCurrentMusic) {
            return music(new Music(music, minDelay, maxDelay, replaceCurrentMusic));
        }

        public final B isNetherBiome() {
            return biomeTypeTag(BiomeTags.IS_NETHER);
        }

        public final B isEndHighlandBiome() {
            return biomeTypeTag(CommonBiomeTags.IS_END_HIGHLAND);
        }

        public final B isEndMidlandBiome() {
            return biomeTypeTag(CommonBiomeTags.IS_END_MIDLAND);
        }

        public final B isEndCenterIslandBiome() {
            return biomeTypeTag(CommonBiomeTags.IS_END_CENTER);
        }

        public final B isEndBarrensBiome() {
            return biomeTypeTag(CommonBiomeTags.IS_END_BARRENS);
        }

        public final B isEndSmallIslandBiome() {
            return biomeTypeTag(CommonBiomeTags.IS_SMALL_END_ISLAND);
        }

        public B spawn(EntityType<?> entityType, int weight, int minGroupCount, int maxGroupCount) {
            mobSpawnSettings.addSpawn(
                    entityType.getCategory(),
                    new MobSpawnSettings.SpawnerData(entityType, weight, minGroupCount, maxGroupCount)
            );
            return (B) this;
        }

        public B addMobCharge(EntityType<?> entityType, double energyBudget, double charge) {
            mobSpawnSettings.addMobCharge(entityType, energyBudget, charge);
            return (B) this;
        }

        public B creatureGenerationProbability(float p) {
            mobSpawnSettings.creatureGenerationProbability(p);
            return (B) this;
        }

        public void register() {
            bootstrapContext.register(this);
        }

        public void registerBiome(BootstrapContext<Biome> biomeContext) {
            biomeContext.register(key.key, buildBiome());
        }

        public abstract void registerBiomeData(BootstrapContext<BiomeData> dataContext);


        private static BiomeGenerationSettings fixGenerationSettings(BiomeGenerationSettings settings) {
            //Fabric Biome Modification API can not handle an empty carver map, thus we will create one with
            //an empty HolderSet for every possible step:
            //https://github.com/FabricMC/fabric/issues/2079
            //TODO: Remove, once fabric gets fixed
            if (settings instanceof BiomeGenerationSettingsAccessor acc) {
                Map<GenerationStep.Carving, HolderSet<ConfiguredWorldCarver<?>>> carvers = new HashMap<>(acc.wover_getCarvers());
                for (GenerationStep.Carving step : GenerationStep.Carving.values()) {
                    carvers.computeIfAbsent(step, __ -> HolderSet.direct(Lists.newArrayList()));
                }
                acc.wover_setCarvers(Map.copyOf(carvers));
            }
            return settings;
        }

        protected Biome buildBiome() {
            Biome.BiomeBuilder vanillaBuilder = new Biome.BiomeBuilder();

            vanillaBuilder.hasPrecipitation(hasPrecipitation);
            vanillaBuilder.downfall(downfall);
            vanillaBuilder.temperature(temperature);
            vanillaBuilder.temperatureAdjustment(temperatureModifier);

            vanillaBuilder.generationSettings(fixGenerationSettings(generationSettings.build()));
            vanillaBuilder.specialEffects(fx.build());
            vanillaBuilder.mobSpawnSettings(mobSpawnSettings.build());

            return vanillaBuilder.build();
        }
    }

    public abstract static class Vanilla extends VanillaBuilder<Vanilla> {
        protected Vanilla(BiomeBootstrapContext context, BiomeKey<Vanilla> key) {
            super(context, key);
        }
    }

    public abstract static class Wrapped extends BiomeBuilder<Wrapped> {
        protected Wrapped(BiomeBootstrapContext context, BiomeKey<Wrapped> key) {
            super(context, key);
        }
    }
}
