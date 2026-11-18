package dev.wp.emisemble.compat.immersiveengineering.squeezer;

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
 * {@code immersiveengineering:squeezer}. The page is a 126x59 slice of the machine's own gui texture, and it
 * already contains the item socket and the fluid tank's frame, so everything is drawn frameless on top of it.
 */
public final class SqueezerCategory {
    static final int WIDTH = 126;
    static final int HEIGHT = 59;

    static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("immersiveengineering", "textures/gui/squeezer.png");
    static final int SHEET = 256;

    private static final EmiStack ICON = EmiStack.of(IEMultiblockLogic.SQUEEZER.iconStack());

    public static final EmiRecipeCategory CATEGORY = new TranslatedCategory(
            EmiRecipes.categoryId(IERecipeTypes.SQUEEZER.get()), ICON, "block.immersiveengineering.squeezer");

    private SqueezerCategory() {}

    public static int register(EmiRegistry registry) {
        try {
            registry.addCategory(CATEGORY);
            registry.addWorkstation(CATEGORY, ICON);
        } catch (Throwable t) {
            Emisemble.LOGGER.warn("immersiveengineering: could not register the squeezer category", t);
            return 0;
        }
        int added = EmiRecipes.addAll(registry, IERecipeTypes.SQUEEZER.get(),
                holder -> new SqueezerEmiRecipe(CATEGORY, IeIds.of(holder), holder.value()));
        Emisemble.LOGGER.info("immersiveengineering: {} squeezer recipes", added);
        return added;
    }

    static void page(WidgetHolder widgets) {
        AlphaTextures.add(widgets, TEXTURE, SHEET, 0, 0, WIDTH, HEIGHT, 6, 12);
    }
}
