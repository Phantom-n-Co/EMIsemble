package dev.wp.emisemble.compat.nutritional;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.wp.emisemble.Emisemble;

/** EMI entry point for Nutritional; every host type stays behind this gate. */
@EmiEntrypoint
public final class NutritionalPlugin implements EmiPlugin {
    @Override
    public void register(EmiRegistry registry) {
        if (!Emisemble.isAvailable("nutritional")) {
            return;
        }
        NutritionalCompat.register(registry);
    }
}
