package dev.wp.emisemble.compat.embers.stamping;

import java.util.List;

import com.rekindled.embers.recipe.IStampingRecipe;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.wp.emisemble.compat.embers.EmbersCompat;
import dev.wp.emisemble.porting.AlphaTextures;
import dev.wp.emisemble.porting.FluidTanks;
import dev.wp.emisemble.porting.RecipeSlots;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.fluids.FluidStack;

/** A stamping page: item and stamp in, one item out, with the fluid the stamp is applied over. */
public final class StampingPage extends BasicEmiRecipe {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(StampingCategory.NAMESPACE, "textures/gui/jei_stamp.png");
    private static final int SHEET = 256;
    private static final int WIDTH = 108;
    private static final int HEIGHT = 83;
    private static final int ITEM_X = 8;
    private static final int ITEM_Y = 28;
    private static final int STAMP_X = 47;
    private static final int STAMP_Y = 7;
    private static final int OUTPUT_X = 84;
    private static final int OUTPUT_Y = 28;
    private static final int TANK_X = 47;
    private static final int TANK_Y = 48;
    private static final int TANK_WIDTH = 16;
    private static final int TANK_HEIGHT = 32;
    // the JEI fluid renderer fills the socket against the stamp base's capacity, not its whole height
    private static final int BASE_CAPACITY = 1500;
    private static final double FILL_SCALE = 1.0 / 16.0;

    private final IStampingRecipe recipe;
    private final FluidStack fluid;

    public StampingPage(RecipeHolder<IStampingRecipe> holder) {
        super(StampingCategory.CATEGORY, holder.id(), WIDTH, HEIGHT);
        this.recipe = holder.value();
        List<FluidStack> fluids = recipe.getDisplayInputFluid().getFluids();
        this.fluid = fluids.isEmpty() ? FluidStack.EMPTY : fluids.get(0);
        this.inputs.add(EmiIngredient.of(recipe.getDisplayInput()));
        this.inputs.add(FluidTanks.of(fluids.toArray(FluidStack[]::new)));
        this.catalysts.add(EmiIngredient.of(recipe.getDisplayStamp()));
        this.outputs.add(EmiStack.of(recipe.getResultItem()));
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        AlphaTextures.add(widgets, TEXTURE, SHEET, 0, 0, WIDTH, HEIGHT, 0, 0);
        RecipeSlots.addFramed(widgets, inputs.get(0), ITEM_X, ITEM_Y);
        RecipeSlots.addFramed(widgets, catalysts.get(0), STAMP_X, STAMP_Y);
        RecipeSlots.addFramed(widgets, outputs.get(0), OUTPUT_X, OUTPUT_Y).recipeContext(this);
        EmbersCompat.ingotTooltip(FluidTanks.add(widgets, inputs.get(1), TANK_X, TANK_Y, TANK_WIDTH, TANK_HEIGHT,
                (int) (BASE_CAPACITY * FILL_SCALE + fluid.getAmount() * (1.0 - FILL_SCALE))), fluid);
    }
}
