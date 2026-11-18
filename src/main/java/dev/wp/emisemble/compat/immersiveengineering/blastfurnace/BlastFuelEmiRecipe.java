package dev.wp.emisemble.compat.immersiveengineering.blastfurnace;

import blusunrize.immersiveengineering.api.crafting.BlastFurnaceFuel;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.wp.emisemble.compat.immersiveengineering.IeText;
import dev.wp.emisemble.porting.RecipeSlots;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * One blast furnace fuel page: the vanilla furnace's fuel strip and the burn time beside it. No flame is drawn — the
 * strip carries the unlit socket and the host's lit overlay comes from a different sheet.
 */
public final class BlastFuelEmiRecipe extends BasicEmiRecipe {
    static final int WIDTH = 86;
    static final int HEIGHT = 36;
    private static final int PAGE_U = 55;
    private static final int PAGE_V = 36;
    private static final int STRIP_WIDTH = 18;
    private static final int STRIP_HEIGHT = 36;

    private static final int INPUT_X = 1;
    private static final int INPUT_Y = 17;
    private static final int TEXT_X = 24;
    private static final int TEXT_Y = 12;
    private static final int TEXT_COLOR = 0xFF777777;

    private final EmiIngredient input;
    private final Component time;

    public BlastFuelEmiRecipe(EmiRecipeCategory category, ResourceLocation id, BlastFurnaceFuel recipe) {
        super(category, id, WIDTH, HEIGHT);
        this.input = EmiIngredient.of(recipe.input);
        // JEI divides the burn time by 20 as ints here, unlike the smelting page's float division.
        this.time = IeText.wholeSeconds(recipe.burnTime / 20);
        inputs.add(input);
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addTexture(BlastFurnaceCategory.FURNACE_TEXTURE, 0, 0, STRIP_WIDTH, STRIP_HEIGHT, PAGE_U, PAGE_V);
        RecipeSlots.add(widgets, input, INPUT_X, INPUT_Y);
        widgets.addText(time, TEXT_X, TEXT_Y, TEXT_COLOR, false);
    }
}
