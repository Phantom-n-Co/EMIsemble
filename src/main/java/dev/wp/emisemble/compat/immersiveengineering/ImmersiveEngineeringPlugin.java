package dev.wp.emisemble.compat.immersiveengineering;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.wp.emisemble.Emisemble;

/** EMI entry point for Immersive Engineering: hands the registry to {@link ImmersiveEngineeringCompat}. */
@EmiEntrypoint
public final class ImmersiveEngineeringPlugin implements EmiPlugin {
    @Override
    public void register(EmiRegistry registry) {
        if (!Emisemble.isAvailable("immersiveengineering")) {
            return;
        }
        ImmersiveEngineeringCompat.register(registry);
    }
}
