package dev.wp.emisemble.compat.cookingforblockheads;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.wp.emisemble.Emisemble;

/** EMI entry point for Cooking for Blockheads: hands the registry to {@link CookingForBlockheadsCompat}. */
@EmiEntrypoint
public final class CookingForBlockheadsPlugin implements EmiPlugin {
    @Override
    public void register(EmiRegistry registry) {
        if (!Emisemble.isAvailable("cookingforblockheads")) {
            return;
        }
        CookingForBlockheadsCompat.register(registry);
    }
}
