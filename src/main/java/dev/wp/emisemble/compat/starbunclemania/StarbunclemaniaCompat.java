package dev.wp.emisemble.compat.starbunclemania;

import dev.emi.emi.api.EmiRegistry;
import dev.wp.emisemble.compat.starbunclemania.fluid_sourcelink.FluidSourcelinkCategory;

/** Starbunclemania's single chapter page. */
public final class StarbunclemaniaCompat {
    private StarbunclemaniaCompat() {}

    public static void register(EmiRegistry registry) {
        FluidSourcelinkCategory.register(registry);
    }
}
