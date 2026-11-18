package dev.wp.emisemble.compat.malum.weeping_well;

import com.sammy.malum.common.recipe.VoidFavorRecipe;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.wp.emisemble.porting.AlphaTextures;
import dev.wp.emisemble.porting.OwnFrameRecipe;
import dev.wp.emisemble.porting.RecipeSlots;
import net.minecraft.world.item.crafting.RecipeHolder;

/** One input dropped into the well and the single item the void gives back. */
final class WeepingWellEmiRecipe extends BasicEmiRecipe implements OwnFrameRecipe {
    private static final int INPUT_X = 58;
    private static final int INPUT_Y = 53;
    private static final int OUTPUT_X = 58;
    private static final int OUTPUT_Y = 120;

    private final RecipeHolder<VoidFavorRecipe> holder;
    private final EmiIngredient input;
    private final EmiStack result;

    WeepingWellEmiRecipe(RecipeHolder<VoidFavorRecipe> holder) {
        super(WeepingWellCompat.CATEGORY, holder.id(), WeepingWellCompat.WIDTH, WeepingWellCompat.HEIGHT);
        VoidFavorRecipe recipe = holder.value();
        this.holder = holder;
        this.input = EmiIngredient.of(recipe.input);
        this.result = EmiStack.of(recipe.result);

        inputs.add(input);
        outputs.add(result);
    }

    @Override
    public RecipeHolder<?> getBackingRecipe() {
        return holder;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        AlphaTextures.add(widgets, WeepingWellCompat.TEXTURE, WeepingWellCompat.SHEET, -4, -4,
                WeepingWellCompat.WIDTH + 8, WeepingWellCompat.HEIGHT + 8, 1, 0);
        RecipeSlots.add(widgets, input, INPUT_X, INPUT_Y);
        RecipeSlots.add(widgets, result, OUTPUT_X, OUTPUT_Y).recipeContext(this);
    }
}
