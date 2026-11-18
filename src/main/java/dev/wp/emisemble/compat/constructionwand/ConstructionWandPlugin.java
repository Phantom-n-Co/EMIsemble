package dev.wp.emisemble.compat.constructionwand;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.wp.emisemble.Emisemble;

/** EMI entry point for Construction Wand; every host type stays behind this gate. */
@EmiEntrypoint
public final class ConstructionWandPlugin implements EmiPlugin {
    @Override
    public void register(EmiRegistry registry) {
        if (!Emisemble.isAvailable("constructionwand")) {
            return;
        }
        ConstructionWandCompat.register(registry);
    }
}
