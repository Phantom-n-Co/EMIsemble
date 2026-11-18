package dev.wp.emisemble.compat.risus;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.wp.emisemble.Emisemble;

/** EMI entry point for Risus; every host type stays behind this gate. */
@EmiEntrypoint
public final class RisusPlugin implements EmiPlugin {
    @Override
    public void register(EmiRegistry registry) {
        if (!Emisemble.isAvailable("risus")) {
            return;
        }
        RisusCompat.register(registry);
    }
}
