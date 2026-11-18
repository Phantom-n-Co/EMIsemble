package dev.wp.emisemble.compat.functionalstorage;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.wp.emisemble.Emisemble;

/** EMI entry point for Functional Storage; every host type stays behind this gate. */
@EmiEntrypoint
public final class FunctionalStoragePlugin implements EmiPlugin {
    @Override
    public void register(EmiRegistry registry) {
        if (!Emisemble.isAvailable("functionalstorage")) {
            return;
        }
        FunctionalStorageCompat.register(registry);
    }
}
