package dev.wp.emisemble.compat.immersiveengineering.refinery;

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
 * {@code immersiveengineering:refinery}. The page is cut straight from the machine's gui, sockets and arrow
 * included, so the tanks are the only widgets the port adds.
 */
public final class RefineryCategory {
    static final int WIDTH = 125;
    static final int HEIGHT = 62;

    static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("immersiveengineering", "textures/gui/refinery.png");
    static final int SHEET = 256;

    private static final EmiStack ICON = EmiStack.of(IEMultiblockLogic.REFINERY.iconStack());

    public static final EmiRecipeCategory CATEGORY = new TranslatedCategory(
            EmiRecipes.categoryId(IERecipeTypes.REFINERY.get()), ICON, "block.immersiveengineering.refinery");

    private RefineryCategory() {}

    public static int register(EmiRegistry registry) {
        try {
            registry.addCategory(CATEGORY);
            registry.addWorkstation(CATEGORY, ICON);
        } catch (Throwable t) {
            Emisemble.LOGGER.warn("immersiveengineering: could not register the refinery category", t);
            return 0;
        }
        int added = EmiRecipes.addAll(registry, IERecipeTypes.REFINERY.get(),
                holder -> new RefineryEmiRecipe(CATEGORY, IeIds.of(holder), holder.value()));
        Emisemble.LOGGER.info("immersiveengineering: {} refinery recipes", added);
        return added;
    }

    static void page(WidgetHolder widgets) {
        AlphaTextures.add(widgets, TEXTURE, SHEET, 0, 0, WIDTH, HEIGHT, 6, 10);
    }
}
