package dev.wp.emisemble.compat.hostilenetworks;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.wp.emisemble.Emisemble;

/** EMI entry point for Hostile Neural Networks; every host type stays behind this gate. */
@EmiEntrypoint
public final class HostileNeuralNetworksPlugin implements EmiPlugin {
    @Override
    public void register(EmiRegistry registry) {
        if (!Emisemble.isAvailable("hostilenetworks")) {
            return;
        }
        HostileNeuralNetworksCompat.register(registry);
    }
}
