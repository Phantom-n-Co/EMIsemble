package dev.wp.emisemble.compat.modopedia;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.wp.emisemble.Emisemble;

/** EMI entry point for Modopedia; every host type stays behind this gate. */
@EmiEntrypoint
public final class ModopediaPlugin implements EmiPlugin {
    @Override
    public void register(EmiRegistry registry) {
        if (!Emisemble.isAvailable("modopedia")) {
            return;
        }
        ModopediaCompat.register(registry);
    }
}
