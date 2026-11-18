package dev.wp.emisemble.compat.malum.runeworking;

import com.sammy.malum.common.recipe.RuneworkingRecipe;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.wp.emisemble.porting.AlphaTextures;
import dev.wp.emisemble.porting.OwnFrameRecipe;
import dev.wp.emisemble.porting.RecipeSlots;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

import java.util.ArrayList;
import java.util.List;

/** One runeworking page: the rune stock on top, the slate in the middle, the rune at the bottom. */
final class RuneworkingEmiRecipe extends BasicEmiRecipe implements OwnFrameRecipe {
    private static final int SECONDARY_X = 58;
    private static final int SECONDARY_Y = 10;
    private static final int INPUT_X = 58;
    private static final int INPUT_Y = 53;
    private static final int OUTPUT_X = 58;
    private static final int OUTPUT_Y = 120;

    private final RecipeHolder<RuneworkingRecipe> holder;
    private final EmiIngredient secondary;
    private final EmiIngredient input;
    private final EmiStack output;

    RuneworkingEmiRecipe(RecipeHolder<RuneworkingRecipe> holder) {
        super(RuneworkingCompat.CATEGORY, holder.id(), RuneworkingCompat.WIDTH, RuneworkingCompat.HEIGHT);
        RuneworkingRecipe recipe = holder.value();
        this.holder = holder;
        this.secondary = sized(recipe.secondaryInput);
        this.input = sized(recipe.input);
        this.output = EmiStack.of(recipe.output);

        inputs.add(secondary);
        inputs.add(input);
        outputs.add(output);
    }

    // SizedIngredient#getItems already carries the stack count, which is the number the host's slot draws.
    private static EmiIngredient sized(SizedIngredient ingredient) {
        ItemStack[] items = ingredient.getItems();
        if (items.length == 0) {
            return EmiStack.EMPTY;
        }
        if (items.length == 1) {
            return EmiStack.of(items[0]);
        }

        List<EmiStack> stacks = new ArrayList<>(items.length);
        for (ItemStack item : items) {
            stacks.add(EmiStack.of(item));
        }
        return EmiIngredient.of(stacks);
    }

    @Override
    public RecipeHolder<?> getBackingRecipe() {
        return holder;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        AlphaTextures.add(widgets, RuneworkingCompat.PAGE, RuneworkingCompat.SHEET,
                -4, -4, RuneworkingCompat.WIDTH + 8, RuneworkingCompat.HEIGHT + 8, 1, 0);
        RecipeSlots.add(widgets, secondary, SECONDARY_X, SECONDARY_Y);
        RecipeSlots.add(widgets, input, INPUT_X, INPUT_Y);
        RecipeSlots.add(widgets, output, OUTPUT_X, OUTPUT_Y).recipeContext(this);
    }
}
