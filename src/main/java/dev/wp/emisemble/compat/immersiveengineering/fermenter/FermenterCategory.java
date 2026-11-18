package dev.wp.emisemble.compat.immersiveengineering.fermenter;

import blusunrize.immersiveengineering.api.crafting.IERecipeTypes;
import blusunrize.immersiveengineering.common.register.IEMultiblockLogic;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.compat.immersiveengineering.IeIds;
import dev.wp.emisemble.porting.AlphaTextures;
import dev.wp.emisemble.porting.EmiRecipes;
import dev.wp.emisemble.porting.TranslatedCategory;
import net.minecraft.resources.ResourceLocation;

/**
 * {@code immersiveengineering:fermenter}. Same page shape as the squeezer, cut from the fermenter's own gui
 * texture; only the input socket moves up.
 */
public final class FermenterCategory {
    static final int WIDTH = 126;
    static final int HEIGHT = 59;

    static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("immersiveengineering", "textures/gui/fermenter.png");
    static final int SHEET = 256;

    private static final EmiStack ICON = EmiStack.of(IEMultiblockLogic.FERMENTER.iconStack());

    public static final EmiRecipeCategory CATEGORY = new TranslatedCategory(
            EmiRecipes.categoryId(IERecipeTypes.FERMENTER.get()), ICON, "block.immersiveengineering.fermenter");

    private FermenterCategory() {}

    public static int register(EmiRegistry registry) {
        try {
            registry.addCategory(CATEGORY);
            registry.addWorkstation(CATEGORY, ICON);
        } catch (Throwable t) {
            Emisemble.LOGGER.warn("immersiveengineering: could not register the fermenter category", t);
            return 0;
        }
        int added = EmiRecipes.addAll(registry, IERecipeTypes.FERMENTER.get(),
                holder -> new FermenterEmiRecipe(CATEGORY, IeIds.of(holder), holder.value()));
        Emisemble.LOGGER.info("immersiveengineering: {} fermenter recipes", added);
        return added;
    }

    static void page(WidgetHolder widgets) {
        AlphaTextures.add(widgets, TEXTURE, SHEET, 0, 0, WIDTH, HEIGHT, 6, 12);
    }
}
