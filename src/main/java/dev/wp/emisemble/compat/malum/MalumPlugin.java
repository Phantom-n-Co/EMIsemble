package dev.wp.emisemble.compat.malum;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.compat.malum.extras.ExtrasCompat;
import dev.wp.emisemble.compat.malum.runeworking.RuneworkingCompat;
import dev.wp.emisemble.compat.malum.spirit_drops.SpiritDropsCompat;
import dev.wp.emisemble.compat.malum.spirit_focusing.SpiritFocusingCompat;
import dev.wp.emisemble.compat.malum.spirit_infusion.SpiritInfusionCompat;
import dev.wp.emisemble.compat.malum.spirit_repair.SpiritRepairCompat;
import dev.wp.emisemble.compat.malum.transmutation.TransmutationCompat;
import dev.wp.emisemble.compat.malum.weeping_well.WeepingWellCompat;

@EmiEntrypoint
public final class MalumPlugin implements EmiPlugin {
    @Override
    public void register(EmiRegistry registry) {
        if (!Emisemble.isAvailable("malum")) {
            return;
        }
        MalumCompat.register(registry);
    }

    static final class MalumCompat {
        private MalumCompat() {}

        static void register(EmiRegistry registry) {
            int infusion = SpiritInfusionCompat.register(registry);
            int focusing = SpiritFocusingCompat.register(registry);
            int runeworking = RuneworkingCompat.register(registry);
            int repair = SpiritRepairCompat.register(registry);
            int weeping = WeepingWellCompat.register(registry);
            int transmutation = TransmutationCompat.register(registry);
            int drops = SpiritDropsCompat.register(registry);
            ExtrasCompat.register(registry);
            Emisemble.LOGGER.info("malum: {} infusion, {} focusing, {} runeworking, {} repair, {} weeping well, "
                    + "{} transmutation pages and {} spirit drop pages registered", infusion, focusing, runeworking, repair,
                    weeping, transmutation, drops);
        }
    }
}
