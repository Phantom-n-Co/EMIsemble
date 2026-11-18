package dev.wp.emisemble.compat.cognition.infecting;

import com.cyanogen.cognition.recipe.InfectingRecipe;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.wp.emisemble.compat.cognition.CognitionCompat;
import dev.wp.emisemble.porting.AlphaTextures;
import dev.wp.emisemble.porting.RecipeSlots;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.List;

public final class InfectingEmiRecipe extends BasicEmiRecipe {
    private static final int ARROW_X = 49;
    private static final int ARROW_Y = 21;
    private static final int ARROW_WIDTH = 41;
    private static final int ARROW_HEIGHT = 7;
    private static final int ARROW_V = 66;
    private static final int ARROW_MS = 100 * 50;
    private static final int COUNTER_X = 56;
    private static final int COUNTER_Y = 7;
    private static final int COUNTER_WIDTH = 11;
    private static final int COUNTER_HEIGHT = 9;
    private static final int COUNTER_V = 73;
    private static final int COUNT_TEXT_X = 71;
    private static final int COUNT_TEXT_Y = 8;
    private static final int COUNT_COLOR = 0x8B8B8B;
    private static final int INPUT_X = 18;
    private static final int INPUT_Y = 18;
    private static final int CATALYST_X = 61;
    private static final int CATALYST_Y = 34;
    private static final int OUTPUT_X = 102;
    private static final int OUTPUT_Y = 18;

    private final EmiIngredient input;
    private final EmiIngredient catalyst;
    private final EmiStack output;
    private final int count;

    public InfectingEmiRecipe(EmiRecipeCategory category, RecipeHolder<InfectingRecipe> holder) {
        super(category, holder.id(), InfectingCategory.WIDTH, InfectingCategory.HEIGHT);
        InfectingRecipe recipe = holder.value();
        this.input = EmiIngredient.of(recipe.getIngredient());
        this.catalyst = EmiIngredient.of(InfectingCategory.CATALYSTS);
        this.output = EmiStack.of(recipe.getResultItem(null));
        this.count = recipe.getInfectionCount();

        inputs.add(input);
        catalysts.add(catalyst);
        outputs.add(output);
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        AlphaTextures.add(widgets, InfectingCategory.TEXTURE, 256, 0, 0,
                InfectingCategory.WIDTH, InfectingCategory.HEIGHT, 0, 0);
        widgets.addAnimatedTexture(InfectingCategory.TEXTURE, ARROW_X, ARROW_Y, ARROW_WIDTH, ARROW_HEIGHT, 0, ARROW_V,
                ARROW_MS, true, false, false);
        widgets.addTooltipText(List.of(Component.translatable("jei.cognition.infecting.tooltip")), 46, 19, 47, 11);
        if (count > 1) {
            AlphaTextures.add(widgets, InfectingCategory.TEXTURE, 256, COUNTER_X, COUNTER_Y, COUNTER_WIDTH,
                    COUNTER_HEIGHT, 0, COUNTER_V);
            widgets.addText(Component.literal("x" + count), COUNT_TEXT_X, COUNT_TEXT_Y, COUNT_COLOR, false);
            widgets.addTooltipText(List.of(Component.translatable("jei.cognition.infecting.tooltip_count",
                    CognitionCompat.green(count))), 55, 6, 31, 11);
        }

        RecipeSlots.add(widgets, input, INPUT_X, INPUT_Y);
        RecipeSlots.add(widgets, catalyst, CATALYST_X, CATALYST_Y);
        RecipeSlots.add(widgets, output, OUTPUT_X, OUTPUT_Y).recipeContext(this);
    }
}
