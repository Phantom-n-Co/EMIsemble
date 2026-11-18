package dev.wp.emisemble.compat.immersiveengineering.alloy;

import blusunrize.immersiveengineering.api.crafting.AlloyRecipe;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.wp.emisemble.porting.RecipeSlots;
import net.minecraft.resources.ResourceLocation;

/** One alloy smelter page: two counted inputs into one output. */
public final class AlloySmelterEmiRecipe extends BasicEmiRecipe {
    private static final int INPUT0_X = 2;
    private static final int INPUT0_Y = 2;
    private static final int INPUT1_X = 30;
    private static final int INPUT1_Y = 2;
    private static final int OUTPUT_X = 84;
    private static final int OUTPUT_Y = 20;

    private static final int FLAME_X = 18;
    private static final int FLAME_Y = 21;
    private static final int ARROW_X = 47;
    private static final int ARROW_Y = 20;
    private static final int FLAME_U = 177;
    private static final int FLAME_V = 0;
    private static final int ARROW_U = 176;
    private static final int ARROW_V = 14;
    private static final int ICON_SIZE = 14;
    private static final int ARROW_WIDTH = 24;
    private static final int ARROW_HEIGHT = 17;
    /** JEI's {@code buildAnimated} counts ticks; EMI's animated texture counts milliseconds. */
    private static final int TICKS_TO_MS = 50;
    private static final int TIME_MS = 200 * TICKS_TO_MS;

    private final EmiIngredient input0;
    private final EmiIngredient input1;
    private final EmiStack output;

    public AlloySmelterEmiRecipe(EmiRecipeCategory category, ResourceLocation id, AlloyRecipe recipe) {
        super(category, id, AlloySmelterCategory.WIDTH, AlloySmelterCategory.HEIGHT);
        this.input0 = EmiIngredient.of(recipe.input0.getBaseIngredient(), recipe.input0.getCount());
        this.input1 = EmiIngredient.of(recipe.input1.getBaseIngredient(), recipe.input1.getCount());
        this.output = EmiStack.of(recipe.output.get());

        inputs.add(input0);
        inputs.add(input1);
        if (!output.isEmpty()) {
            outputs.add(output);
        }
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addTexture(AlloySmelterCategory.TEXTURE, 0, 0, AlloySmelterCategory.WIDTH,
                AlloySmelterCategory.HEIGHT, AlloySmelterCategory.PAGE_U, AlloySmelterCategory.PAGE_V);

        // Host: flame animated downward, arrow rightward — EMI spells both with no inversion.
        widgets.addAnimatedTexture(AlloySmelterCategory.TEXTURE, FLAME_X, FLAME_Y, ICON_SIZE, ICON_SIZE,
                FLAME_U, FLAME_V, TIME_MS, false, true, false);
        widgets.addAnimatedTexture(AlloySmelterCategory.TEXTURE, ARROW_X, ARROW_Y, ARROW_WIDTH, ARROW_HEIGHT,
                ARROW_U, ARROW_V, TIME_MS, true, false, false);

        RecipeSlots.add(widgets, input0, INPUT0_X, INPUT0_Y);
        RecipeSlots.add(widgets, input1, INPUT1_X, INPUT1_Y);
        RecipeSlots.add(widgets, output, OUTPUT_X, OUTPUT_Y).recipeContext(this);
    }
}
