package dev.wp.emisemble.compat.sfm;

import dev.emi.emi.api.EmiRegistry;
import dev.wp.emisemble.compat.sfm.falling_anvil.FallingAnvilCategory;
import dev.wp.emisemble.compat.sfm.printing_press.PrintingPressCategory;

/** SFM's two viewer categories; the falling anvil pages are derived from the printing press recipes. */
public final class SfmCompat {
    private SfmCompat() {}

    public static void register(EmiRegistry registry) {
        PrintingPressCategory.register(registry);
        FallingAnvilCategory.register(registry);
    }
}
