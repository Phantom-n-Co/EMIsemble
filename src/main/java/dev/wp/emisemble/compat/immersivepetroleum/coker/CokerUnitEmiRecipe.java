package dev.wp.emisemble.compat.immersivepetroleum.coker;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.wp.emisemble.compat.immersiveengineering.IeText;
import dev.wp.emisemble.compat.immersivepetroleum.IpText;
import dev.wp.emisemble.porting.FluidTanks;
import dev.wp.emisemble.porting.RecipeSlots;
import flaxbeard.immersivepetroleum.api.crafting.CokerUnitRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/** One coker page: fluid and item in on the left, both out on the right, energy and duration beside them. */
public final class CokerUnitEmiRecipe extends BasicEmiRecipe {
    private static final int IN_TANK_X = 2;
    private static final int IN_TANK_Y = 2;
    private static final int TANK_WIDTH = 20;
    private static final int TANK_HEIGHT = 51;
    private static final int OUT_TANK_X = 50;
    private static final int ITEM_IN_X = 4;
    private static final int ITEM_IN_Y = 58;
    private static final int ITEM_OUT_X = 52;

    private static final int TEXT_X = 76;
    private static final int TEXT_Y = 30;
    private static final int TEXT_LINE = 9;
    private static final int TEXT_COLOR = 0xFFFFFFFF;

    private final EmiIngredient itemIn;
    private final EmiIngredient fluidIn;
    private final EmiStack itemOut;
    private final EmiIngredient fluidOut;
    private final EmiStack fluidOutStack;
    private final int time;
    private final int energyPerTick;

    public CokerUnitEmiRecipe(EmiRecipeCategory category, ResourceLocation id, CokerUnitRecipe recipe) {
        super(category, id, CokerUnitCategory.WIDTH, CokerUnitCategory.HEIGHT);
        this.itemIn = EmiIngredient.of(recipe.getInputItem().getBaseIngredient(), recipe.getInputItem().getCount());
        this.fluidIn = FluidTanks.of(recipe.getInputFluid());
        this.itemOut = EmiStack.of(recipe.getOutputItem());
        this.fluidOut = FluidTanks.of(recipe.getOutputFluid());
        this.fluidOutStack = FluidTanks.stack(recipe.getOutputFluid());
        this.time = recipe.getTotalProcessTime();
        this.energyPerTick = this.time <= 0 ? 0 : recipe.getTotalProcessEnergy() / this.time;

        inputs.add(itemIn);
        if (!fluidIn.isEmpty()) {
            inputs.add(fluidIn);
        }
        if (!itemOut.isEmpty()) {
            outputs.add(itemOut);
        }
        if (!fluidOutStack.isEmpty()) {
            outputs.add(fluidOutStack);
        }
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addTexture(CokerUnitCategory.TEXTURE, 0, 0, CokerUnitCategory.WIDTH, CokerUnitCategory.HEIGHT, 0, 0);
        FluidTanks.addExact(widgets, fluidIn, IN_TANK_X + 1, IN_TANK_Y + 1, TANK_WIDTH - 2, TANK_HEIGHT - 2, capacity(fluidIn));
        FluidTanks.addExact(widgets, fluidOut, OUT_TANK_X + 1, IN_TANK_Y + 1, TANK_WIDTH - 2, TANK_HEIGHT - 2, capacity(fluidOut))
                .recipeContext(this);
        RecipeSlots.addFramed(widgets, itemIn, ITEM_IN_X, ITEM_IN_Y);
        if (!itemOut.isEmpty()) {
            RecipeSlots.addFramed(widgets, itemOut, ITEM_OUT_X, ITEM_IN_Y).recipeContext(this);
        } else {
            RecipeSlots.addEmpty(widgets, ITEM_OUT_X, ITEM_IN_Y);
        }
        // The item row sits at y=58 here, so the numbers go beside the tanks instead of under them.
        widgets.addText(IeText.energyPerTick(energyPerTick), TEXT_X, TEXT_Y, 0xFFFFFFFF, false);
        widgets.addText(IeText.seconds(time), TEXT_X, TEXT_Y + TEXT_LINE, 0xFFFFFFFF, false);
    }

    /** JEI passes capacity 0 here, which sizes the tank to the recipe; an empty-looking tank reads as a bug. */
    private static int capacity(EmiIngredient fluid) {
        return (int) Math.min(Integer.MAX_VALUE, fluid.getAmount());
    }
}
