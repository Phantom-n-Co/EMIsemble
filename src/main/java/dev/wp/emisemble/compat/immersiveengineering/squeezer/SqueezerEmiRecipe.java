package dev.wp.emisemble.compat.immersiveengineering.squeezer;

import blusunrize.immersiveengineering.api.crafting.SqueezerRecipe;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.wp.emisemble.porting.RecipeSlots;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

/** One squeezer page: a sized item input, an optional byproduct, and the fluid output. */
public final class SqueezerEmiRecipe extends BasicEmiRecipe {
    private static final int INPUT_X = 2;
    private static final int INPUT_Y = 23;
    private static final int ITEM_OUTPUT_X = 85;
    private static final int ITEM_OUTPUT_Y = 41;
    private static final int TANK_X = 106;
    private static final int TANK_Y = 9;
    private static final int TANK_WIDTH = 16;
    private static final int TANK_HEIGHT = 47;
    private static final int MIN_TANK_CAPACITY = 250;

    private final EmiIngredient input;
    private final EmiStack itemOutput;
    private final EmiStack fluidOutput;
    private final int tankCapacity;

    public SqueezerEmiRecipe(EmiRecipeCategory category, ResourceLocation id, SqueezerRecipe recipe) {
        super(category, id, SqueezerCategory.WIDTH, SqueezerCategory.HEIGHT);
        this.input = EmiIngredient.of(recipe.input.getBaseIngredient(), recipe.input.getCount());

        ItemStack item = recipe.itemOutput.get();
        this.itemOutput = item.isEmpty() ? EmiStack.EMPTY : EmiStack.of(item);

        FluidStack fluid = recipe.fluidOutput;
        if (fluid.isEmpty()) {
            this.fluidOutput = EmiStack.EMPTY;
            this.tankCapacity = MIN_TANK_CAPACITY;
        } else {
            this.fluidOutput = EmiStack.of(fluid.getFluid(), fluid.getComponentsPatch(), fluid.getAmount());
            this.tankCapacity = Math.max(MIN_TANK_CAPACITY, fluid.getAmount());
        }

        inputs.add(input);
        if (!itemOutput.isEmpty()) {
            outputs.add(itemOutput);
        }
        if (!fluidOutput.isEmpty()) {
            outputs.add(fluidOutput);
        }
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        SqueezerCategory.page(widgets);
        RecipeSlots.add(widgets, input, INPUT_X, INPUT_Y);

        if (itemOutput.isEmpty()) {
            RecipeSlots.addEmpty(widgets, ITEM_OUTPUT_X, ITEM_OUTPUT_Y);
        } else {
            RecipeSlots.add(widgets, itemOutput, ITEM_OUTPUT_X, ITEM_OUTPUT_Y).recipeContext(this);
        }

        if (!fluidOutput.isEmpty()) {
            widgets.addTank(fluidOutput, TANK_X - 1, TANK_Y - 1, TANK_WIDTH + 2, TANK_HEIGHT + 2, tankCapacity)
                    .drawBack(false)
                    .recipeContext(this);
        }
    }
}
