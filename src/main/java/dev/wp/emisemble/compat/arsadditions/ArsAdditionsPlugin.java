package dev.wp.emisemble.compat.arsadditions;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.wp.emisemble.Emisemble;

/** EMI entry point for Ars Additions; every host type stays behind this gate. */
@EmiEntrypoint
public final class ArsAdditionsPlugin implements EmiPlugin {
    @Override
    public void register(EmiRegistry registry) {
        if (!Emisemble.isAvailable("ars_additions")) {
            return;
        }
        ArsAdditionsCompat.register(registry);
    }
}
