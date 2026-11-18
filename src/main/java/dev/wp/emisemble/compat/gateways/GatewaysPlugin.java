package dev.wp.emisemble.compat.gateways;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.wp.emisemble.Emisemble;

/** EMI entry point for Gateways; every host type stays behind this gate. */
@EmiEntrypoint
public final class GatewaysPlugin implements EmiPlugin {
    @Override
    public void register(EmiRegistry registry) {
        if (!Emisemble.isAvailable("gateways")) {
            return;
        }
        GatewaysCompat.register(registry);
    }
}
