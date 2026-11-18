package dev.wp.emisemble.compat.cataclysm;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.wp.emisemble.Emisemble;

/** EMI entry point for L_Ender's Cataclysm; every host type stays behind this gate. */
@EmiEntrypoint
public final class CataclysmPlugin implements EmiPlugin {
    @Override
    public void register(EmiRegistry registry) {
        if (!Emisemble.isAvailable("cataclysm")) {
            return;
        }
        CataclysmCompat.register(registry);
    }
}
