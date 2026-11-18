package dev.wp.emisemble.compat.starbunclemania;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.wp.emisemble.Emisemble;

/** EMI entry point for Starbunclemania; every host type stays behind this gate. */
@EmiEntrypoint
public final class StarbunclemaniaPlugin implements EmiPlugin {
    @Override
    public void register(EmiRegistry registry) {
        if (!Emisemble.isAvailable("starbunclemania")) {
            return;
        }
        StarbunclemaniaCompat.register(registry);
    }
}
