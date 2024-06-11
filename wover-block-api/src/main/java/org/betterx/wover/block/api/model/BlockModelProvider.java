package org.betterx.wover.block.api.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public interface BlockModelProvider {
    @Environment(EnvType.CLIENT)
    void provideBlockModels(WoverBlockModelGenerators generators);
}
