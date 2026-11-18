package dev.wp.emisemble.compat.sfm;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.wp.emisemble.Emisemble;

/** EMI entry point for Super Factory Manager; every host type stays behind this gate. */
@EmiEntrypoint
public final class SfmPlugin implements EmiPlugin {
    @Override
    public void register(EmiRegistry registry) {
        if (!Emisemble.isAvailable("sfm")) {
            return;
        }
        SfmCompat.register(registry);
    }
}
