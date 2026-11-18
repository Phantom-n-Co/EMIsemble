package dev.wp.emisemble.compat.animalpen;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.wp.emisemble.Emisemble;

/** EMI entry point for Animal Pens; every host type stays behind this gate. */
@EmiEntrypoint
public final class AnimalPenPlugin implements EmiPlugin {
    @Override
    public void register(EmiRegistry registry) {
        if (!Emisemble.isAvailable("animal_pen")) {
            return;
        }
        AnimalPenCompat.register(registry);
    }
}
