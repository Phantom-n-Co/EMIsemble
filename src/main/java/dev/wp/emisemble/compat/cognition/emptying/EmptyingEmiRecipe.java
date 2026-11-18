package dev.wp.emisemble.compat.cognition.emptying;

import com.cyanogen.cognition.recipe.EmptyingRecipe;
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

public final class EmptyingEmiRecipe extends BasicEmiRecipe {
    private static final int ARROW_X = 35;
    private static final int ARROW_Y = 21;
    private static final int ARROW_WIDTH = 41;
    private static final int ARROW_HEIGHT = 7;
    private static final int ARROW_V = 66;
    private static final int ARROW_MS = 100 * 50;
    private static final int INPUT_X = 10;
    private static final int INPUT_Y = 18;
    private static final int CATALYST_X = 47;
    private static final int CATALYST_Y = 34;
    private static final int OUTPUT_X = 86;
    private static final int OUTPUT_Y = 18;
    private static final int COGNITIUM_X = 110;
    private static final int COGNITIUM_Y = 18;

    private final EmiIngredient input;
    private final EmiStack output;
    private final int gain;

    public EmptyingEmiRecipe(EmiRecipeCategory category, RecipeHolder<EmptyingRecipe> holder) {
        super(category, holder.id(), EmptyingCategory.WIDTH, EmptyingCategory.HEIGHT);
        EmptyingRecipe recipe = holder.value();
        this.input = EmiIngredient.of(recipe.getIngredient());
        this.output = recipe.hasResultStack() ? EmiStack.of(recipe.getResultItem(null)) : EmiStack.EMPTY;
        this.gain = recipe.getCognitiumGain();

        inputs.add(input);
        catalysts.add(EmptyingCategory.FOUNTAIN);
        if (!output.isEmpty()) {
            outputs.add(output);
        }
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        AlphaTextures.add(widgets, EmptyingCategory.TEXTURE, 256, 0, 0,
                EmptyingCategory.WIDTH, EmptyingCategory.HEIGHT, 0, 0);
        widgets.addAnimatedTexture(EmptyingCategory.TEXTURE, ARROW_X, ARROW_Y, ARROW_WIDTH, ARROW_HEIGHT, 0, ARROW_V,
                ARROW_MS, true, false, false);
        widgets.addTooltipText(List.of(Component.translatable("jei.cognition.emptying.tooltip")), 32, 19, 47, 11);

        RecipeSlots.add(widgets, input, INPUT_X, INPUT_Y);
        RecipeSlots.add(widgets, EmptyingCategory.FOUNTAIN, CATALYST_X, CATALYST_Y);
        if (output.isEmpty()) {
            RecipeSlots.addEmpty(widgets, OUTPUT_X, OUTPUT_Y);
        } else {
            RecipeSlots.add(widgets, output, OUTPUT_X, OUTPUT_Y).recipeContext(this);
        }
        CognitionCompat.cognitium(widgets, COGNITIUM_X, COGNITIUM_Y, gain);
    }
}
