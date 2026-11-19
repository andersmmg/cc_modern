package com.andersmmg.cc_modern.compat;

import net.neoforged.fml.ModList;

/**
 * CC: Terminals is an optional dependency. When it is not installed the wall terminal is simply
 * never registered.
 */
public final class CCTerminalsCompat {
    public static final String MOD_ID = "ccterminals";

    private static Boolean present;

    private CCTerminalsCompat() {
    }

    public static boolean isPresent() {
        if (present == null) {
            var modList = ModList.get();
            present = modList != null && modList.isLoaded(MOD_ID);
        }
        return present;
    }
}
