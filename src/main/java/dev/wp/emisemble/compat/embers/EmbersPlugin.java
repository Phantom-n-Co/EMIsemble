package dev.wp.emisemble.compat.embers;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.wp.emisemble.Emisemble;

/** Embers Re-Ignited; nothing here names a host type, so the gate holds with the mod absent. */
@EmiEntrypoint
public final class EmbersPlugin implements EmiPlugin {
    @Override
    public void register(EmiRegistry registry) {
        if (!Emisemble.isAvailable("embers")) {
            return;
        }
        EmbersCompat.register(registry);
    }
}
