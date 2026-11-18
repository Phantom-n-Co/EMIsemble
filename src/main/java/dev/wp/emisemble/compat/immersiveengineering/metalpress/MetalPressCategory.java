package dev.wp.emisemble.compat.immersiveengineering.metalpress;

import blusunrize.immersiveengineering.api.crafting.IERecipeTypes;
import blusunrize.immersiveengineering.api.crafting.MetalPressRecipe;
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
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** {@code immersiveengineering:metal_press}. */
public final class MetalPressCategory {
    static final int WIDTH = 100;
    static final int HEIGHT = 50;

    private static final EmiStack ICON = EmiStack.of(IEMultiblockLogic.METAL_PRESS.iconStack());

    public static final EmiRecipeCategory CATEGORY = new TranslatedCategory(
            EmiRecipes.categoryId(IERecipeTypes.METAL_PRESS.get()), ICON, "block.immersiveengineering.metal_press");

    private MetalPressCategory() {}

    public static int register(EmiRegistry registry) {
        try {
            registry.addCategory(CATEGORY);
            registry.addWorkstation(CATEGORY, ICON);
        } catch (Throwable t) {
            Emisemble.LOGGER.warn("immersiveengineering: could not register the metal press category", t);
            return 0;
        }

        Level level = Minecraft.getInstance().level;
        if (level == null) {
            Emisemble.LOGGER.warn("immersiveengineering: no client level, skipping the metal press recipes");
            return 0;
        }

        // STANDARD_RECIPES, not the recipe manager: the host reads the same cache and the same set of pages.
        // Sorting by the mold's translation key keeps every mold's recipes together, as the host's page does.
        List<RecipeHolder<MetalPressRecipe>> recipes = new ArrayList<>(MetalPressRecipe.STANDARD_RECIPES.getRecipes(level));
        recipes.sort(Comparator.comparing(holder -> holder.value().mold.getDescriptionId()));

        int added = 0;
        for (RecipeHolder<MetalPressRecipe> holder : recipes) {
            try {
                if (EmiRecipes.add(registry, new MetalPressEmiRecipe(CATEGORY, IeIds.of(holder), holder.value()))) {
                    added++;
                }
            } catch (Throwable t) {
                Emisemble.LOGGER.warn("immersiveengineering: skipping metal press recipe {}", holder.id(), t);
            }
        }
        Emisemble.LOGGER.info("immersiveengineering: {} metal press recipes", added);
        return added;
    }

    /** The page art: the press blown up to 48x48 at (15, 0), drawn before the slots so they stay readable. */
    static void addIcon(WidgetHolder widgets) {
        widgets.addDrawable(15, 0, 48, 48, (draw, mouseX, mouseY, delta) -> {
            PoseStack pose = draw.pose();
            pose.pushPose();
            pose.scale(3.0F, 3.0F, 1.0F);
            ICON.render(draw, 0, 0, delta, EmiIngredient.RENDER_ICON);
            pose.popPose();
        });
    }
}
