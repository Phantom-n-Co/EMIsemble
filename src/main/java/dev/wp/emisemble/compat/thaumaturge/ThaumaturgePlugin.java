package dev.wp.emisemble.compat.thaumaturge;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.wp.emisemble.Emisemble;

@EmiEntrypoint
public final class ThaumaturgePlugin implements EmiPlugin {
    @Override
    public void register(EmiRegistry registry) {
        if (!Emisemble.isAvailable("thaumaturge")) {
            return;
        }
        ThaumaturgeCompat.register(registry);
    }
}
