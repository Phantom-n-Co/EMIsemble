package dev.wp.emisemble.compat.malum.weeping_well;

import com.sammy.malum.common.recipe.VoidFavorRecipe;
import com.sammy.malum.registry.common.item.MalumItems;
import com.sammy.malum.registry.common.recipe.MalumRecipeTypes;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.porting.EmiRecipes;
import dev.wp.emisemble.porting.TranslatedCategory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;

/** {@code malum:weeping_well}. */
public final class WeepingWellCompat {
    static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("malum", "textures/gui/weeping_well_jei.png");
    static final int SHEET = 256;
    // The host's 142x185 art is blitted from u=1 as 140x183, and the page is that less the 4px EMI insets each side.
    static final int WIDTH = 132;
    static final int HEIGHT = 175;

    private static final RecipeType<VoidFavorRecipe> TYPE = MalumRecipeTypes.VOID_FAVOR.get();

    // The category is named after the well while its recipe type is malum:void_favor, so only the host's JEI uid can
    // name it — the one place a category id cannot come from EmiRecipes.categoryId.
    public static final EmiRecipeCategory CATEGORY = new TranslatedCategory(
            ResourceLocation.fromNamespaceAndPath("malum", "weeping_well"),
            EmiStack.of(MalumItems.VOID_DEPOT.get()),
            "malum.jei.weeping_well");

    private WeepingWellCompat() {}

    public static int register(EmiRegistry registry) {
        registry.addCategory(CATEGORY);
        registry.addWorkstation(CATEGORY, EmiStack.of(MalumItems.VOID_DEPOT.get()));

        int added = 0;
        int skipped = 0;
        for (RecipeHolder<VoidFavorRecipe> holder : registry.getRecipeManager().getAllRecipesFor(TYPE)) {
            try {
                // The host hides a favour without a result from JEI, where its output socket would be empty.
                if (holder.value().result.isEmpty()) {
                    skipped++;
                    continue;
                }
                if (EmiRecipes.add(registry, new WeepingWellEmiRecipe(holder))) {
                    added++;
                }
            } catch (Throwable t) {
                Emisemble.LOGGER.warn("Skipping malum void favor recipe {}", holder.id(), t);
            }
        }
        Emisemble.LOGGER.info("malum: {} weeping well recipes registered, {} without a result skipped",
                added, skipped);
        return added;
    }
}
