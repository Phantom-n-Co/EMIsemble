package dev.wp.emisemble.compat.enchanted;

import dev.emi.emi.api.EmiRegistry;
import dev.wp.emisemble.compat.enchanted.byproduct.ByproductCategory;
import dev.wp.emisemble.compat.enchanted.distilling.DistillingCategory;
import dev.wp.emisemble.compat.enchanted.info.InfoPages;
import dev.wp.emisemble.compat.enchanted.kettle.KettleCategory;
import dev.wp.emisemble.compat.enchanted.mutandis.MutandisCategory;
import dev.wp.emisemble.compat.enchanted.rite.RiteCategory;
import dev.wp.emisemble.compat.enchanted.spinning.SpinningCategory;
import dev.wp.emisemble.compat.enchanted.transfer.TransferHandlers;

/** Registers Enchanted's six categories, six item info pages and three transfer handlers. */
public final class EnchantedCompat {
    private EnchantedCompat() {}

    public static int register(EmiRegistry registry) {
        int pages = ByproductCategory.register(registry)
                + SpinningCategory.register(registry)
                + DistillingCategory.register(registry)
                + KettleCategory.register(registry)
                + RiteCategory.register(registry)
                + MutandisCategory.register(registry);

        InfoPages.register(registry);
        TransferHandlers.register(registry);
        return pages;
    }
}
