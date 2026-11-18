package dev.wp.emisemble.compat.immersiveengineering.crusher;

import blusunrize.immersiveengineering.api.crafting.IERecipeTypes;
import blusunrize.immersiveengineering.common.register.IEMultiblockLogic;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.compat.immersiveengineering.IeIds;
import dev.wp.emisemble.porting.EmiRecipes;
import dev.wp.emisemble.porting.TranslatedCategory;

/** {@code immersiveengineering:crusher}: a blank sheet with the machine icon at triple size for decoration. */
public final class CrusherCategory {
    static final int WIDTH = 140;
    static final int HEIGHT = 54;

    private static final EmiStack ICON = EmiStack.of(IEMultiblockLogic.CRUSHER.iconStack());

    public static final EmiRecipeCategory CATEGORY = new TranslatedCategory(
            EmiRecipes.categoryId(IERecipeTypes.CRUSHER.get()), ICON, "block.immersiveengineering.crusher");

    private CrusherCategory() {}

    public static int register(EmiRegistry registry) {
        try {
            registry.addCategory(CATEGORY);
            registry.addWorkstation(CATEGORY, ICON);
        } catch (Throwable t) {
            Emisemble.LOGGER.warn("immersiveengineering: could not register the crusher category", t);
            return 0;
        }
        int added = EmiRecipes.addAll(registry, IERecipeTypes.CRUSHER.get(),
                holder -> new CrusherEmiRecipe(CATEGORY, IeIds.of(holder), holder.value()));
        Emisemble.LOGGER.info("immersiveengineering: {} crusher recipes", added);
        return added;
    }

    /** The page art: the crusher blew up to 48x48 at (24, 0). */
    static void addIcon(WidgetHolder widgets) {
        widgets.addDrawable(24, 0, 48, 48, (draw, mouseX, mouseY, delta) -> {
            PoseStack pose = draw.pose();
            pose.pushPose();
            pose.scale(3.0F, 3.0F, 1.0F);
            ICON.render(draw, 0, 0, delta, EmiIngredient.RENDER_ICON);
            pose.popPose();
        });
    }
}
