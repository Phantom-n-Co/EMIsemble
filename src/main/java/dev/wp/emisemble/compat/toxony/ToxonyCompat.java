package dev.wp.emisemble.compat.toxony;

import dev.emi.emi.api.EmiRegistry;
import dev.wp.emisemble.compat.toxony.alchemical_forge.AlchemicalForgeCategory;
import dev.wp.emisemble.compat.toxony.alembic.AlembicCategory;
import dev.wp.emisemble.compat.toxony.crucible.CrucibleCategory;
import dev.wp.emisemble.compat.toxony.mortar_pestle.MortarPestleCategory;

/** Toxony's four categories, each a recipe type of its own. */
public final class ToxonyCompat {
    private ToxonyCompat() {}

    public static void register(EmiRegistry registry) {
        MortarPestleCategory.register(registry);
        CrucibleCategory.register(registry);
        AlembicCategory.register(registry);
        AlchemicalForgeCategory.register(registry);
    }
}
