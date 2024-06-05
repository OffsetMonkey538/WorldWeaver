package org.betterx.wover.testmod.item;

import org.betterx.wover.common.item.api.ItemWithCustomStack;
import org.betterx.wover.enchantment.api.EnchantmentUtils;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.enchantment.Enchantments;

public class EnchantedAxe extends AxeItem implements ItemWithCustomStack {
    public EnchantedAxe() {
        super(Tiers.WOOD, (new Item.Properties()).attributes(AxeItem.createAttributes(Tiers.WOOD, 6.0F, -3.2F)));
    }

    @Override
    public void setupItemStack(ItemStack stack, HolderLookup.Provider provider) {
        EnchantmentUtils.enchantInWorld(stack, Enchantments.SHARPNESS, 5, provider);
    }
}
