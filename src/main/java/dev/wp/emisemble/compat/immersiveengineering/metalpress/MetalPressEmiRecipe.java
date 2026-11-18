package dev.wp.emisemble.compat.immersiveengineering.metalpress;

import blusunrize.immersiveengineering.api.crafting.MetalPressRecipe;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.wp.emisemble.porting.RecipeSlots;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;

/**
 * One metal press page. The mold is JEI's CATALYST role: required to run the recipe, never consumed by it, so it
 * goes into the page's catalysts rather than its inputs and EMI will list the recipe as a use of that mold.
 */
public final class MetalPressEmiRecipe extends BasicEmiRecipe {
    private static final int INPUT_X = 1;
    private static final int INPUT_Y = 13;
    private static final int MOLD_X = 57;
    private static final int MOLD_Y = 1;
    private static final int OUTPUT_X = 83;
    private static final int OUTPUT_Y = 13;

    private final EmiIngredient input;
    private final EmiStack mold;
    private final EmiStack output;

    public MetalPressEmiRecipe(EmiRecipeCategory category, ResourceLocation id, MetalPressRecipe recipe) {
        super(category, id, MetalPressCategory.WIDTH, MetalPressCategory.HEIGHT);
        // IngredientWithSize hands back copies already carrying the required count, as the host's own page uses them.
        this.input = EmiIngredient.of(Arrays.stream(recipe.input.getMatchingStacks()).map(EmiStack::of).toList());
        this.mold = EmiStack.of(recipe.mold.getDefaultInstance());
        this.output = EmiStack.of(recipe.output.get());

        inputs.add(this.input);
        catalysts.add(this.mold);
        if (!this.output.isEmpty()) {
            outputs.add(this.output);
        }
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        MetalPressCategory.addIcon(widgets);
        RecipeSlots.addFramed(widgets, input, INPUT_X, INPUT_Y);
        RecipeSlots.addFramed(widgets, mold, MOLD_X, MOLD_Y);

        if (output.isEmpty()) {
            RecipeSlots.addEmpty(widgets, OUTPUT_X, OUTPUT_Y);
        } else {
            RecipeSlots.addFramed(widgets, output, OUTPUT_X, OUTPUT_Y).recipeContext(this);
        }
    }
}
