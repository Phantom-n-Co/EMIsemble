package dev.wp.emisemble.compat.mythsandlegends;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.wp.emisemble.Emisemble;

/** EMI entry point for Myths and Legends; every host type stays behind this gate. */
@EmiEntrypoint
public final class MythsAndLegendsPlugin implements EmiPlugin {
    @Override
    public void register(EmiRegistry registry) {
        if (!Emisemble.isAvailable("mythsandlegends")) {
            return;
        }
        MythsAndLegendsCompat.register(registry);
    }
}
