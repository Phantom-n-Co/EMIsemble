package dev.wp.emisemble.compat.arsadditions;

import dev.emi.emi.api.EmiRegistry;
import dev.wp.emisemble.compat.arsadditions.charm_charging.CharmChargingCategory;
import dev.wp.emisemble.compat.arsadditions.locate_structure.LocateStructureCategory;

/** Ars Additions' two categories, each a recipe type of its own. */
public final class ArsAdditionsCompat {
    private ArsAdditionsCompat() {}

    public static void register(EmiRegistry registry) {
        CharmChargingCategory.register(registry);
        LocateStructureCategory.register(registry);
    }
}
