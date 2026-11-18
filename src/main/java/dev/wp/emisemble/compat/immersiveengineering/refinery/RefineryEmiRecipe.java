package dev.wp.emisemble.compat.immersiveengineering.refinery;

import blusunrize.immersiveengineering.api.crafting.RefineryRecipe;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.wp.emisemble.porting.FluidTanks;
import dev.wp.emisemble.porting.RecipeSlots;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.fluids.FluidStack;

/** One refinery page: two fluid inputs and an optional catalyst become one fluid. */
public final class RefineryEmiRecipe extends BasicEmiRecipe {
    private static final int INPUT_0_X = 7;
    private static final int INPUT_1_X = 34;
    private static final int INPUT_Y = 10;
    private static final int INPUT_WIDTH = 16;
    private static final int INPUT_HEIGHT = 47;
    private static final int CATALYST_X = 67;
    private static final int CATALYST_Y = 16;
    private static final int OUTPUT_X = 103;
    private static final int OUTPUT_Y = 10;
    private static final int TANK_CAPACITY = 50;

    private final EmiIngredient input0;
    private final EmiIngredient input1;
    private final EmiIngredient catalyst;
    private final EmiStack output;

    public RefineryEmiRecipe(EmiRecipeCategory category, ResourceLocation id, RefineryRecipe recipe) {
        super(category, id, RefineryCategory.WIDTH, RefineryCategory.HEIGHT);
        this.input0 = recipe.input0 == null ? EmiStack.EMPTY : FluidTanks.of(recipe.input0);
        this.input1 = recipe.input1 == null ? EmiStack.EMPTY : FluidTanks.of(recipe.input1);
        this.catalyst = EmiIngredient.of(recipe.catalyst);

        FluidStack fluid = recipe.output;
        this.output = fluid == null || fluid.isEmpty()
                ? EmiStack.EMPTY
                : EmiStack.of(fluid.getFluid(), fluid.getComponentsPatch(), fluid.getAmount());

        if (!input0.isEmpty()) {
            inputs.add(input0);
        }
        if (!input1.isEmpty()) {
            inputs.add(input1);
        }
        if (!catalyst.isEmpty()) {
            inputs.add(catalyst);
        }
        if (!output.isEmpty()) {
            outputs.add(output);
        }
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        RefineryCategory.page(widgets);
        FluidTanks.add(widgets, input0, INPUT_0_X, INPUT_Y, INPUT_WIDTH, INPUT_HEIGHT, TANK_CAPACITY);
        FluidTanks.add(widgets, input1, INPUT_1_X, INPUT_Y, INPUT_WIDTH, INPUT_HEIGHT, TANK_CAPACITY);
        if (!catalyst.isEmpty()) {
            RecipeSlots.add(widgets, catalyst, CATALYST_X, CATALYST_Y);
        }
        FluidTanks.add(widgets, output, OUTPUT_X, OUTPUT_Y, INPUT_WIDTH, INPUT_HEIGHT, TANK_CAPACITY)
                .recipeContext(this);
    }
}
