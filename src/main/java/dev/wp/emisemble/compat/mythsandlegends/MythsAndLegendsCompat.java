package dev.wp.emisemble.compat.mythsandlegends;

import dev.emi.emi.api.EmiRegistry;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.compat.mythsandlegends.research.ResearchCategory;
import dev.wp.emisemble.compat.mythsandlegends.stone_sanctum.StoneSanctumCategory;
import dev.wp.emisemble.compat.mythsandlegends.stone_sanctum.StoneSanctumHandler;
import net.minecraft.resources.ResourceLocation;

/** The two categories the host's JEI plugin has: the research pages and the one hand-built Stone Sanctum ritual. */
public final class MythsAndLegendsCompat {
    private static final String NAMESPACE = "mythsandlegends";

    private MythsAndLegendsCompat() {}

    /**
     * Neither category is backed by a recipe type — research comes from a datapack reload listener and the sanctum page
     * is built in the host's plugin — so both uids are literals from its own {@code RecipeType.create} calls.
     */
    public static ResourceLocation categoryId(String path) {
        return ResourceLocation.fromNamespaceAndPath(NAMESPACE, path);
    }

    public static void register(EmiRegistry registry) {
        int added = 0;
        added += ResearchCategory.register(registry);
        added += StoneSanctumCategory.register(registry);
        StoneSanctumHandler.register(registry);
        Emisemble.LOGGER.info("mythsandlegends: {} recipes registered", added);
    }
}
