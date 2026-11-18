package dev.wp.emisemble.compat.immersiveengineering.blueprint;

import blusunrize.immersiveengineering.api.crafting.BlueprintCraftingRecipe;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.wp.emisemble.porting.RecipeSlots;
import net.minecraft.resources.ResourceLocation;

import java.util.Arrays;

/** One blueprint page: the blueprint that unlocks it, its inputs in a two-column grid, its output. */
public final class BlueprintEmiRecipe extends BasicEmiRecipe {
    private static final int BLUEPRINT_X = 25;
    private static final int BLUEPRINT_Y = 6;
    private static final int INPUT_X = 81;
    private static final int INPUT_COLUMN = 18;
    private static final int INPUT_ROW = 18;
    // Four inputs fit under the blueprint; a fifth pushes the grid up to keep two full rows on a 54px page.
    private static final int INPUT_Y_LOW = 13;
    private static final int INPUT_Y_HIGH = 1;
    private static final int OUTPUT_X = 141;
    private static final int OUTPUT_Y = 15;

    private final EmiStack blueprint;
    private final EmiStack output;

    public BlueprintEmiRecipe(EmiRecipeCategory category, ResourceLocation id, BlueprintCraftingRecipe recipe) {
        super(category, id, BlueprintCategory.WIDTH, BlueprintCategory.HEIGHT);
        this.blueprint = EmiStack.of(BlueprintCraftingRecipe.getTypedBlueprint(recipe.blueprintCategory));
        this.output = EmiStack.of(recipe.output.get());

        for (IngredientWithSize ingredient : recipe.inputs) {
            inputs.add(EmiIngredient.of(Arrays.stream(ingredient.getMatchingStacks()).map(EmiStack::of).toList()));
        }
        catalysts.add(this.blueprint);
        if (!this.output.isEmpty()) {
            outputs.add(this.output);
        }
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addTexture(BlueprintCategory.TEXTURE, 0, 0, BlueprintCategory.WIDTH, BlueprintCategory.HEIGHT, 0, 11);
        RecipeSlots.addFramed(widgets, blueprint, BLUEPRINT_X, BLUEPRINT_Y);

        int startY = inputs.size() > 4 ? INPUT_Y_HIGH : INPUT_Y_LOW;
        for (int i = 0; i < inputs.size(); i++) {
            RecipeSlots.addFramed(widgets, inputs.get(i),
                    INPUT_X + (i % 2) * INPUT_COLUMN, startY + (i / 2) * INPUT_ROW);
        }

        if (output.isEmpty()) {
            RecipeSlots.addEmpty(widgets, OUTPUT_X, OUTPUT_Y);
        } else {
            RecipeSlots.addFramed(widgets, output, OUTPUT_X, OUTPUT_Y).recipeContext(this);
        }
    }
}
