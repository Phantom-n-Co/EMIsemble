package dev.wp.emisemble.compat.functionalstorage.dissolution;

import com.buuz135.functionalstorage.recipe.CustomCompactingRecipe;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.wp.emisemble.porting.AlphaTextures;
import dev.wp.emisemble.porting.RecipeSlots;
import net.minecraft.resources.ResourceLocation;

/** One compacting page: the lower item going in, the compacted item the drawer keeps out. */
public final class DissolutionEmiRecipe extends BasicEmiRecipe {
    private static final int INPUT_X = 8;
    private static final int INPUT_Y = 40;
    private static final int OUTPUT_X = 24;
    private static final int OUTPUT_Y = 8;

    private final EmiStack input;
    private final EmiStack output;

    DissolutionEmiRecipe(ResourceLocation id, CustomCompactingRecipe recipe) {
        super(DissolutionCategory.CATEGORY, id, DissolutionCategory.WIDTH, DissolutionCategory.HEIGHT);
        // The host's layout puts lower_input in the input role and higher_input in the output role, so the drawer
        // compacts the first into the second; both stacks keep their recipe count (4 or 9 in, 1 out).
        this.input = EmiStack.of(recipe.getLower_input());
        this.output = EmiStack.of(recipe.getHigher_input());

        if (!input.isEmpty()) {
            inputs.add(input);
        }
        if (!output.isEmpty()) {
            outputs.add(output);
        }
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        AlphaTextures.scaled(widgets, DissolutionCategory.TEXTURE, DissolutionCategory.SHEET,
                0, 0, DissolutionCategory.WIDTH, DissolutionCategory.HEIGHT);
        RecipeSlots.add(widgets, input, INPUT_X, INPUT_Y);
        RecipeSlots.add(widgets, output, OUTPUT_X, OUTPUT_Y).recipeContext(this);
    }
}
