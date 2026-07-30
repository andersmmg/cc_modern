package com.andersmmg.cc_modern.mixin;

import dan200.computercraft.core.terminal.Palette;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Palette.class)
public interface PaletteAccess {
    @Accessor("byteColours")
    int[] cc_modern$getByteColours();
}
