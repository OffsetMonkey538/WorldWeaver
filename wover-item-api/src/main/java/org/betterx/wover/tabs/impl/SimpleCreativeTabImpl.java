package org.betterx.wover.tabs.impl;

import org.betterx.wover.tabs.api.interfaces.CreativeTabPredicate;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;

import java.util.LinkedList;
import java.util.List;

public class SimpleCreativeTabImpl {
    public final ResourceLocation id;
    public final ItemLike icon;
    public final Component title;
    public final CreativeTabPredicate predicate;

    public final ResourceKey<CreativeModeTab> key;

    SimpleCreativeTabImpl(ResourceLocation id, ItemLike icon, Component title, CreativeTabPredicate predicate) {
        this.id = id;
        this.icon = icon;
        this.title = title;
        this.predicate = predicate;
        this.key = ResourceKey.create(
                Registries.CREATIVE_MODE_TAB,
                id
        );
        this.items = new LinkedList<>();
    }

    protected final List<Item> items;

    void addItem(Item item) {
        items.add(item);
    }
}
