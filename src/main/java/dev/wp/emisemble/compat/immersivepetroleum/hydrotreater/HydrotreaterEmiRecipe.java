package dev.wp.emisemble.compat.immersivepetroleum.hydrotreater;

import blusunrize.immersiveengineering.api.crafting.StackWithChance;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.wp.emisemble.compat.immersiveengineering.IeText;
import dev.wp.emisemble.compat.immersivepetroleum.IpText;
import dev.wp.emisemble.porting.FluidTanks;
import dev.wp.emisemble.porting.RecipeSlots;
import flaxbeard.immersivepetroleum.api.crafting.HighPressureRefineryRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/** One hydrotreater page: two fluids in, one out, plus the optional item byproduct. */
public final class HydrotreaterEmiRecipe extends BasicEmiRecipe {
    private static final int TANK_WIDTH = 20;
    private static final int TANK_HEIGHT = 51;
    private static final int TANK_Y = 3;
    private static final int IN_X = 3;
    private static final int IN_SECOND_X = 25;
    private static final int OUT_X = 71;
    private static final int ITEM_X = 94;
    private static final int ITEM_Y = 21;
    private static final int TANK_CAPACITY = 1000;

    private final EmiIngredient input;
    private final EmiIngredient secondaryInput;
    private final EmiIngredient output;
    private final EmiStack outputStack;
    private final EmiStack itemOut;
    private final int time;
    private final int energyPerTick;

    public HydrotreaterEmiRecipe(EmiRecipeCategory category, ResourceLocation id, HighPressureRefineryRecipe recipe) {
        super(category, id, HydrotreaterCategory.WIDTH, HydrotreaterCategory.HEIGHT);
        this.input = FluidTanks.of(recipe.getPrimaryInputFluid());
        // Absent secondary input comes back as null, not an empty Optional.
        var secondary = recipe.getSecondaryInputFluid();
        this.secondaryInput = secondary == null ? EmiStack.EMPTY : FluidTanks.of(secondary);
        this.output = FluidTanks.of(recipe.getOutputFluid());
        this.outputStack = FluidTanks.stack(recipe.getOutputFluid());
        ItemStack byproduct = recipe.getSecondaryItem() == null ? ItemStack.EMPTY : recipe.getSecondaryItem().stack().get();
        this.itemOut = byproduct.isEmpty()
                ? EmiStack.EMPTY
                : EmiStack.of(byproduct).setChance(recipe.getSecondaryItem().chance());
        this.time = recipe.getTotalProcessTime();
        this.energyPerTick = this.time <= 0 ? 0 : recipe.getTotalProcessEnergy() / this.time;

        inputs.add(input);
        if (!secondaryInput.isEmpty()) {
            inputs.add(secondaryInput);
        }
        outputs.add(outputStack);
        if (!itemOut.isEmpty()) {
            outputs.add(itemOut);
        }
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addTexture(HydrotreaterCategory.TEXTURE, 0, 0, HydrotreaterCategory.WIDTH, HydrotreaterCategory.HEIGHT,
                0, 0);
        FluidTanks.addExact(widgets, input, IN_X + 1, TANK_Y + 1, TANK_WIDTH - 2, TANK_HEIGHT - 2, TANK_CAPACITY);
        if (!secondaryInput.isEmpty()) {
            FluidTanks.addExact(widgets, secondaryInput, IN_SECOND_X + 1, TANK_Y + 1, TANK_WIDTH - 2, TANK_HEIGHT - 2, TANK_CAPACITY);
        }
        FluidTanks.addExact(widgets, output, OUT_X + 1, TANK_Y + 1, TANK_WIDTH - 2, TANK_HEIGHT - 2, TANK_CAPACITY).recipeContext(this);
        if (itemOut.isEmpty()) {
            RecipeSlots.addEmpty(widgets, ITEM_X, ITEM_Y);
        } else {
            RecipeSlots.addFramed(widgets, itemOut, ITEM_X, ITEM_Y).recipeContext(this);
        }
        IpText.addLines(widgets, IeText.energyPerTick(energyPerTick), IeText.seconds(time),
                HydrotreaterCategory.WIDTH, HydrotreaterCategory.HEIGHT, 0);
    }
}
