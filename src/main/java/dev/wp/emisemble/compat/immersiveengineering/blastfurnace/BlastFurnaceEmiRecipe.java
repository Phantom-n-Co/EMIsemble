package dev.wp.emisemble.compat.immersiveengineering.blastfurnace;

import blusunrize.immersiveengineering.api.crafting.BlastFurnaceRecipe;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.TextWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.wp.emisemble.compat.immersiveengineering.IeText;
import dev.wp.emisemble.porting.RecipeSlots;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/** One blast furnace page: iron and a fuel in, steel and slag out, with the burn time printed under the arrow. */
public final class BlastFurnaceEmiRecipe extends BasicEmiRecipe {
    static final int WIDTH = 100;
    static final int HEIGHT = 64;
    private static final int PAGE_U = 42;
    private static final int PAGE_V = 9;

    private static final int INPUT_X = 10;
    private static final int INPUT_Y = 8;
    private static final int OUTPUT_X = 70;
    private static final int OUTPUT_Y = 8;
    private static final int SLAG_X = 70;
    private static final int SLAG_Y = 44;

    private static final int FLAME_X = 12;
    private static final int FLAME_Y = 27;
    private static final int FLAME_U = 177;
    private static final int FLAME_V = 0;
    private static final int FLAME_SIZE = 14;
    private static final int ARROW_X = 33;
    private static final int ARROW_Y = 26;
    private static final int ARROW_U = 176;
    private static final int ARROW_V = 14;
    private static final int ARROW_WIDTH = 24;
    private static final int ARROW_HEIGHT = 17;

    private static final int TIME_TEXT_X = 68;
    private static final int TIME_TEXT_Y = 48;
    private static final int TEXT_COLOR = 0xFF777777;
    private static final int TICKS_TO_MS = 50;
    private static final int TIME_MS = 300 * TICKS_TO_MS;

    private final EmiIngredient input;
    private final EmiStack output;
    private final EmiStack slag;
    private final Component time;

    public BlastFurnaceEmiRecipe(EmiRecipeCategory category, ResourceLocation id, BlastFurnaceRecipe recipe) {
        super(category, id, WIDTH, HEIGHT);
        this.input = EmiIngredient.of(recipe.input.getBaseIngredient(), recipe.input.getCount());
        this.output = EmiStack.of(recipe.output.get());
        this.slag = EmiStack.of(recipe.slag.get());
        this.time = IeText.seconds(recipe.time);

        inputs.add(input);
        if (!output.isEmpty()) {
            outputs.add(output);
        }
        if (!slag.isEmpty()) {
            outputs.add(slag);
        }
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addTexture(BlastFurnaceCategory.TEXTURE, 0, 0, WIDTH, HEIGHT, PAGE_U, PAGE_V);
        widgets.addAnimatedTexture(BlastFurnaceCategory.TEXTURE, FLAME_X, FLAME_Y, FLAME_SIZE, FLAME_SIZE,
                FLAME_U, FLAME_V, TIME_MS, false, true, false);
        widgets.addAnimatedTexture(BlastFurnaceCategory.TEXTURE, ARROW_X, ARROW_Y, ARROW_WIDTH, ARROW_HEIGHT,
                ARROW_U, ARROW_V, TIME_MS, true, false, false);

        RecipeSlots.add(widgets, input, INPUT_X, INPUT_Y);
        RecipeSlots.add(widgets, output, OUTPUT_X, OUTPUT_Y).recipeContext(this);
        if (!slag.isEmpty()) {
            RecipeSlots.add(widgets, slag, SLAG_X, SLAG_Y).recipeContext(this);
        }
        widgets.addText(time, TIME_TEXT_X, TIME_TEXT_Y, TEXT_COLOR, false).horizontalAlign(TextWidget.Alignment.END);
    }
}
