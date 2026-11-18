package dev.wp.emisemble.compat.immersivepetroleum.hydrotreater;

import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.compat.immersivepetroleum.ImmersivePetroleumCompat;
import dev.wp.emisemble.porting.EmiRecipes;
import dev.wp.emisemble.porting.TranslatedCategory;
import flaxbeard.immersivepetroleum.api.crafting.HighPressureRefineryRecipe;
import flaxbeard.immersivepetroleum.api.crafting.IPRecipeTypes;
import flaxbeard.immersivepetroleum.common.IPContent;
import net.minecraft.resources.ResourceLocation;

/** {@code immersivepetroleum:hydrotreater}, the high pressure refinery. */
public final class HydrotreaterCategory {
    static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("immersivepetroleum", "textures/gui/jei/hydrotreater.png");
    static final int WIDTH = 113;
    static final int HEIGHT = 75;

    private static final EmiStack ICON = EmiStack.of(IPContent.Multiblock.HYDROTREATER.iconStack());

    public static final EmiRecipeCategory CATEGORY = new TranslatedCategory(
            ImmersivePetroleumCompat.categoryId("hydrotreater"), ICON, "block.immersivepetroleum.hydrotreater");

    private HydrotreaterCategory() {}

    public static int register(EmiRegistry registry) {
        try {
            registry.addCategory(CATEGORY);
            registry.addWorkstation(CATEGORY, ICON);
        } catch (Throwable t) {
            Emisemble.LOGGER.warn("immersivepetroleum: could not register the hydrotreater category", t);
            return 0;
        }
        int added = EmiRecipes.addAll(registry, IPRecipeTypes.HYDROTREATER.get(),
                holder -> new HydrotreaterEmiRecipe(CATEGORY, holder.id(), holder.value()));
        Emisemble.LOGGER.info("immersivepetroleum: {} hydrotreater recipes", added);
        return added;
    }
}
