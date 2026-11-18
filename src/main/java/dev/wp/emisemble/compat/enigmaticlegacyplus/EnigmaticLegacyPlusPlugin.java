package dev.wp.emisemble.compat.enigmaticlegacyplus;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.wp.emisemble.Emisemble;

@EmiEntrypoint
/** EMI entry point for Enigmatic Legacy Plus: hands the registry to {@link EnigmaticLegacyPlusCompat}. */
public final class EnigmaticLegacyPlusPlugin implements EmiPlugin {
    @Override
    public void register(EmiRegistry registry) {
        if (!Emisemble.isAvailable("enigmaticlegacyplus")) {
            return;
        }
        EnigmaticLegacyPlusCompat.register(registry);
    }
}
