package dev.wp.emisemble.compat.golemoverhaul;

import dev.emi.emi.api.EmiRegistry;

/** Golem Overhaul's one category: the drawn construction pattern. */
public final class GolemOverhaulCompat {
    private GolemOverhaulCompat() {}

    public static void register(EmiRegistry registry) {
        GolemConstructionCategory.register(registry);
    }
}
