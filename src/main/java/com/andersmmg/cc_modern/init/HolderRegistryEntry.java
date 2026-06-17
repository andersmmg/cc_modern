package com.andersmmg.cc_modern.init;

import dan200.computercraft.shared.platform.RegistryEntry;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

public record HolderRegistryEntry<U>(Supplier<U> supplier, ResourceLocation id) implements RegistryEntry<U> {
    @Override
    public U get() {
        return supplier.get();
    }
}
