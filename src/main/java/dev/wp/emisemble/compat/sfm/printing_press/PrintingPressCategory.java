package dev.wp.emisemble.compat.sfm.printing_press;

import ca.teamdman.sfm.common.recipe.PrintingPressRecipe;
import ca.teamdman.sfm.common.registry.registration.SFMBlocks;
import ca.teamdman.sfm.common.registry.registration.SFMRecipeTypes;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.porting.EmiRecipes;
import dev.wp.emisemble.porting.TranslatedCategory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeType;

/** {@code sfm:printing_press}: a form, an ink and paper in, copies of the form out. */
public final class PrintingPressCategory {
    static final ResourceLocation ID = EmiRecipes.categoryId(type());
    // The host overrides getWidth/getHeight to 50x54 and draws no background, so the page is bare slots.
    static final int WIDTH = 50;
    static final int HEIGHT = 54;

    public static final EmiRecipeCategory CATEGORY = new TranslatedCategory(ID,
            EmiStack.of(SFMBlocks.PRINTING_PRESS.get()), "gui.jei.category.sfm.printing_press");

    private PrintingPressCategory() {}

    public static int register(EmiRegistry registry) {
        registry.addCategory(CATEGORY);
        registry.addWorkstation(CATEGORY, EmiStack.of(SFMBlocks.PRINTING_PRESS.get()));
        int added = EmiRecipes.addAll(registry, type(), holder -> new PrintingPressEmiRecipe(holder.id(), holder.value()));
        Emisemble.LOGGER.info("sfm: {} printing press recipes registered", added);
        return added;
    }

    public static RecipeType<PrintingPressRecipe> type() {
        return SFMRecipeTypes.PRINTING_PRESS.get();
    }
}
