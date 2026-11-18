package dev.wp.emisemble.compat.hexerei;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.wp.emisemble.Emisemble;

@EmiEntrypoint
/** EMI entry point for Hexerei: hands the registry to {@link HexereiCompat}. */
public final class HexereiPlugin implements EmiPlugin {
    @Override
    public void register(EmiRegistry registry) {
        if (!Emisemble.isAvailable("hexerei")) {
            return;
        }
        HexereiCompat.register(registry);
    }
}
