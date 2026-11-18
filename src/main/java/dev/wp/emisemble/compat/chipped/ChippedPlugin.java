package dev.wp.emisemble.compat.chipped;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.wp.emisemble.Emisemble;

/** EMI entry point for Chipped; every host type stays behind this gate. */
@EmiEntrypoint
public final class ChippedPlugin implements EmiPlugin {
    @Override
    public void register(EmiRegistry registry) {
        if (!Emisemble.isAvailable("chipped")) {
            return;
        }
        ChippedCompat.register(registry);
    }
}
