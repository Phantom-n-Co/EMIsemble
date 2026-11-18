package dev.wp.emisemble.compat.appliedthaumaturge;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.wp.emisemble.Emisemble;

@EmiEntrypoint
public final class AppliedThaumaturgePlugin implements EmiPlugin {
    @Override
    public void register(EmiRegistry registry) {
        if (!Emisemble.isAvailable("appliedthaumaturge")) {
            return;
        }
        AppliedThaumaturgeCompat.register(registry);
    }
}
