package org.betterx.wover.testmod.entrypoint;

import org.betterx.wover.core.api.ModCore;
import org.betterx.wover.enchantment.api.EnchantmentKey;
import org.betterx.wover.enchantment.api.EnchantmentManager;
import org.betterx.wover.tabs.api.CreativeTabs;
import org.betterx.wover.testmod.item.TestItemRegistry;

import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.item.enchantment.effects.EnchantmentAttributeEffect;

import net.fabricmc.api.ModInitializer;

import java.util.List;

public class TestModWoverItem implements ModInitializer {
    // ModCore for the TestMod. TestMod's do not share the wover namespace,
    // but (like other Mods that include Wover) have a unique one
    public static final ModCore C = ModCore.create("wover-item-testmod");
    public static final EnchantmentKey TEST_ENCHANT = EnchantmentManager
            .createKey(C.mk("test_enchant"));

    public static final EnchantmentKey BREAKER_ENCHANT = EnchantmentManager
            .createKey(C.mk("breaker_enchant"));

    @Override
    public void onInitialize() {
        TestItemRegistry.ensureStaticallyLoaded();

        CreativeTabs.start(C)
                    .createItemOnlyTab(Items.WOODEN_AXE).buildAndAdd()
                    .processRegistries()
                    .registerAllTabs();

        EnchantmentManager.BOOTSTRAP_ENCHANTMENTS.subscribe(context -> {
            HolderGetter<Item> itemGetter = context.lookup(Registries.ITEM);
            BREAKER_ENCHANT.register(context, Enchantment
                    .enchantment(
                            Enchantment.definition(
                                    itemGetter.getOrThrow(ItemTags.MINING_ENCHANTABLE),
                                    10, 5,
                                    Enchantment.dynamicCost(20, 20),
                                    Enchantment.dynamicCost(120, 20),
                                    1,
                                    EquipmentSlotGroup.MAINHAND
                            )
                    )
                    .withEffect(
                            EnchantmentEffectComponents.ATTRIBUTES,
                            new EnchantmentAttributeEffect(
                                    ResourceLocation.withDefaultNamespace("enchantment.efficiency"),
                                    Attributes.MINING_EFFICIENCY,
                                    new LevelBasedValue.Lookup(List.of(6f, 12f, 18f), new LevelBasedValue.LevelsSquared(9.0F)),
                                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
                            )
                    )
            );
        });
    }
}