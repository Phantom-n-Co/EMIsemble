package dev.wp.emisemble.compat.apothic_enchanting;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.wp.emisemble.Emisemble;

@EmiEntrypoint
public final class ApothicEnchantingPlugin implements EmiPlugin {
    @Override
    public void register(EmiRegistry registry) {
        if (!Emisemble.isAvailable("apothic_enchanting")) {
            return;
        }
        ApothicEnchantingCompat.register(registry);
    }
}
