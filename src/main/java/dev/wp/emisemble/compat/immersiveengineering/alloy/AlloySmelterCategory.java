package dev.wp.emisemble.compat.immersiveengineering.alloy;

import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.api.crafting.AlloyRecipe;
import blusunrize.immersiveengineering.api.crafting.IERecipeTypes;
import blusunrize.immersiveengineering.common.register.IEMultiblockLogic;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.compat.immersiveengineering.IeIds;
import dev.wp.emisemble.compat.immersiveengineering.IeRecipes;
import dev.wp.emisemble.porting.EmiRecipes;
import dev.wp.emisemble.porting.TranslatedCategory;
import net.minecraft.resources.ResourceLocation;

/** {@code immersiveengineering:alloy}, the alloy smelter. */
public final class AlloySmelterCategory {
    static final int WIDTH = 106;
    static final int HEIGHT = 56;
    static final int PAGE_U = 36;
    static final int PAGE_V = 15;
    static final ResourceLocation TEXTURE = IEApi.ieLoc("textures/gui/alloy_smelter.png");

    private static final EmiStack ICON = EmiStack.of(IEMultiblockLogic.ALLOY_SMELTER.iconStack());

    public static final EmiRecipeCategory CATEGORY = new TranslatedCategory(
            EmiRecipes.categoryId(IERecipeTypes.ALLOY.get()), ICON, "block.immersiveengineering.alloy_smelter");

    private AlloySmelterCategory() {}

    public static int register(EmiRegistry registry) {
        try {
            registry.addCategory(CATEGORY);
            registry.addWorkstation(CATEGORY, ICON);
        } catch (Throwable t) {
            Emisemble.LOGGER.warn("immersiveengineering: could not register the alloy smelter category", t);
            return 0;
        }
        int added = IeRecipes.addAll(registry, AlloyRecipe.RECIPES,
                holder -> new AlloySmelterEmiRecipe(CATEGORY, IeIds.of(holder), holder.value()));
        Emisemble.LOGGER.info("immersiveengineering: {} alloy smelter recipes", added);
        return added;
    }
}
