package dev.wp.emisemble.compat.malum.runeworking;

import com.sammy.malum.common.recipe.RuneworkingRecipe;
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

/** {@code malum:runeworking}. */
public final class RuneworkingCompat {
    // The page art paints all three item sockets, so the page adds no frames of its own.
    static final ResourceLocation PAGE =
            ResourceLocation.fromNamespaceAndPath("malum", "textures/gui/runeworking_jei.png");
    static final int SHEET = 256;
    // The host's 142x185 art is blitted from u=1 as 140x183, and the page is that less the 4px EMI insets each side.
    static final int WIDTH = 132;
    static final int HEIGHT = 175;

    private static final RecipeType<RuneworkingRecipe> TYPE = MalumRecipeTypes.RUNEWORKING.get();

    public static final EmiRecipeCategory CATEGORY = new TranslatedCategory(
            EmiRecipes.categoryId(TYPE),
            EmiStack.of(MalumItems.RUNIC_WORKBENCH.get()),
            "malum.jei.runeworking");

    private RuneworkingCompat() {}

    public static int register(EmiRegistry registry) {
        registry.addCategory(CATEGORY);
        registry.addWorkstation(CATEGORY, EmiStack.of(MalumItems.RUNIC_WORKBENCH.get()));

        int added = 0;
        int skipped = 0;
        for (RecipeHolder<RuneworkingRecipe> holder : registry.getRecipeManager().getAllRecipesFor(TYPE)) {
            try {
                // The host hides a resultless recipe from JEI, where it would be an empty output socket.
                if (holder.value().output.isEmpty()) {
                    skipped++;
                    continue;
                }
                if (EmiRecipes.add(registry, new RuneworkingEmiRecipe(holder))) {
                    added++;
                }
            } catch (Throwable t) {
                Emisemble.LOGGER.warn("Skipping malum runeworking recipe {}", holder.id(), t);
            }
        }
        Emisemble.LOGGER.info("malum: {} runeworking recipes registered, {} without a result skipped", added, skipped);
        return added;
    }
}
