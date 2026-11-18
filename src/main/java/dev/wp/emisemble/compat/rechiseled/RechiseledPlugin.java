package dev.wp.emisemble.compat.rechiseled;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.wp.emisemble.Emisemble;

/** EMI entry point for Rechiseled; every host type stays behind this gate. */
@EmiEntrypoint
public final class RechiseledPlugin implements EmiPlugin {
    @Override
    public void register(EmiRegistry registry) {
        if (!Emisemble.isAvailable("rechiseled")) {
            return;
        }
        RechiseledCompat.register(registry);
    }
}
