package dev.wp.emisemble.compat.golemoverhaul;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.wp.emisemble.Emisemble;

/** EMI entry point for Golem Overhaul; every host type stays behind this gate. */
@EmiEntrypoint
public final class GolemOverhaulPlugin implements EmiPlugin {
    @Override
    public void register(EmiRegistry registry) {
        if (!Emisemble.isAvailable("golemoverhaul")) {
            return;
        }
        GolemOverhaulCompat.register(registry);
    }
}
