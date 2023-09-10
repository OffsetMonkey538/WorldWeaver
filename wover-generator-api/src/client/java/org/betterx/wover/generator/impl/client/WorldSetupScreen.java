package org.betterx.wover.generator.impl.client;

import de.ambertation.wunderlib.ui.ColorHelper;
import de.ambertation.wunderlib.ui.layout.components.*;
import de.ambertation.wunderlib.ui.layout.components.render.RenderHelper;
import de.ambertation.wunderlib.ui.layout.values.Rectangle;
import de.ambertation.wunderlib.ui.layout.values.Size;
import de.ambertation.wunderlib.ui.layout.values.Value;
import de.ambertation.wunderlib.ui.vanilla.LayoutScreen;
import org.betterx.wover.entrypoint.WoverWorldGenerator;
import org.betterx.wover.generator.api.client.biomesource.client.BiomeSourceConfigPanel;
import org.betterx.wover.generator.api.client.biomesource.client.BiomeSourceWithConfigScreen;
import org.betterx.wover.generator.impl.chunkgenerator.ConfiguredChunkGenerator;
import org.betterx.wover.generator.impl.chunkgenerator.WoverChunkGeneratorImpl;
import org.betterx.wover.preset.api.SortableWorldPreset;
import org.betterx.wover.preset.api.WorldPresetTags;
import org.betterx.wover.state.api.WorldState;
import org.betterx.wover.ui.impl.client.WelcomeScreen;
import org.betterx.wover.ui.impl.client.WoverLayoutScreen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationContext;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.presets.WorldPreset;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class WorldSetupScreen extends LayoutScreen implements BiomeSourceConfigPanel.DimensionUpdater {
    protected record DimensionValue(Holder<WorldPreset> preset, ResourceKey<WorldPreset> key, LevelStem dimension) {
    }


    public static class DimensionContent {
        public final ResourceKey<LevelStem> dimensionKey;
        public final ResourceKey<DimensionType> dimensionTypeKey;
        private BiomeSourceConfigPanel<?, ?> configPanel;
        private Component pageTitle;
        private Component buttonTitle;
        private DropDown<DimensionValue> generators;
        private VerticalStack stack;
        private int stackIndex = -1;

        private DimensionContent(ResourceKey<LevelStem> dimensionKey, ResourceKey<DimensionType> dimensionTypeKey) {
            this.dimensionKey = dimensionKey;
            this.dimensionTypeKey = dimensionTypeKey;
        }
    }

    private final List<ResourceKey<DimensionType>> sortedDimensions = List.of(
            BuiltinDimensionTypes.NETHER,
            BuiltinDimensionTypes.END
    );

    private final Map<ResourceKey<DimensionType>, DimensionContent> dimensions = Map.of(
            BuiltinDimensionTypes.NETHER, new DimensionContent(LevelStem.NETHER, BuiltinDimensionTypes.NETHER),
            BuiltinDimensionTypes.END, new DimensionContent(LevelStem.END, BuiltinDimensionTypes.END)
    );

    private final WorldCreationContext context;

    private final VerticalStack contentFallback;

    private final CreateWorldScreen createWorldScreen;

    public WorldSetupScreen(@Nullable CreateWorldScreen parent, WorldCreationContext context) {
        super(parent, Component.translatable("title.screen.wover.worldgen.main"), 10, 10, 10);
        this.context = context;
        this.createWorldScreen = parent;

        contentFallback = new VerticalStack(fit(), fit()).centerHorizontal();
        contentFallback.addSpacer(16);
        contentFallback.addText(
                fit(), fit(),
                Component.translatable("title.screen.wover.worldgen.no_generator_config")
        ).centerVertical().setColor(ColorHelper.DARK_GRAY);
    }

    private void generatorChanged(
            DimensionContent content,
            DimensionValue selected
    ) {
        if (content.stack != null) {
            final BiomeSource selectedBiomeSource = selected.dimension.generator().getBiomeSource();
            if (selectedBiomeSource instanceof BiomeSourceWithConfigScreen<?, ?> bs) {
                content.configPanel = bs.biomeSourceConfigPanel(this);
                content.stack.replaceOrAdd(content.stackIndex, content.configPanel.getPanel());
            } else {
                content.configPanel = null;
                content.stack.replaceOrAdd(content.stackIndex, contentFallback);
            }

            content.stack.recalculateLayout();
        }
    }


    @Nullable
    private static ResourceKey<WorldPreset> getConfiguredGeneratorKey(
            @Nullable Holder<WorldPreset> configuredPreset,
            @NotNull ResourceKey<LevelStem> forDimension
    ) {
        ResourceKey<WorldPreset> configuredKey = null;

        //see if the generator stores the preset key
        if (configuredPreset != null && configuredPreset.value() instanceof SortableWorldPreset wp) {
            final LevelStem dimension = wp.getDimension(forDimension);
            if (dimension != null && dimension.generator() instanceof ConfiguredChunkGenerator cfg) {
                configuredKey = cfg.wover_getConfiguredWorldPreset();
            }
        }

        //see if the preset is a reference with a valid key
        if (configuredKey == null && configuredPreset != null) {
            configuredKey = configuredPreset.unwrapKey().orElse(null);
        }

        //see if the preset was derived from a registered one
        if (configuredKey == null
                && configuredPreset != null
                && configuredPreset.value() instanceof SortableWorldPreset wp) {
            configuredKey = wp.parentKey();
        }

        return configuredKey;
    }

    private String languageKey(Holder<WorldPreset> key) {
        return languageKey(key.unwrapKey().orElseThrow().location());
    }

    private String languageKey(ResourceLocation key) {
        return "generator." + key.getNamespace() + "." + key.getPath();
    }

    protected void populateGenerators(
            @Nullable Holder<WorldPreset> configuredPreset,
            @NotNull ResourceKey<LevelStem> forDimension,
            @NotNull DropDown<DimensionValue> dimensions
    ) {
        ResourceKey<WorldPreset> finalConfiguredKey = getConfiguredGeneratorKey(configuredPreset, forDimension);
        DimensionValue[] selectedStem = {null};

        final Registry<WorldPreset> worldPresets = WorldState
                .allStageRegistryAccess()
                .registryOrThrow(Registries.WORLD_PRESET);

        final Optional<HolderSet.Named<WorldPreset>> normal = worldPresets.getTag(WorldPresetTags.NORMAL);
        if (normal.isPresent()) {
            Set<ChunkGenerator> generators = new HashSet<>();
            final Language language = Language.getInstance();

            normal
                    .get()
                    .stream()
                    .filter(preset -> preset.unwrapKey().isPresent())
                    .sorted((a, b) -> language.getOrDefault(languageKey(a))
                                              .compareTo(language.getOrDefault(languageKey(b))))
                    .forEach(preset -> {
                        final Optional<LevelStem> targetDimension = preset.value()
                                                                          .createWorldDimensions()
                                                                          .get(forDimension);
                        final ResourceLocation key = preset.unwrapKey().orElseThrow().location();
                        if (targetDimension.isPresent()) {
                            final ChunkGenerator generator = targetDimension.get().generator();
                            final DimensionValue dimensionValue = new DimensionValue(
                                    preset,
                                    preset.unwrapKey().orElseThrow(),
                                    targetDimension.get()
                            );
                            if (!generators.contains(generator)) {
                                generators.add(generator);
                                dimensions.addOption(Component.translatable(languageKey(key)), dimensionValue);
                            } else {
                                WoverWorldGenerator.C.log.debug("Skipping duplicate generator for preset {}", key);
                            }

                            if (finalConfiguredKey != null && key.equals(finalConfiguredKey.location())) {
                                selectedStem[0] = dimensionValue;
                            }
                        }
                    });
        }
        if (selectedStem[0] != null) {
            dimensions.select(selectedStem[0]);
        }
    }

    private LayoutComponent<?, ?> contentPage(
            Holder<WorldPreset> configuredPreset,
            Component title,
            DimensionContent contentData
    ) {
        VerticalStack content = new VerticalStack(fill(), fit()).centerHorizontal();
        content.setDebugName("Stack " + contentData.dimensionKey.location());
        content.addSpacer(16);
        content.addText(fit(), fit(), title).centerHorizontal();
        content.addHorizontalSeparator(8).alignTop();

        final HorizontalStack generatorRow = content.addRow(Value.fill(), Value.fit());
        generatorRow.addText(
                Value.fit(),
                Value.fit(),
                Component.translatable("title.screen.wover.worldgen.generator_template")
        );

        generatorRow.addSpacer(8);
        final DropDown<DimensionValue> generators = generatorRow.addDropDown(Value.fixed(200), Value.fit());
        generators.onChange((__unused, selected) -> generatorChanged(contentData, selected));

        contentData.stackIndex = content.size();
        contentData.generators = generators;
        contentData.stack = content;

        populateGenerators(configuredPreset, contentData.dimensionKey, generators);

        return content;
    }

    private void updateSettings() {
        dimensions.values().forEach(content -> {
            if (content.generators == null) return;
            final DimensionValue value = content.generators.selectedOption();
            if (value == null) return;

            ChunkGenerator generator = value.dimension.generator();

            if (generator instanceof ConfiguredChunkGenerator cfg) {
                cfg.wover_setConfiguredWorldPreset(value.key);
            }

            if (content.configPanel != null) {
                generator = content.configPanel.updateSettings(generator);
            }

            this.updateConfiguration(LevelStem.NETHER, BuiltinDimensionTypes.NETHER, generator);
        });

        final WorldCreationUiState acc = createWorldScreen.getUiState();
        final Holder<WorldPreset> configuredPreset = acc.getWorldType().preset();
        if (configuredPreset != null && configuredPreset.value() instanceof SortableWorldPreset worldPreset) {
            ResourceKey<WorldPreset> key = configuredPreset.unwrapKey().orElse(null);
            if (key == null) key = worldPreset.parentKey();

            acc.setWorldType(new WorldCreationUiState.WorldTypeEntry(Holder.direct(
                    worldPreset.withDimensions(
                            createWorldScreen
                                    .getUiState()
                                    .getSettings()
                                    .selectedDimensions()
                                    .dimensions(),
                            key
                    )
            )));
        }
    }


    @Override
    public void updateConfiguration(
            ResourceKey<LevelStem> dimensionKey,
            ResourceKey<DimensionType> dimensionTypeKey,
            ChunkGenerator chunkGenerator
    ) {
        createWorldScreen.getUiState().updateDimensions(
                (registryAccess, worldDimensions) -> new WorldDimensions(WoverChunkGeneratorImpl.replaceGenerator(
                        dimensionKey,
                        dimensionTypeKey,
                        registryAccess,
                        worldDimensions.dimensions(),
                        chunkGenerator
                ))
        );
    }

    @Override
    protected LayoutComponent<?, ?> createScreen(LayoutComponent<?, ?> content) {
        VerticalStack rows = new VerticalStack(fill(), fill()).setDebugName("title stack");

        if (topPadding > 0) rows.addSpacer(topPadding);
        rows.add(content);
        if (bottomPadding > 0) rows.addSpacer(bottomPadding);

        if (sidePadding <= 0) return rows;

        HorizontalStack cols = new HorizontalStack(fill(), fill()).setDebugName("padded side");
        cols.addSpacer(sidePadding);
        cols.add(rows);
        cols.addSpacer(sidePadding);
        return cols;
    }

    Button netherButton, endButton;
    VerticalScroll<?> scroller;
    HorizontalStack title;

    @Override
    protected LayoutComponent<?, ?> initContent() {
        final WorldCreationUiState acc = createWorldScreen.getUiState();
        final Holder<WorldPreset> configuredPreset = acc.getWorldType().preset();
        if (configuredPreset != null && configuredPreset.value() instanceof SortableWorldPreset wp) {
            dimensions.values().forEach(content -> {
                LevelStem stem = wp.getDimension(content.dimensionKey);
                if (stem != null
                        && stem.generator() instanceof ConfiguredChunkGenerator cfg
                        && configuredPreset.unwrapKey().isPresent()) {
                    if (cfg.wover_getConfiguredWorldPreset() == null)
                        cfg.wover_setConfiguredWorldPreset(configuredPreset.unwrapKey().orElseThrow());
                }
            });
        }

        final Tabs main = new Tabs(fill(), fill()).setPadding(8, 0, 0, 0);
        sortedDimensions
                .stream()
                .map(type -> dimensions.get(type))
                .filter(content -> content != null)
                .forEach(content -> {
                    LayoutComponent<?, ? extends LayoutComponent<?, ?>> page = contentPage(
                            configuredPreset,
                            Component.translatable("title.screen.wover.worldgen."
                                    + content.dimensionKey.location().getPath() + "_generator"),
                            content
                    );

                    if (!main.isEmpty()) {
                        main.addSpacer(8);
                    }

                    main.addPage(
                            Component.translatable("title.wover." + content.dimensionKey.location().getPath()),
                            this.scroller = VerticalScroll
                                    .create(page)
                                    .setDebugName(content.dimensionKey.location().getPath() + " scroll")
                    );
                });
        
        netherButton = main.getButton(0);
        endButton = main.getButton(1);

        title = new HorizontalStack(fit(), fit()).setDebugName("title bar").alignBottom();
        title.addImage(fixed(22), fixed(22), WoverLayoutScreen.WOVER_LOGO_WHITE_LOCATION, Size.of(256))
             .setDebugName("icon");
        title.addSpacer(4);
        VerticalStack logos = title.addColumn(fit(), fit());
        logos.addImage(fixed(178 / 3), fixed(40 / 3), WelcomeScreen.BETTERX_LOCATION, Size.of(178, 40));
        logos.add(super.createTitle());
        logos.addSpacer(2);

        main.addFiller();
        main.addComponent(title);


        VerticalStack rows = new VerticalStack(fill(), fill());
        rows.add(main);
        rows.addSpacer(4);
        rows.addButton(fit(), fit(), CommonComponents.GUI_DONE).onPress((bt) -> {
            updateSettings();
            onClose();
        }).alignRight();

        main.onPageChange((tabs, idx) -> targetT = 1 - idx);

        return rows;
    }

    @Override
    protected void renderBackground(GuiGraphics guiGraphics, int i, int j, float f) {
        guiGraphics.fill(0, 0, width, height, 0xBD343444);
    }

    record IconState(int left, int top, int size) {
        //easing curves from https://easings.net/de
        static double easeInOutQuint(double t) {
            return t < 0.5 ? 16 * t * t * t * t * t : 1 - Math.pow(-2 * t + 2, 5) / 2;
        }

        static double easeOutBounce(double x) {
            final double n1 = 7.5625;
            final double d1 = 2.75;

            if (x < 1 / d1) {
                return n1 * x * x;
            } else if (x < 2 / d1) {
                return n1 * (x -= 1.5 / d1) * x + 0.75;
            } else if (x < 2.5 / d1) {
                return n1 * (x -= 2.25 / d1) * x + 0.9375;
            } else {
                return n1 * (x -= 2.625 / d1) * x + 0.984375;
            }
        }

        static int lerp(double t, int x0, int x1) {
            return (int) ((1 - t) * x0 + t * x1);
        }
    }

    IconState netherOff, netherOn, endOff, endOn;
    double iconT = 0.5;
    double targetT = 1;

    @Override
    public void render(GuiGraphics guiGraphics, int i, int j, float f) {
        super.render(guiGraphics, i, j, f);
        final double SPEED = 0.05;
        if (targetT < iconT && iconT > 0) iconT = Math.max(0, iconT - f * SPEED);
        else if (targetT > iconT && iconT < 1) iconT = Math.min(1, iconT + f * SPEED);

        final double t;
        if (iconT > 0 && iconT < 1) {
            if (targetT > iconT) {
                t = IconState.easeOutBounce(iconT);
            } else {
                t = 1 - IconState.easeOutBounce(1 - iconT);
            }
        } else t = iconT;

        if (endButton != null) {
            if (endOff == null) {
                endOff = new IconState(
                        endButton.getScreenBounds().right() - 12,
                        endButton.getScreenBounds().top - 7,
                        16
                );
                endOn = new IconState(
                        (title.getScreenBounds().left - endButton.getScreenBounds().right()) / 2
                                + endButton.getScreenBounds().right()
                                - 14,
                        scroller.getScreenBounds().top - 16,
                        32
                );
            }
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(
                    IconState.lerp(t, endOn.left, endOff.left),
                    IconState.lerp(t, endOn.top, endOff.top),
                    0
            );
            int size = IconState.lerp(t, endOn.size, endOff.size);
            RenderHelper.renderImage(
                    guiGraphics, 0, 0,
                    size,
                    size,
                    WelcomeScreen.ICON_BETTEREND,
                    Size.of(32), new Rectangle(0, 0, 32, 32),
                    (float) 1
            );
            guiGraphics.pose().popPose();
        }

        if (netherButton != null) {
            if (netherOff == null) {
                netherOff = new IconState(
                        netherButton.getScreenBounds().right() - 12,
                        netherButton.getScreenBounds().top - 7,
                        16
                );
                netherOn = endOn;
            }
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(
                    IconState.lerp(t, netherOff.left, netherOn.left),
                    IconState.lerp(t, netherOff.top, netherOn.top),
                    0
            );
            int size = IconState.lerp(t, netherOff.size, netherOn.size);
            RenderHelper.renderImage(
                    guiGraphics, 0, 0,
                    size,
                    size,
                    WelcomeScreen.ICON_BETTERNETHER,
                    Size.of(32), new Rectangle(0, 0, 32, 32),
                    (float) 1
            );
            guiGraphics.pose().popPose();
        }
    }
}