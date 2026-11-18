package dev.wp.emisemble.compat.dungeonsdelight;

import dev.emi.emi.api.EmiRegistry;
import dev.wp.emisemble.compat.dungeonsdelight.info.InfoPages;
import dev.wp.emisemble.compat.dungeonsdelight.monster_cooking.MonsterCookingCategory;
import dev.wp.emisemble.compat.dungeonsdelight.sculking.SculkingCategory;
import dev.wp.emisemble.compat.dungeonsdelight.transfer.TransferHandlers;

/** Registers Dungeons Delight's two categories, seven item info pages and one transfer handler. */
public final class DungeonsDelightCompat {
    private DungeonsDelightCompat() {}

    public static int register(EmiRegistry registry) {
        int pages = MonsterCookingCategory.register(registry)
                + SculkingCategory.register(registry);

        InfoPages.register(registry);
        TransferHandlers.register(registry);
        return pages;
    }
}
