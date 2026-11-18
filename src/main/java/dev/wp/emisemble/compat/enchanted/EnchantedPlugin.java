package dev.wp.emisemble.compat.enchanted;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.wp.emisemble.Emisemble;

/** EMI entry point for Enchanted; every host type stays behind this gate. */
@EmiEntrypoint
public final class EnchantedPlugin implements EmiPlugin {
    @Override
    public void register(EmiRegistry registry) {
        if (!Emisemble.isAvailable("enchanted")) {
            return;
        }
        EnchantedCompat.register(registry);
    }
}
