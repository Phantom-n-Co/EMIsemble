package dev.wp.emisemble.compat.immersiveengineering.sawmill;

import blusunrize.immersiveengineering.api.crafting.SawmillRecipe;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.wp.emisemble.porting.RecipeSlots;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/** One sawmill page: log, the strip pass's output, the planks, and the two secondary lists. */
public final class SawmillEmiRecipe extends BasicEmiRecipe {
    private static final int INPUT_X = 3;
    private static final int INPUT_Y = 7;
    private static final int STRIPPED_X = 47;
    private static final int EXTRA_ROW_Y = 29;
    private static final int OUTPUT_X = 95;
    private static final int STRIPPING_COLUMN_X = 47;
    private static final int AGING_COLUMN_X = 91;
    private static final int COLUMN_STRIDE = 18;
    private static final int ROW_STRIDE = 18;

    private final EmiIngredient input;
    private final EmiStack stripped;
    private final EmiStack output;
    /** Positional, so a missing entry leaves its cell empty instead of shifting the rest left. */
    private final List<EmiStack> stripping = new ArrayList<>();
    private final List<EmiStack> aging = new ArrayList<>();

    public SawmillEmiRecipe(EmiRecipeCategory category, ResourceLocation id, SawmillRecipe recipe) {
        super(category, id, SawmillCategory.WIDTH, SawmillCategory.HEIGHT);
        this.input = EmiIngredient.of(recipe.input);
        this.stripped = EmiStack.of(recipe.stripped.get());
        this.output = EmiStack.of(recipe.output.get());
        collect(recipe.secondaryStripping.get(), stripping);
        collect(recipe.secondaryOutputs.get(), aging);

        inputs.add(input);
        if (!stripped.isEmpty()) {
            outputs.add(stripped);
        }
        if (!output.isEmpty()) {
            outputs.add(output);
        }
        for (EmiStack stack : stripping) {
            if (!stack.isEmpty()) {
                outputs.add(stack);
            }
        }
        for (EmiStack stack : aging) {
            if (!stack.isEmpty()) {
                outputs.add(stack);
            }
        }
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        SawmillCategory.page(widgets);
        // Arrows first: the split arrow runs underneath the stripped output's slot.
        if (stripped.isEmpty()) {
            SawmillCategory.plainArrow(widgets);
        } else {
            SawmillCategory.splitArrow(widgets);
        }

        RecipeSlots.add(widgets, input, INPUT_X, INPUT_Y);
        if (!stripped.isEmpty()) {
            RecipeSlots.add(widgets, stripped, STRIPPED_X, INPUT_Y).recipeContext(this);
        }
        if (output.isEmpty()) {
            RecipeSlots.addEmpty(widgets, OUTPUT_X, INPUT_Y);
        } else {
            RecipeSlots.add(widgets, output, OUTPUT_X, INPUT_Y).recipeContext(this);
        }

        for (int i = 0; i < stripping.size(); i++) {
            EmiStack stack = stripping.get(i);
            if (stack.isEmpty()) {
                continue;
            }
            RecipeSlots.add(widgets, stack, column(STRIPPING_COLUMN_X, i), row(i)).recipeContext(this);
        }
        for (int i = 0; i < aging.size(); i++) {
            EmiStack stack = aging.get(i);
            if (stack.isEmpty()) {
                continue;
            }
            RecipeSlots.addFramed(widgets, stack, column(AGING_COLUMN_X, i), row(i)).recipeContext(this);
        }
    }

    private static int column(int startX, int index) {
        return startX + (index % 2) * COLUMN_STRIDE;
    }

    private static int row(int index) {
        return EXTRA_ROW_Y + (index / 2) * ROW_STRIDE;
    }

    private static void collect(NonNullList<ItemStack> stacks, List<EmiStack> into) {
        for (ItemStack stack : stacks) {
            into.add(stack.isEmpty() ? EmiStack.EMPTY : EmiStack.of(stack));
        }
    }
}
