package dev.wp.emisemble.compat.vanillabackport;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.wp.emisemble.Emisemble;

@EmiEntrypoint
public final class VanillaBackportPlugin implements EmiPlugin {
    @Override
    public void register(EmiRegistry registry) {
        if (!Emisemble.isAvailable("vanillabackport")) {
            return;
        }
        VanillaBackportCompat.register(registry);
    }
}
