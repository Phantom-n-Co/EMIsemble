package dev.wp.emisemble.compat.embers.mixing;

import java.util.ArrayList;
import java.util.List;

import com.rekindled.embers.recipe.FluidIngredient;
import com.rekindled.embers.recipe.IMixingRecipe;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.wp.emisemble.compat.embers.EmbersCompat;
import dev.wp.emisemble.porting.AlphaTextures;
import dev.wp.emisemble.porting.FluidTanks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;

/** A mixing page: the fluids in the tanks the host lays them out in, and the mix leaving the bottom one. */
public final class MixingPage extends BasicEmiRecipe {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MixingCategory.NAMESPACE, "textures/gui/jei_mixer.png");
    private static final int SHEET = 256;
    private static final int WIDTH = 108;
    private static final int HEIGHT = 124;
    // slot positions in the JEI layout, in display order
    private static final int[][] INPUT_SLOTS = {{34, 7}, {59, 7}, {9, 7}, {83, 7}};
    private static final int TANK_WIDTH = 16;
    private static final int TANK_HEIGHT = 32;
    private static final int OUTPUT_X = 46;
    private static final int OUTPUT_Y = 84;
    private static final int MAX_INPUTS = 4;
    // the JEI fluid renderers fill the sockets against the centrifuge's capacity, not their whole height
    private static final int CAPACITY = FluidType.BUCKET_VOLUME * 8;
    private static final double FILL_SCALE = 1.0 / 1000.0;

    private final IMixingRecipe recipe;
    private final List<FluidStack> displayed = new ArrayList<>();

    public MixingPage(RecipeHolder<IMixingRecipe> holder) {
        super(MixingCategory.CATEGORY, holder.id(), WIDTH, HEIGHT);
        this.recipe = holder.value();
        int count = 0;
        for (FluidIngredient ingredient : recipe.getDisplayInputFluids()) {
            if (count++ >= MAX_INPUTS) {
                break;
            }
            List<FluidStack> fluids = ingredient.getFluids();
            this.displayed.add(fluids.isEmpty() ? FluidStack.EMPTY : fluids.get(0));
            this.inputs.add(FluidTanks.of(fluids.toArray(FluidStack[]::new)));
        }
        this.outputs.add(FluidTanks.stack(recipe.getDisplayOutput()));
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        AlphaTextures.add(widgets, TEXTURE, SHEET, 0, 0, WIDTH, HEIGHT, 0, 0);
        for (int i = 0; i < inputs.size() && i < INPUT_SLOTS.length; i++) {
            FluidStack stack = displayed.get(i);
            EmbersCompat.ingotTooltip(FluidTanks.add(widgets, inputs.get(i), INPUT_SLOTS[i][0], INPUT_SLOTS[i][1],
                    TANK_WIDTH, TANK_HEIGHT, stack.isEmpty() ? CAPACITY : fill(stack.getAmount())), stack);
        }
        EmbersCompat.ingotTooltip(FluidTanks.add(widgets, outputs.get(0), OUTPUT_X, OUTPUT_Y, TANK_WIDTH, TANK_HEIGHT,
                fill(recipe.getDisplayOutput().getAmount())), recipe.getDisplayOutput()).recipeContext(this);
    }

    private static int fill(int amount) {
        return (int) (CAPACITY * FILL_SCALE + amount * (1.0 - FILL_SCALE));
    }
}
