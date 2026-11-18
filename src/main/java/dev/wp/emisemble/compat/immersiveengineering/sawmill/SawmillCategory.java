package dev.wp.emisemble.compat.immersiveengineering.sawmill;

import blusunrize.immersiveengineering.api.crafting.IERecipeTypes;
import blusunrize.immersiveengineering.common.register.IEMultiblockLogic;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.compat.immersiveengineering.IeIds;
import dev.wp.emisemble.porting.EmiRecipes;
import dev.wp.emisemble.porting.TranslatedCategory;
import net.minecraft.resources.ResourceLocation;

/**
 * {@code immersiveengineering:sawmill}: IE carves the page and its arrow pieces out of one 128x128 sheet.
 */
public final class SawmillCategory {
    static final int WIDTH = 128;
    static final int HEIGHT = 64;

    static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("immersiveengineering", "textures/gui/jei_elements.png");
    static final int SHEET = 128;

    /** The host builds both sawmill arrows with {@code buildAnimated(80, LEFT)}. */
    static final int ARROW_TIME_MS = 80 * 50;

    private static final EmiStack ICON = EmiStack.of(IEMultiblockLogic.SAWMILL.iconStack());

    public static final EmiRecipeCategory CATEGORY = new TranslatedCategory(
            EmiRecipes.categoryId(IERecipeTypes.SAWMILL.get()), ICON, "block.immersiveengineering.sawmill");

    private SawmillCategory() {}

    public static int register(EmiRegistry registry) {
        try {
            registry.addCategory(CATEGORY);
            registry.addWorkstation(CATEGORY, ICON);
        } catch (Throwable t) {
            Emisemble.LOGGER.warn("immersiveengineering: could not register the sawmill category", t);
            return 0;
        }
        int added = EmiRecipes.addAll(registry, IERecipeTypes.SAWMILL.get(),
                holder -> new SawmillEmiRecipe(CATEGORY, IeIds.of(holder), holder.value()));
        Emisemble.LOGGER.info("immersiveengineering: {} sawmill recipes", added);
        return added;
    }

    static void page(WidgetHolder widgets) {
        blit(widgets, 2, 2, 114, 26, 0, 0);
    }

    /** No stripping output: the plain input-to-output arrow, with the machine's middle bar. */
    static void plainArrow(WidgetHolder widgets) {
        blit(widgets, 36, 7, 29, 16, 0, 26);
        animated(widgets, 22, 6, 66, 17, 29, 42);
    }

    /** A stripping output exists: the arrow forks up into the stripped slot. */
    static void splitArrow(WidgetHolder widgets) {
        animated(widgets, 22, 6, 66, 16, 29, 26);
    }

    /** The short animated-texture overload assumes a 256x256 sheet; this one is 128x128 and needs explicit sizes. */
    private static void animated(WidgetHolder widgets, int x, int y, int width, int height, int u, int v) {
        widgets.addAnimatedTexture(TEXTURE, x, y, width, height, u, v, width, height, SHEET, SHEET,
                ARROW_TIME_MS, true, false, false);
    }

    private static void blit(WidgetHolder widgets, int x, int y, int width, int height, int u, int v) {
        widgets.addTexture(TEXTURE, x, y, width, height, u, v, width, height, SHEET, SHEET);
    }
}
