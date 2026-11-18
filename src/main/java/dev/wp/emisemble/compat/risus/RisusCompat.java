package dev.wp.emisemble.compat.risus;

import dev.emi.emi.api.EmiRegistry;

/**
 * Risus' one recipe category. Its plugin also feeds anvil pages, which belong to EMI's own anvil category rather than a
 * category of this mod, so those stay unported.
 */
public final class RisusCompat {
    private RisusCompat() {}

    public static void register(EmiRegistry registry) {
        AlterationCategory.register(registry);
    }
}
