package org.betterx.wover.tag.datagen;

import org.betterx.wover.core.api.ModCore;
import org.betterx.wover.datagen.api.WoverTagProvider;
import org.betterx.wover.tag.api.event.context.ItemTagBootstrapContext;
import org.betterx.wover.tag.api.predefined.CommonItemTags;

import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

import java.util.List;

public class ItemTagProvider extends WoverTagProvider.ForItems {
    public ItemTagProvider(ModCore modCore) {
        super(modCore, List.of(modCore.namespace, modCore.modId, "c", "minecraft"));
    }

    public void prepareTags(ItemTagBootstrapContext ctx) {
        ctx.add(CommonItemTags.SOUL_GROUND, Blocks.SOUL_SAND.asItem(), Blocks.SOUL_SOIL.asItem());

        ctx.add(CommonItemTags.CHEST, Items.CHEST);
        ctx.add(CommonItemTags.IRON_INGOTS, Items.IRON_INGOT);
        ctx.add(CommonItemTags.FURNACES, Blocks.FURNACE.asItem());
        ctx.add(CommonItemTags.WATER_BOTTLES, Items.WATER_BUCKET);

        ctx.add(ItemTags.MINING_ENCHANTABLE, CommonItemTags.SHEARS);

        ctx.add(
                CommonItemTags.MUSIC_DISCS,
                Items.MUSIC_DISC_13,
                Items.MUSIC_DISC_CAT,
                Items.MUSIC_DISC_BLOCKS,
                Items.MUSIC_DISC_CHIRP,
                Items.MUSIC_DISC_CREATOR,
                Items.MUSIC_DISC_CREATOR_MUSIC_BOX,
                Items.MUSIC_DISC_FAR,
                Items.MUSIC_DISC_MALL,
                Items.MUSIC_DISC_MELLOHI,
                Items.MUSIC_DISC_STAL,
                Items.MUSIC_DISC_STRAD,
                Items.MUSIC_DISC_WARD,
                Items.MUSIC_DISC_11,
                Items.MUSIC_DISC_WAIT,
                Items.MUSIC_DISC_OTHERSIDE,
                Items.MUSIC_DISC_RELIC,
                Items.MUSIC_DISC_5,
                Items.MUSIC_DISC_PIGSTEP,
                Items.MUSIC_DISC_PRECIPICE
        );
    }
}
