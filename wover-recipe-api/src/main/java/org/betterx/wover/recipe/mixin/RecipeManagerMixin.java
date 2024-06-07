package org.betterx.wover.recipe.mixin;

import org.betterx.wover.entrypoint.LibWoverRecipe;
import org.betterx.wover.recipe.impl.RecipeRuntimeProviderImpl;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Multimap;
import com.google.gson.JsonElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(RecipeManager.class)
public class RecipeManagerMixin {
    @Shadow
    private Multimap<RecipeType<?>, RecipeHolder<?>> byType;

    @Shadow
    private Map<ResourceLocation, RecipeHolder<?>> byName;

    @Inject(method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V", at = @At("TAIL"))
    void wover_apply(
            Map<ResourceLocation, JsonElement> map,
            ResourceManager resourceManager,
            ProfilerFiller profilerFiller,
            CallbackInfo ci
    ) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        final int count = this.byType.size();
        RecipeRuntimeProviderImpl.LoadedRecipes loaded = RecipeRuntimeProviderImpl.loadedRecipes(new RecipeRuntimeProviderImpl.LoadedRecipes(this.byType, this.byName));
        this.byType = loaded.byType();
        this.byName = loaded.byName();


        LibWoverRecipe.C.LOG.info("Added {} recipes in {}ms", this.byType.size() - count, stopwatch
                .stop()
                .elapsed()
                .toMillis());
    }
}
