package dev.wp.emisemble.compat.immersivepetroleum;

import dev.emi.emi.api.EmiRegistry;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.compat.immersivepetroleum.coker.CokerUnitCategory;
import dev.wp.emisemble.compat.immersivepetroleum.distillation.DistillationCategory;
import dev.wp.emisemble.compat.immersivepetroleum.hydrotreater.HydrotreaterCategory;
import net.minecraft.resources.ResourceLocation;

/** The three categories IP registers with JEI, one per machine. */
public final class ImmersivePetroleumCompat {
    private static final String NAMESPACE = "immersivepetroleum";

    private ImmersivePetroleumCompat() {}

    /**
     * IP names its JEI categories differently from its recipe types ({@code cokerunit} over {@code coker}), and JEMI
     * matches the category uid, so these literals — not the recipe type keys — are the ids that suppress its shims.
     */
    public static ResourceLocation categoryId(String path) {
        return ResourceLocation.fromNamespaceAndPath(NAMESPACE, path);
    }

    public static void register(EmiRegistry registry) {
        int added = 0;
        added += CokerUnitCategory.register(registry);
        added += DistillationCategory.register(registry);
        added += HydrotreaterCategory.register(registry);
        Emisemble.LOGGER.info("immersivepetroleum: {} recipes registered", added);
    }
}
