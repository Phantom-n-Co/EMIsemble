package dev.wp.emisemble.compat.malum.transmutation;

import com.sammy.malum.common.recipe.UnchainedTransmutationRecipe;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.wp.emisemble.porting.AlphaTextures;
import dev.wp.emisemble.porting.OwnFrameRecipe;
import dev.wp.emisemble.porting.RecipeSlots;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** One transmutation ring or lone recipe: 140x81 art, a single input socket and a single output socket. */
public final class TransmutationEmiRecipe extends BasicEmiRecipe implements OwnFrameRecipe {
    private final EmiIngredient input;
    private final EmiIngredient results;

    private TransmutationEmiRecipe(ResourceLocation id, List<RecipeHolder<UnchainedTransmutationRecipe>> group) {
        super(TransmutationCompat.CATEGORY, id, TransmutationCompat.WIDTH, TransmutationCompat.HEIGHT);
        Set<EmiStack> items = new LinkedHashSet<>();
        Set<EmiStack> outputs = new LinkedHashSet<>();
        for (RecipeHolder<UnchainedTransmutationRecipe> holder : group) {
            UnchainedTransmutationRecipe recipe = holder.value();
            EmiStack result = EmiStack.of(recipe.output);
            if (result.isEmpty()) {
                continue;
            }
            for (ItemStack item : recipe.ingredient.getItems()) {
                EmiStack stack = EmiStack.of(item);
                if (!stack.isEmpty()) {
                    items.add(stack);
                }
            }
            outputs.add(result);
        }
        this.input = EmiIngredient.of(new ArrayList<>(items));
        this.results = EmiIngredient.of(new ArrayList<>(outputs));
        this.inputs.add(input);
        this.outputs.addAll(outputs);
    }

    static TransmutationEmiRecipe of(ResourceLocation id, List<RecipeHolder<UnchainedTransmutationRecipe>> group) {
        return new TransmutationEmiRecipe(id, group);
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        AlphaTextures.add(widgets, TransmutationCompat.TEXTURE, TransmutationCompat.SHEET,
                -4, -4, TransmutationCompat.WIDTH + 8, TransmutationCompat.HEIGHT + 8, 0, 0);
        RecipeSlots.add(widgets, input, TransmutationCompat.INPUT_X, TransmutationCompat.INPUT_Y);
        RecipeSlots.add(widgets, results, TransmutationCompat.OUTPUT_X, TransmutationCompat.OUTPUT_Y).recipeContext(this);
    }
}
