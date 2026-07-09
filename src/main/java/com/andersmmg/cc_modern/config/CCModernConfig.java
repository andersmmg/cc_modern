package com.andersmmg.cc_modern.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class CCModernConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue PREVENT_MERGE_WHEN_SNEAKING = BUILDER
            .define("preventMergeWhenSneaking", true);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private CCModernConfig() {
    }
}
