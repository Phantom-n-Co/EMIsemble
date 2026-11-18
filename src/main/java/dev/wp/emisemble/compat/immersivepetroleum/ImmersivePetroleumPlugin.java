package dev.wp.emisemble.compat.immersivepetroleum;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.wp.emisemble.Emisemble;

/** EMI entry point for Immersive Petroleum; every host type stays behind this gate. */
@EmiEntrypoint
public final class ImmersivePetroleumPlugin implements EmiPlugin {
    @Override
    public void register(EmiRegistry registry) {
        if (!Emisemble.isAvailable("immersivepetroleum")) {
            return;
        }
        ImmersivePetroleumCompat.register(registry);
    }
}
