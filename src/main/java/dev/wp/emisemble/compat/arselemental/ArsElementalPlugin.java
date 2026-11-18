package dev.wp.emisemble.compat.arselemental;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.wp.emisemble.Emisemble;

/** EMI entry point for Ars Elemental; every host type stays behind this gate. */
@EmiEntrypoint
public final class ArsElementalPlugin implements EmiPlugin {
    @Override
    public void register(EmiRegistry registry) {
        if (!Emisemble.isAvailable("ars_elemental")) {
            return;
        }
        ArsElementalCompat.register(registry);
    }
}
