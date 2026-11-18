package dev.wp.emisemble.compat.immersivepetroleum.coker;

import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.compat.immersivepetroleum.ImmersivePetroleumCompat;
import dev.wp.emisemble.porting.EmiRecipes;
import dev.wp.emisemble.porting.TranslatedCategory;
import flaxbeard.immersivepetroleum.api.crafting.CokerUnitRecipe;
import flaxbeard.immersivepetroleum.api.crafting.IPRecipeTypes;
import flaxbeard.immersivepetroleum.common.IPContent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeType;

/** {@code immersivepetroleum:cokerunit}. */
public final class CokerUnitCategory {
    static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("immersivepetroleum", "textures/gui/jei/coker.png");
    static final int WIDTH = 150;
    static final int HEIGHT = 77;

    private static final EmiStack ICON = EmiStack.of(IPContent.Multiblock.COKERUNIT.iconStack());

    public static final EmiRecipeCategory CATEGORY = new TranslatedCategory(
            ImmersivePetroleumCompat.categoryId("cokerunit"), ICON, "block.immersivepetroleum.coker_unit");

    private CokerUnitCategory() {}

    public static int register(EmiRegistry registry) {
        try {
            registry.addCategory(CATEGORY);
            registry.addWorkstation(CATEGORY, ICON);
        } catch (Throwable t) {
            Emisemble.LOGGER.warn("immersivepetroleum: could not register the coker unit category", t);
            return 0;
        }
        RecipeType<CokerUnitRecipe> type = IPRecipeTypes.COKER.get();
        int added = EmiRecipes.addAll(registry, type, holder -> new CokerUnitEmiRecipe(CATEGORY, holder.id(), holder.value()));
        Emisemble.LOGGER.info("immersivepetroleum: {} coker unit recipes", added);
        return added;
    }
}
