package org.betterx.wover.recipe.api;

import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;

import org.jetbrains.annotations.NotNull;

public interface BaseRecipeBuilder<I extends BaseRecipeBuilder<I>> {
    I category(@NotNull RecipeCategory category);
    void build(RecipeOutput ctx);
}
