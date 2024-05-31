package org.betterx.wover.testmod.structure.datagen;

import org.betterx.wover.core.api.ModCore;
import org.betterx.wover.datagen.api.provider.multi.WoverStructureProvider;
import org.betterx.wover.structure.api.sets.StructureSetManager;
import org.betterx.wover.structure.api.structures.StructurePlacement;
import org.betterx.wover.tag.api.event.context.TagBootstrapContext;
import org.betterx.wover.testmod.entrypoint.TestModWoverStructure;

import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;

public class StructureProvider extends WoverStructureProvider {
    private final ModCore C = TestModWoverStructure.C;

    /**
     * Creates a new instance of {@link WoverStructureProvider}.
     *
     * @param modCore The {@link ModCore} of the Mod.
     */
    public StructureProvider(ModCore modCore) {
        super(modCore);
    }

    @Override
    protected void bootstrapSturctures(BootstrapContext<Structure> context) {
        TestModWoverStructure.TEST_STRUCTURE
                .bootstrap(context)
                .adjustment(TerrainAdjustment.BEARD_BOX)
                .register();

        TestModWoverStructure.JIGSAW_STRUCTURE
                .bootstrap(context)
                .startPool(TestModWoverStructure.TEST_STRUCTURE_POOL_START)
                .maxDepth(5)
                .projectStartToHeightmap(Heightmap.Types.MOTION_BLOCKING)
                .adjustment(TerrainAdjustment.BEARD_BOX)
                .register();

        TestModWoverStructure.RND_STRUCTURE
                .bootstrap(context)
                .addElement(TestModWoverStructure.C.id("simple"), 1, 2.0)
                .placement(StructurePlacement.NETHER_SURFACE_FLAT_2)
                .register();
    }

    @Override
    protected void bootstrapSets(BootstrapContext<StructureSet> context) {
        TestModWoverStructure.TEST_STRUCTURE_SET
                .bootstrap(context)
                .addStructure(TestModWoverStructure.TEST_STRUCTURE)
                .addStructure(TestModWoverStructure.JIGSAW_STRUCTURE)
                .randomPlacement()
                //.spreadType(RandomSpreadType.TRIANGULAR)
                .finishPlacement()
                .register();

        StructureSetManager
                .bootstrap(TestModWoverStructure.RND_STRUCTURE, context)
                .randomPlacement(16, 4)
                .register();
    }

    @Override
    protected void bootstrapPools(BootstrapContext<StructureTemplatePool> context) {
        TestModWoverStructure.TEST_STRUCTURE_POOL_START
                .bootstrap(context)
                .terminator(TestModWoverStructure.TEST_STRUCTURE_POOL_TERMINAL)
                .startSingle(C.id("street"))
                .endElement()
                .projection(StructureTemplatePool.Projection.TERRAIN_MATCHING)
                .register();

        TestModWoverStructure.TEST_STRUCTURE_POOL_HOUSE
                .bootstrap(context)
                .startSingle(C.id("house"))
                .endElement()
                .register();

        TestModWoverStructure.TEST_STRUCTURE_POOL_TERMINAL
                .bootstrap(context)
                .startSingle(C.id("terminator"))
                .processor(TestModWoverStructure.TEST_STRUCTURE_PROCESSOR)
                .endElement()
                .register();
    }

    @Override
    protected void bootstrapProcessors(BootstrapContext<StructureProcessorList> context) {
        TestModWoverStructure.TEST_STRUCTURE_PROCESSOR
                .bootstrap(context)

                .startRule()
                .startProcessor()
                .inputPredicateRandom(Blocks.RED_GLAZED_TERRACOTTA, 0.33f)
                .outputState(Blocks.RED_CONCRETE)
                .endProcessor()
                .endRule()

                .register();
    }

    @Override
    protected void prepareBiomeTags(TagBootstrapContext<Biome> context) {
        context.add(
                TestModWoverStructure.TEST_STRUCTURE.biomeTag(),
                Biomes.SAVANNA, Biomes.END_HIGHLANDS, Biomes.END_MIDLANDS, Biomes.NETHER_WASTES
        );

        context.add(
                TestModWoverStructure.RND_STRUCTURE.biomeTag(),
                Biomes.NETHER_WASTES, Biomes.CRIMSON_FOREST, Biomes.WARPED_FOREST, Biomes.SOUL_SAND_VALLEY
        );
    }
}
