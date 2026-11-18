package dev.wp.emisemble.compat.immersiveengineering.cloche;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.crafting.ClocheRecipe;
import blusunrize.immersiveengineering.api.crafting.StackWithChance;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.wp.emisemble.compat.immersiveengineering.IeText;
import dev.wp.emisemble.porting.AlphaTextures;
import dev.wp.emisemble.porting.FluidTanks;
import dev.wp.emisemble.porting.RecipeSlots;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/** One cloche page: seed on soil, an optional fluid, and the produce it grows. */
public final class ClocheEmiRecipe extends BasicEmiRecipe {
    private static final int SEED_X = 62;
    private static final int SEED_Y = 34;
    private static final int SOIL_X = 62;
    private static final int SOIL_Y = 54;
    private static final int OUTPUT_X = 116;
    private static final int OUTPUT_Y = 34;
    private static final int OUTPUT_COLUMNS = 2;
    private static final int OUTPUT_SPACING = 18;
    private static final int FLUID_X = 6;
    private static final int FLUID_Y = 6;
    private static final int FLUID_WIDTH = 20;
    private static final int FLUID_HEIGHT = 51;
    private static final int TANK_CAPACITY = 4000;
    private static final int GAUGE_U = 176;
    private static final int GAUGE_V = 30;
    private static final int ARROW_X = 101;
    private static final int ARROW_Y = 35;
    private static final int ARROW_SIZE = 13;
    private static final int ARROW_U = 181;
    private static final int ARROW_V = 1;
    private static final int FERTILIZER_X = 8;
    private static final int FERTILIZER_Y = 59;
    private static final int ARROW_TIME_MS = 200 * 50;

    private final EmiIngredient seed;
    private final EmiIngredient soil;
    private final EmiIngredient fluid;
    private final List<Produce> produce;
    private final List<EmiStack> fertilizers;

    public ClocheEmiRecipe(EmiRecipeCategory category, ResourceLocation id, ClocheRecipe recipe,
                           List<EmiStack> fertilizers) {
        super(category, id, ClocheCategory.WIDTH, ClocheCategory.HEIGHT);
        this.fertilizers = fertilizers;
        this.seed = EmiIngredient.of(recipe.seed);
        this.soil = EmiIngredient.of(recipe.soil);
        this.fluid = recipe.requiredFluid == null ? EmiStack.EMPTY : FluidTanks.of(recipe.requiredFluid);

        this.produce = new ArrayList<>(recipe.outputs.size());
        for (int i = 0; i < recipe.outputs.size(); i++) {
            StackWithChance output = recipe.outputs.get(i);
            ItemStack stack = output.stack().get();
            if (!stack.isEmpty()) {
                // The index is the grid cell, so an unresolvable output leaves its cell empty instead of shifting the rest.
                produce.add(new Produce(EmiStack.of(stack).setChance(output.chance()), output.chance(), i));
            }
        }

        if (!seed.isEmpty()) {
            inputs.add(seed);
        }
        if (!fluid.isEmpty()) {
            inputs.add(fluid);
        }
        if (!soil.isEmpty()) {
            catalysts.add(soil);
        }
        produce.forEach(output -> outputs.add(output.stack()));
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        ClocheCategory.page(widgets);
        RecipeSlots.add(widgets, seed, SEED_X, SEED_Y);
        RecipeSlots.add(widgets, soil, SOIL_X, SOIL_Y);

        for (Produce output : produce) {
            int x = OUTPUT_X + (output.slot() % OUTPUT_COLUMNS) * OUTPUT_SPACING;
            int y = OUTPUT_Y + (output.slot() / OUTPUT_COLUMNS) * OUTPUT_SPACING;
            RecipeSlots.add(widgets, output.stack(), x, y).recipeContext(this);
        }

        FluidTanks.add(widgets, fluid, FLUID_X, FLUID_Y, FLUID_WIDTH, FLUID_HEIGHT, TANK_CAPACITY);
        // The gauge sits over the fluid, as the host draws it as the slot's overlay.
        AlphaTextures.add(widgets, ClocheCategory.TEXTURE, ClocheCategory.SHEET,
                FLUID_X, FLUID_Y, FLUID_WIDTH, FLUID_HEIGHT, GAUGE_U, GAUGE_V);
        // Same 200-tick left-to-right fill the host builds.
        widgets.addAnimatedTexture(ClocheCategory.TEXTURE, ARROW_X, ARROW_Y, ARROW_SIZE, ARROW_SIZE,
                ARROW_U, ARROW_V, ARROW_TIME_MS, true, false, false);

        if (fertilizers.isEmpty()) {
            RecipeSlots.addEmpty(widgets, FERTILIZER_X, FERTILIZER_Y);
        } else {
            RecipeSlots.add(widgets, EmiIngredient.of(fertilizers), FERTILIZER_X, FERTILIZER_Y);
        }
    }

    /** One produce slot: what it holds and the grid cell the host places it in. */
    private record Produce(EmiStack stack, float chance, int slot) {}
}
