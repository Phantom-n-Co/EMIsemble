package dev.wp.emisemble.compat.apotheosis;

import dev.emi.emi.api.EmiRegistry;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.compat.apotheosis.gem_cutting.GemCuttingCompat;
import dev.wp.emisemble.compat.apotheosis.salvaging.SalvagingCompat;
import dev.wp.emisemble.compat.apotheosis.stoneforming.StoneformingCategory;
import dev.wp.emisemble.compat.apotheosis.vanilla.VanillaExtensions;

/**
 * Ordering matters only where a page replaces one EMI already rendered: the vanilla-category work removes recipes by id
 * and re-adds its own, so it runs last and every category is registered before it.
 */
public final class ApotheosisCompat {
    private ApotheosisCompat() {}

    static void register(EmiRegistry registry) {
        int gemCutting = GemCuttingCompat.register(registry);
        int salvaging = SalvagingCompat.register(registry);
        int stoneforming = StoneformingCategory.register(registry);
        int vanilla = VanillaExtensions.register(registry);
        Emisemble.LOGGER.info("apotheosis: {} gem cutting, {} salvaging, {} stoneforming and {} vanilla pages registered",
                gemCutting, salvaging, stoneforming, vanilla);
    }
}
