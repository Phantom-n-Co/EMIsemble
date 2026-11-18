package dev.wp.emisemble.compat.immersiveengineering.cokeoven;

import blusunrize.immersiveengineering.api.crafting.CokeOvenRecipe;
import blusunrize.immersiveengineering.common.register.IEFluids;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.wp.emisemble.compat.immersiveengineering.IeText;
import dev.wp.emisemble.porting.AlphaTextures;
import dev.wp.emisemble.porting.FluidTanks;
import dev.wp.emisemble.porting.RecipeSlots;
import net.minecraft.resources.ResourceLocation;

/** One coke oven page, drawn per item: the recipe wants a batch of sixteen, which a page cannot cycle. */
public final class CokeOvenEmiRecipe extends BasicEmiRecipe {
    private static final int INPUT_X = 4;
    private static final int INPUT_Y = 19;
    private static final int OUTPUT_X = 59;
    private static final int OUTPUT_Y = 19;
    private static final int FLAME_X = 31;
    private static final int FLAME_Y = 20;
    private static final int FLAME_U = 177;
    private static final int FLAME_V = 0;
    private static final int FLAME_SIZE = 14;
    private static final int TICKS_TO_MS = 50;
    private static final int TIME_MS = 500 * TICKS_TO_MS;

    private final EmiIngredient input;
    private final EmiStack output;
    private final EmiStack creosote;
    private final int batch;

    public CokeOvenEmiRecipe(EmiRecipeCategory category, ResourceLocation id, CokeOvenRecipe recipe) {
        super(category, id, CokeOvenCategory.WIDTH, CokeOvenCategory.HEIGHT);
        this.batch = recipe.input.getCount();
        this.input = EmiIngredient.of(recipe.input.getBaseIngredient(), 1);
        this.output = EmiStack.of(recipe.output.get());
        // Per item: CokeOvenLogic multiplies it by the batch.
        this.creosote = recipe.creosoteOutput > 0
                ? EmiStack.of(IEFluids.CREOSOTE.getStill(), recipe.creosoteOutput)
                : EmiStack.EMPTY;

        inputs.add(input);
        if (!output.isEmpty()) {
            outputs.add(output);
        }
        if (!creosote.isEmpty()) {
            outputs.add(creosote);
        }
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addTexture(CokeOvenCategory.TEXTURE, 0, 0, CokeOvenCategory.WIDTH, CokeOvenCategory.HEIGHT,
                CokeOvenCategory.PAGE_U, CokeOvenCategory.PAGE_V);
        // The host's flame region in coke_oven.png is blank in 12.4.2, so JEI shows nothing here either.
        widgets.addAnimatedTexture(CokeOvenCategory.TEXTURE, FLAME_X, FLAME_Y, FLAME_SIZE, FLAME_SIZE,
                FLAME_U, FLAME_V, TIME_MS, false, true, false);

        RecipeSlots.add(widgets, input, INPUT_X, INPUT_Y).appendTooltip(IeText.batched(batch));
        if (!output.isEmpty()) {
            RecipeSlots.add(widgets, output, OUTPUT_X, OUTPUT_Y).recipeContext(this);
        }
        if (!creosote.isEmpty()) {
            FluidTanks.add(widgets, creosote, CokeOvenCategory.TANK_X, CokeOvenCategory.TANK_Y,
                    CokeOvenCategory.TANK_WIDTH, CokeOvenCategory.TANK_HEIGHT, CokeOvenCategory.TANK_CAPACITY)
                    .recipeContext(this);
            AlphaTextures.add(widgets, CokeOvenCategory.TEXTURE, 256, CokeOvenCategory.TANK_X, CokeOvenCategory.TANK_Y,
                    CokeOvenCategory.TANK_WIDTH, CokeOvenCategory.TANK_HEIGHT,
                    CokeOvenCategory.TANK_U, CokeOvenCategory.TANK_V);
        }
    }

}
