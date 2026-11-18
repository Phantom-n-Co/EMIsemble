package dev.wp.emisemble.compat.cognition;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.wp.emisemble.Emisemble;

/** EMI entry point for Cognition; every host type stays behind this gate. */
@EmiEntrypoint
public final class CognitionPlugin implements EmiPlugin {
    @Override
    public void register(EmiRegistry registry) {
        if (!Emisemble.isAvailable("cognition")) {
            return;
        }
        CognitionCompat.register(registry);
    }
}
