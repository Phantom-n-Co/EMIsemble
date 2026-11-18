package dev.wp.emisemble.compat.immersiveengineering.cloche;

import blusunrize.immersiveengineering.api.crafting.ClocheFertilizer;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.wp.emisemble.compat.immersiveengineering.IeText;
import dev.wp.emisemble.porting.RecipeSlots;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/** One cloche fertiliser page: the fertiliser and how much faster it grows the crop. */
public final class ClocheFertilizerEmiRecipe extends BasicEmiRecipe {
    private static final int INPUT_X = 33;
    private static final int INPUT_Y = 13;
    private static final int TEXT_X = 53;
    private static final int TEXT_Y = 17;
    private static final int TEXT_COLOR = 0xFF777777;
    private static final float ICON_SCALE = 3.0F;
/**
 * Icon space: the host's -2 plus the half pixel that centres the 48px icon between the page edge and the slot.
 */
    private static final float ICON_SHIFT = -3.5F;
    private static final int ICON_WIDTH = 54;
    private static final int ICON_HEIGHT = 54;

    private final EmiIngredient fertilizer;
    private final Component growth;

    public ClocheFertilizerEmiRecipe(EmiRecipeCategory category, ResourceLocation id, ClocheFertilizer recipe) {
        super(category, id, ClocheCategory.FERTILIZER_WIDTH, ClocheCategory.FERTILIZER_HEIGHT);
        this.fertilizer = EmiIngredient.of(recipe.input);
        this.growth = Component.translatable("desc.immersiveengineering.jei.cloche_modifier",
                IeText.format(recipe.growthModifier, "#.##"));
        if (!fertilizer.isEmpty()) {
            inputs.add(fertilizer);
        }
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addDrawable(0, 0, ICON_WIDTH, ICON_HEIGHT, (draw, mouseX, mouseY, delta) -> {
            PoseStack pose = draw.pose();
            pose.pushPose();
            pose.scale(ICON_SCALE, ICON_SCALE, 1.0F);
            // Inside the scale: the host scales, then hands its icon -2, so this is 2 icon pixels, not 2 page pixels.
            pose.translate(ICON_SHIFT, 0.0F, 0.0F);
            ClocheCategory.badgeIcon(draw, delta);
            pose.popPose();
        });
        widgets.addText(growth, TEXT_X, TEXT_Y, TEXT_COLOR, false);
        RecipeSlots.addFramed(widgets, fertilizer, INPUT_X, INPUT_Y);
    }
}
