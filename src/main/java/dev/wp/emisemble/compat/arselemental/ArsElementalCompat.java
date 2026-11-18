package dev.wp.emisemble.compat.arselemental;

import dev.emi.emi.api.EmiRegistry;
import dev.wp.emisemble.compat.arselemental.netherite_upgrade.NetheriteUpgradeCategory;

/** Ars Elemental's single chapter page. */
public final class ArsElementalCompat {
    private ArsElementalCompat() {}

    public static void register(EmiRegistry registry) {
        NetheriteUpgradeCategory.register(registry);
    }
}
