package dev.wp.emisemble.compat.animus;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.wp.emisemble.Emisemble;

/** EMI entry point for Animus; every host type stays behind this gate. */
@EmiEntrypoint
public final class AnimusPlugin implements EmiPlugin {
    @Override
    public void register(EmiRegistry registry) {
        if (!Emisemble.isAvailable("animusnv")) {
            return;
        }
        AnimusCompat.register(registry);
    }
}
