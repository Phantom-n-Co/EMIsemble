package dev.wp.emisemble.compat.immersivepetroleum.distillation;

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
import flaxbeard.immersivepetroleum.api.crafting.DistillationTowerRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/** One distillation page: one fluid in, two fluid fractions and the optional item byproduct out. */
public final class DistillationEmiRecipe extends BasicEmiRecipe {
    private static final int IN_X = 11;
    private static final int IN_Y = 22;
    private static final int IN_WIDTH = 16;
    private static final int IN_HEIGHT = 47;
    private static final int OUT_X = 47;
    private static final int OUT_TOP_Y = 6;
    private static final int OUT_BOTTOM_Y = 29;
    private static final int OUT_WIDTH = 16;
    private static final int OUT_HEIGHT = 23;
    private static final int ITEM_X = 77;
    private static final int ITEM_Y = 37;

    private final EmiIngredient input;
    private final List<EmiIngredient> fluidsOut = new ArrayList<>();
    private final List<EmiStack> fluidOutStacks = new ArrayList<>();
    private final EmiIngredient itemsOut;
    private final List<EmiStack> itemStacks;
    private final int time;
    private final int energyPerTick;

    public DistillationEmiRecipe(EmiRecipeCategory category, ResourceLocation id, DistillationTowerRecipe recipe) {
        super(category, id, DistillationCategory.WIDTH, DistillationCategory.HEIGHT);
        this.input = FluidTanks.of(recipe.getInputFluid());
        for (var fluid : recipe.getFluidOutputs()) {
            fluidsOut.add(FluidTanks.of(fluid));
            fluidOutStacks.add(FluidTanks.stack(fluid));
        }
        List<EmiStack> items = new ArrayList<>();
        for (StackWithChance entry : recipe.getItemOutput()) {
            ItemStack stack = entry.stack().get();
            if (!stack.isEmpty()) {
                // EMI styles chance slots itself; the host prints the odds in a tooltip callback.
                items.add(EmiStack.of(stack).setChance(entry.chance()));
            }
        }
        this.itemsOut = items.isEmpty() ? EmiStack.EMPTY : EmiIngredient.of(items);
        this.itemStacks = items;
        this.time = recipe.getTotalProcessTime();
        this.energyPerTick = this.time <= 0 ? 0 : recipe.getTotalProcessEnergy() / this.time;

        inputs.add(input);
        outputs.addAll(fluidOutStacks);
        if (!itemsOut.isEmpty()) {
            outputs.addAll(items);
        }
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addTexture(DistillationCategory.TEXTURE, 0, 0, DistillationCategory.WIDTH, DistillationCategory.HEIGHT,
                0, 0);
        // TankWidget insets its own bounds by a pixel on every side, so the widget is the art's fluid area grown by 1.
        FluidTanks.addExact(widgets, input, IN_X - 1, IN_Y - 1, IN_WIDTH + 2, IN_HEIGHT + 2, capacity(input));
        for (int i = 0; i < fluidsOut.size() && i < 2; i++) {
            int y = i == 0 ? OUT_TOP_Y : OUT_BOTTOM_Y;
            // Both sockets share one interior column, so the halves split at the art's boundary, not JEI's rects.
            int widgetY = i == 0 ? OUT_TOP_Y - 2 : OUT_BOTTOM_Y - 1;
            int widgetHeight = i == 0 ? OUT_HEIGHT + 3 : OUT_HEIGHT + 2;
            FluidTanks.addExact(widgets, fluidsOut.get(i), OUT_X - 1, widgetY, OUT_WIDTH + 2, widgetHeight,
                    capacity(fluidsOut.get(i)))
                    .recipeContext(this);
        }
        if (itemsOut.isEmpty()) {
            RecipeSlots.addEmpty(widgets, ITEM_X, ITEM_Y);
        } else {
            RecipeSlots.addFramed(widgets, itemsOut, ITEM_X, ITEM_Y).recipeContext(this);
        }
        // The host shifts this block 23px right, clear of the input tank that runs down the left edge.
        IpText.addLines(widgets, IeText.energyPerTick(energyPerTick), IeText.seconds(time),
                DistillationCategory.WIDTH, DistillationCategory.HEIGHT, 23);
    }

    private static int capacity(EmiIngredient fluid) {
        return (int) Math.min(Integer.MAX_VALUE, fluid.getAmount());
    }
}
