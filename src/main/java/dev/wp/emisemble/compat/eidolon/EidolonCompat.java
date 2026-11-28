package dev.wp.emisemble.compat.eidolon;

import dev.emi.emi.api.EmiRegistry;
import dev.wp.emisemble.compat.eidolon.crucible.CrucibleCategory;
import dev.wp.emisemble.compat.eidolon.dye.DyeCategory;
import dev.wp.emisemble.compat.eidolon.ritual.RitualCategory;
import dev.wp.emisemble.compat.eidolon.worktable.WorktableCategory;
import dev.wp.emisemble.compat.eidolon.worktable.WorktableTransfer;

/** Eidolon Repraised: the crucible, magic workbench and brazier categories, plus the dyed robe pages. */
public final class EidolonCompat {
    public static final String NAMESPACE = "eidolon_repraised";

    private EidolonCompat() {}

    public static void register(EmiRegistry registry) {
        CrucibleCategory.register(registry);
        WorktableCategory.register(registry);
        RitualCategory.register(registry);
        DyeCategory.register(registry);
        WorktableTransfer.register(registry);
    }
}
