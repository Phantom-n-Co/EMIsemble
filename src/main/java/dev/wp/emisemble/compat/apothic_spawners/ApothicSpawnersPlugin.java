package dev.wp.emisemble.compat.apothic_spawners;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.wp.emisemble.Emisemble;

@EmiEntrypoint
public final class ApothicSpawnersPlugin implements EmiPlugin {
    @Override
    public void register(EmiRegistry registry) {
        if (!Emisemble.isAvailable("apothic_spawners")) {
            return;
        }
        ApothicSpawnersCompat.register(registry);
    }
}
