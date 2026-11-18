package dev.wp.emisemble.compat.chipped;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.wp.emisemble.porting.RecipeSlots;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/** The tag as one cycling input, and this page's share of its candidates in the host's 7x5 grid. */
final class WorkbenchEmiRecipe extends BasicEmiRecipe {
    static final int WIDTH = 142;
    static final int HEIGHT = 110;

    private static final int COLUMNS = 7;
    private static final int VISIBLE_ROWS = 5;
    static final int PAGE_SIZE = COLUMNS * VISIBLE_ROWS;

    private static final int PITCH = 18;
    private static final int GRID_X = 1;
    private static final int GRID_Y = 21;

    // The host centres its single input on the page width and puts it above the grid.
    private static final int INPUT_X = 63;
    private static final int INPUT_Y = 0;

    private final EmiIngredient input;
    private final List<EmiStack> candidates;

    WorkbenchEmiRecipe(EmiRecipeCategory category, ResourceLocation id, EmiIngredient input,
                       List<EmiStack> candidates) {
        super(category, id, WIDTH, HEIGHT);
        this.input = input;
        this.candidates = List.copyOf(candidates);
        this.inputs.add(input);
        this.outputs.addAll(this.candidates);
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        // The host draws no page art, only slot chrome, so the grid keeps EMI's own frames.
        RecipeSlots.addFramed(widgets, this.input, INPUT_X, INPUT_Y);

        for (int cell = 0; cell < PAGE_SIZE; cell++) {
            int x = GRID_X + (cell % COLUMNS) * PITCH;
            int y = GRID_Y + (cell / COLUMNS) * PITCH;
            // The host paints all five rows whether or not the page fills them, which is what a short last page shows.
            if (cell < this.candidates.size()) {
                RecipeSlots.addFramed(widgets, this.candidates.get(cell), x, y).recipeContext(this);
            } else {
                RecipeSlots.addFramedEmpty(widgets, x, y);
            }
        }
    }
}
