package dev.wp.emisemble.compat.immersiveengineering.mixer;

import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.api.crafting.MixerRecipe;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.wp.emisemble.porting.FluidTanks;
import dev.wp.emisemble.porting.RecipeSlots;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

/** One mixer page: up to six sized item inputs beside a 58x47 tank of input fluid, and the fluid it produces. */
public final class MixerEmiRecipe extends BasicEmiRecipe {
    static final int FLUID_INPUT_X = 48;
    static final int FLUID_INPUT_Y = 3;
    static final int FLUID_INPUT_WIDTH = 58;
    static final int FLUID_INPUT_HEIGHT = 47;
    static final int FLUID_OUTPUT_X = 139;
    static final int FLUID_OUTPUT_Y = 3;
    static final int FLUID_OUTPUT_WIDTH = 16;
    static final int FLUID_OUTPUT_HEIGHT = 47;
    static final int ITEM_ORIGIN_X = 1;
    static final int ITEM_ORIGIN_Y = 1;
    static final int ITEM_SPACING = 18;
    static final int ITEM_COLUMNS = 2;

    private static final int MIN_TANK_CAPACITY = 2000;

    private final EmiIngredient fluidInput;
    private final EmiStack fluidOutput;
    private final List<EmiIngredient> itemInputs;
    private final int tankCapacity;

    public MixerEmiRecipe(EmiRecipeCategory category, ResourceLocation id, MixerRecipe recipe) {
        super(category, id, MixerCategory.WIDTH, MixerCategory.HEIGHT);
        this.fluidInput = fluidInput(recipe);
        this.fluidOutput = fluidOutput(recipe);
        this.itemInputs = itemInputs(recipe);
        this.tankCapacity = tankCapacity(recipe);

        if (!fluidInput.isEmpty()) {
            inputs.add(fluidInput);
        }
        inputs.addAll(itemInputs);
        if (!fluidOutput.isEmpty()) {
            outputs.add(fluidOutput);
        }
    }

    /** The statics below are shared with the collapsed potion page, so both draw one recipe the same way. */
    static EmiIngredient fluidInput(MixerRecipe recipe) {
        return recipe.fluidInput == null ? EmiStack.EMPTY : FluidTanks.of(recipe.fluidInput);
    }

    static EmiStack fluidOutput(MixerRecipe recipe) {
        FluidStack output = recipe.fluidOutput;
        return output == null || output.isEmpty()
                ? EmiStack.EMPTY
                : EmiStack.of(output.getFluid(), output.getComponentsPatch(), output.getAmount());
    }

    static List<EmiIngredient> itemInputs(MixerRecipe recipe) {
        List<EmiIngredient> items = new ArrayList<>(recipe.itemInputs.size());
        for (IngredientWithSize input : recipe.itemInputs) {
            items.add(EmiIngredient.of(input.getBaseIngredient(), input.getCount()));
        }
        return items;
    }

    static int tankCapacity(MixerRecipe recipe) {
        int inputAmount = recipe.fluidInput == null ? 0 : recipe.fluidInput.amount();
        int outputAmount = recipe.fluidOutput == null ? 0 : recipe.fluidOutput.getAmount();
        return Math.max(MIN_TANK_CAPACITY, Math.max(inputAmount, outputAmount));
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        // The host blits an arrow at (117,19) from mixer.png (178,17) - a region this build ships fully transparent,
        // so there is nothing to draw.
        MixerCategory.page(widgets);
        FluidTanks.add(widgets, fluidInput, FLUID_INPUT_X, FLUID_INPUT_Y,
                FLUID_INPUT_WIDTH, FLUID_INPUT_HEIGHT, tankCapacity);
        FluidTanks.socket(widgets, FLUID_OUTPUT_X, FLUID_OUTPUT_Y, FLUID_OUTPUT_WIDTH, FLUID_OUTPUT_HEIGHT);
        FluidTanks.add(widgets, fluidOutput, FLUID_OUTPUT_X, FLUID_OUTPUT_Y,
                FLUID_OUTPUT_WIDTH, FLUID_OUTPUT_HEIGHT, tankCapacity).recipeContext(this);

        for (int i = 0; i < itemInputs.size(); i++) {
            int x = ITEM_ORIGIN_X + (i % ITEM_COLUMNS) * ITEM_SPACING;
            int y = ITEM_ORIGIN_Y + (i / ITEM_COLUMNS) * ITEM_SPACING;
            RecipeSlots.addFramed(widgets, itemInputs.get(i), x, y);
        }
    }
}
