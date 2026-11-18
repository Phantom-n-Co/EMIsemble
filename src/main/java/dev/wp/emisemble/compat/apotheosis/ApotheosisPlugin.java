package dev.wp.emisemble.compat.apotheosis;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.compat.apotheosis.gem_cutting.GemCuttingCompat;
import dev.wp.emisemble.compat.apotheosis.salvaging.SalvagingCompat;
import dev.wp.emisemble.compat.apotheosis.stoneforming.StoneformingCategory;
import dev.wp.emisemble.compat.apotheosis.vanilla.VanillaExtensions;

/** Every Apotheosis port; nothing here names a host type, so the gate holds with the mod absent. */
@EmiEntrypoint
public final class ApotheosisPlugin implements EmiPlugin {
    @Override
    public void register(EmiRegistry registry) {
        if (!Emisemble.isAvailable("apotheosis")) {
            return;
        }
        ApotheosisCompat.register(registry);
    }
}
