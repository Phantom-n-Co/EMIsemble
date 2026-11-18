package dev.wp.emisemble.compat.toxony;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.wp.emisemble.Emisemble;

/** EMI entry point for Toxony; every host type stays behind this gate. */
@EmiEntrypoint
public final class ToxonyPlugin implements EmiPlugin {
    @Override
    public void register(EmiRegistry registry) {
        if (!Emisemble.isAvailable("toxony")) {
            return;
        }
        ToxonyCompat.register(registry);
    }
}
