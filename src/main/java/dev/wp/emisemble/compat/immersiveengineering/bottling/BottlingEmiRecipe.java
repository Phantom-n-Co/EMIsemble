package dev.wp.emisemble.compat.immersiveengineering.bottling;

import blusunrize.immersiveengineering.api.crafting.BottlingMachineRecipe;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.wp.emisemble.porting.FluidTanks;
import dev.wp.emisemble.porting.RecipeSlots;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/** One bottling machine page: a container in, fluid in, a filled container out. */
public final class BottlingEmiRecipe extends BasicEmiRecipe {
    static final int TANK_X = 24;
    static final int TANK_Y = 2;
    static final int TANK_WIDTH = 16;
    static final int TANK_HEIGHT = 52;
    static final int INPUT_X = 1;
    static final int OUTPUT_X = 101;
    static final int ROW_CENTRE_Y = 29;
    static final int ROW_SPACING = 18;
    static final int CENTRED_ROWS = 3;

    private static final int MIN_TANK_CAPACITY = 1000;

    private final EmiIngredient fluidInput;
    private final List<EmiIngredient> itemInputs;
    private final List<EmiStack> itemOutputs;
    private final int tankCapacity;

    public BottlingEmiRecipe(EmiRecipeCategory category, ResourceLocation id, BottlingMachineRecipe recipe) {
        super(category, id, BottlingCategory.WIDTH, BottlingCategory.HEIGHT);
        this.fluidInput = fluidInput(recipe);
        this.itemInputs = itemInputs(recipe);
        this.itemOutputs = itemOutputs(recipe);
        this.tankCapacity = tankCapacity(recipe);

        if (!fluidInput.isEmpty()) {
            inputs.add(fluidInput);
        }
        inputs.addAll(itemInputs);
        outputs.addAll(itemOutputs);
    }

    /** The statics below are shared with the cycling page, so one variant of a family draws like any other page. */
    static EmiIngredient fluidInput(BottlingMachineRecipe recipe) {
        return recipe.fluidInput == null ? EmiStack.EMPTY : FluidTanks.of(recipe.fluidInput);
    }

    static List<EmiIngredient> itemInputs(BottlingMachineRecipe recipe) {
        List<EmiIngredient> items = new ArrayList<>(recipe.inputs.size());
        for (IngredientWithSize input : recipe.inputs) {
            items.add(EmiIngredient.of(input.getBaseIngredient(), input.getCount()));
        }
        return items;
    }

    static List<EmiStack> itemOutputs(BottlingMachineRecipe recipe) {
        List<EmiStack> items = new ArrayList<>();
        for (ItemStack stack : recipe.output.get()) {
            if (!stack.isEmpty()) {
                items.add(EmiStack.of(stack));
            }
        }
        return items;
    }

    static int tankCapacity(BottlingMachineRecipe recipe) {
        return Math.max(MIN_TANK_CAPACITY, recipe.fluidInput == null ? 0 : recipe.fluidInput.amount());
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        BottlingCategory.machine(widgets);

        // The host's only chrome here is a bare socket - its tank overlay is a blank region of fermenter.png.
        FluidTanks.socket(widgets, TANK_X, TANK_Y, TANK_WIDTH, TANK_HEIGHT);
        FluidTanks.add(widgets, fluidInput, TANK_X, TANK_Y, TANK_WIDTH, TANK_HEIGHT, tankCapacity);

        int inputY = rowY(this.itemInputs.size());
        for (int i = 0; i < this.itemInputs.size(); i++) {
            RecipeSlots.addFramed(widgets, this.itemInputs.get(i), INPUT_X, inputY + i * ROW_SPACING);
        }

        int outputY = rowY(this.itemOutputs.size());
        for (int i = 0; i < this.itemOutputs.size(); i++) {
            RecipeSlots.addFramed(widgets, this.itemOutputs.get(i), OUTPUT_X, outputY + i * ROW_SPACING)
                    .recipeContext(this);
        }
    }

    /** Where a column of {@code count} slots starts, centred on the row the machine centres its own on. */
    static int rowY(int count) {
        return ROW_CENTRE_Y - Math.min(count, CENTRED_ROWS) * ROW_SPACING / 2;
    }
}
