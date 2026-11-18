package dev.wp.emisemble.compat.paganbless;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.wp.emisemble.Emisemble;

/** EMI entry point for Pagan's Blessing; every host type stays behind this gate. */
@EmiEntrypoint
public final class PaganblessPlugin implements EmiPlugin {
    @Override
    public void register(EmiRegistry registry) {
        if (!Emisemble.isAvailable("paganbless")) {
            return;
        }
        PaganblessCompat.register(registry);
    }
}
