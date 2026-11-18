package dev.wp.emisemble.compat.immersiveengineering.crusher;

import blusunrize.immersiveengineering.api.crafting.CrusherRecipe;
import blusunrize.immersiveengineering.api.crafting.StackWithChance;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.wp.emisemble.porting.RecipeSlots;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * One crusher page. Chance outputs carry their probability on the stack ({@code setChance}), so EMI styles the
 * slot and explains the odds in its own tooltip instead of the host's flat percentage text.
 */
public final class CrusherEmiRecipe extends BasicEmiRecipe {
    private static final int INPUT_X = 2;
    private static final int INPUT_Y = 20;
    private static final int OUTPUT_X = 78;
    private static final int SECONDARY_STRIDE = 44;

    private final EmiIngredient input;
    private final EmiStack output;
    private final List<EmiStack> secondary = new ArrayList<>();
    private final int outputY;

    public CrusherEmiRecipe(EmiRecipeCategory category, ResourceLocation id, CrusherRecipe recipe) {
        super(category, id, CrusherCategory.WIDTH, CrusherCategory.HEIGHT);
        this.input = EmiIngredient.of(recipe.input);
        this.output = EmiStack.of(recipe.output.get());

        for (StackWithChance entry : recipe.secondaryOutputs) {
            ItemStack stack = entry.stack().get();
            if (stack.isEmpty() || entry.chance() <= 0.0F) {
                continue;
            }
            // EMI draws its own chance styling and tooltip from the stack's chance; IE's printed percent is not needed.
            this.secondary.add(EmiStack.of(stack).setChance(entry.chance()));
        }

        // With no chance outputs the single output is centred; each of the first two lifts it 9px to make room.
        this.outputY = 1 + (secondary.isEmpty() ? 18 : secondary.size() < 2 ? 9 : 0);

        inputs.add(input);
        if (!output.isEmpty()) {
            outputs.add(output);
        }
        for (EmiStack stack : secondary) {
            outputs.add(stack);
        }
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        CrusherCategory.addIcon(widgets);
        // Framed like the outputs: the host sets JEI's slot background on the input too, and this page has no art.
        RecipeSlots.addFramed(widgets, input, INPUT_X, INPUT_Y);

        if (output.isEmpty()) {
            RecipeSlots.addEmpty(widgets, OUTPUT_X, outputY);
        } else {
            RecipeSlots.addFramed(widgets, output, OUTPUT_X, outputY).recipeContext(this);
        }

        for (int i = 0; i < secondary.size(); i++) {
            int x = OUTPUT_X + (i / 2) * SECONDARY_STRIDE;
            int y = outputY + 18 + (i % 2) * 18;
            RecipeSlots.addFramed(widgets, secondary.get(i), x, y).recipeContext(this);
        }
    }
}
