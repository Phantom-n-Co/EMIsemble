package dev.wp.emisemble.compat.embers.melting;

import com.rekindled.embers.recipe.IMeltingRecipe;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.wp.emisemble.compat.embers.EmbersCompat;
import dev.wp.emisemble.porting.AlphaTextures;
import dev.wp.emisemble.porting.FluidTanks;
import dev.wp.emisemble.porting.RecipeSlots;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;

/** A melter page, and on the separator page the bonus fluid drawn beside the main one. */
public final class MeltingPage extends BasicEmiRecipe {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MeltingCategory.NAMESPACE, "textures/gui/jei_melter.png");
    private static final ResourceLocation BONUS_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MeltingCategory.NAMESPACE, "textures/gui/jei_geologic_separator.png");
    private static final int SHEET = 256;
    private static final int WIDTH = 108;
    private static final int HEIGHT = 42;
    // the host's art starts 14px down the sheet
    private static final int TEXTURE_V = 14;

    private static final int INPUT_X = 8;
    private static final int INPUT_Y = 14;
    private static final int OUTPUT_Y = 6;
    private static final int TANK_WIDTH = 16;
    private static final int TANK_HEIGHT = 32;
    private static final int MELTING_OUTPUT_X = 84;
    private static final int BONUS_OUTPUT_X = 63;
    private static final int BONUS_X = 84;
    private static final int BONUS_Y = 26;
    private static final int BONUS_HEIGHT = 12;
    // the JEI renderers fill the socket against the melter's capacity instead of its whole height
    private static final double FILL_SCALE = 1.0 / 32.0;
    private static final int MELTER_CAPACITY = FluidType.BUCKET_VOLUME * 4;

    private final IMeltingRecipe recipe;
    private final ResourceLocation texture;
    private final boolean bonus;

    private MeltingPage(EmiRecipeCategory category, RecipeHolder<IMeltingRecipe> holder, boolean bonus) {
        super(category, holder.id(), WIDTH, HEIGHT);
        this.recipe = holder.value();
        this.bonus = bonus;
        this.texture = bonus ? BONUS_TEXTURE : TEXTURE;
        this.inputs.add(EmiIngredient.of(recipe.getDisplayInput()));
        this.outputs.add(FluidTanks.stack(recipe.getDisplayOutput()));
        if (bonus && !recipe.getBonus().isEmpty()) {
            this.outputs.add(FluidTanks.stack(recipe.getBonus()));
        }
    }

    public static MeltingPage melting(EmiRecipeCategory category, RecipeHolder<IMeltingRecipe> holder) {
        return new MeltingPage(category, holder, false);
    }

    public static MeltingPage bonus(EmiRecipeCategory category, RecipeHolder<IMeltingRecipe> holder) {
        return new MeltingPage(category, holder, true);
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        AlphaTextures.add(widgets, texture, SHEET, 0, 0, WIDTH, HEIGHT, 0, TEXTURE_V);
        RecipeSlots.addFramed(widgets, inputs.get(0), INPUT_X, INPUT_Y);

        FluidStack output = recipe.getDisplayOutput();
        EmbersCompat.ingotTooltip(FluidTanks.add(widgets, outputs.get(0), bonus ? BONUS_OUTPUT_X : MELTING_OUTPUT_X,
                OUTPUT_Y, TANK_WIDTH, TANK_HEIGHT, fill(MELTER_CAPACITY, output.getAmount())), output)
                .recipeContext(this);

        if (outputs.size() > 1) {
            FluidStack extra = recipe.getBonus();
            EmbersCompat.ingotTooltip(FluidTanks.add(widgets, outputs.get(1), BONUS_X, BONUS_Y, TANK_WIDTH,
                    BONUS_HEIGHT, fill(FluidType.BUCKET_VOLUME, extra.getAmount())), extra);
        }
    }

    private static int fill(int capacity, int amount) {
        return (int) (capacity * FILL_SCALE + amount * (1.0 - FILL_SCALE));
    }
}
