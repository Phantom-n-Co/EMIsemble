package dev.wp.emisemble.compat.functionalstorage;

import dev.emi.emi.api.EmiRegistry;
import dev.wp.emisemble.compat.functionalstorage.dissolution.DissolutionCategory;

/** The one category Functional Storage's JEI plugin registers. */
public final class FunctionalStorageCompat {
    private FunctionalStorageCompat() {}

    public static void register(EmiRegistry registry) {
        DissolutionCategory.register(registry);
    }
}
