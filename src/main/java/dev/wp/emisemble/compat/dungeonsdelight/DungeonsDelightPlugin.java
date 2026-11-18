package dev.wp.emisemble.compat.dungeonsdelight;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.wp.emisemble.Emisemble;

/** EMI entry point for Dungeons Delight; every host type stays behind this gate. */
@EmiEntrypoint
public final class DungeonsDelightPlugin implements EmiPlugin {
    @Override
    public void register(EmiRegistry registry) {
        if (!Emisemble.isAvailable("dungeonsdelight")) {
            return;
        }
        DungeonsDelightCompat.register(registry);
    }
}
