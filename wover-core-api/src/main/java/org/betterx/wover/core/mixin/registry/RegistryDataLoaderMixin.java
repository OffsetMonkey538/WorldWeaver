package org.betterx.wover.core.mixin.registry;

import org.betterx.wover.core.impl.registry.DatapackRegistryBuilderImpl;
import org.betterx.wover.entrypoint.LibWoverCore;

import com.mojang.serialization.Decoder;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.ResourceManager;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mixin(RegistryDataLoader.class)
public class RegistryDataLoaderMixin {
    @Accessor("WORLDGEN_REGISTRIES")
    @Mutable
    static void wt_set_WORLDGEN_REGISTRIES(List<RegistryDataLoader.RegistryData<?>> list) {
        //SHADOWED
    }

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void wover_init(CallbackInfo ci) {
        List<RegistryDataLoader.RegistryData<?>> enhanced = new ArrayList<>(RegistryDataLoader.WORLDGEN_REGISTRIES.size() + 1);
        enhanced.addAll(RegistryDataLoader.WORLDGEN_REGISTRIES);
        LibWoverCore.C.log.debug("Enhanced RegistryDataLoader.WORLDGEN_REGISTRIES");
        DatapackRegistryBuilderImpl.forEach((key, codec) -> {
            if (codec != null) {
                LibWoverCore.C.log.debug("    - Adding " + key.location());
                enhanced.add(new RegistryDataLoader.RegistryData(key, codec, false));
            }
        });

        wt_set_WORLDGEN_REGISTRIES(enhanced);
    }

    @Inject(method = "loadContentsFromManager", at = @At("TAIL"))
    private static <E> void wover_bootstrap(
            ResourceManager resourceManager,
            RegistryOps.RegistryInfoLookup registryInfoLookup,
            WritableRegistry<E> writableRegistry,
            Decoder<E> decoder,
            Map<ResourceKey<?>, Exception> map,
            CallbackInfo ci
    ) {
        DatapackRegistryBuilderImpl.bootstrap(registryInfoLookup, writableRegistry.key(), writableRegistry);
    }
}
