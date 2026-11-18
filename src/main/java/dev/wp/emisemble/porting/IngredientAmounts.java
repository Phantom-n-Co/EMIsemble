package dev.wp.emisemble.porting;

import dev.emi.emi.api.stack.EmiIngredient;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

/** A sized ingredient keeps its count on the wrapper, so EMI shows one item unless the amount is passed explicitly. */
public final class IngredientAmounts {
    private IngredientAmounts() {}

    public static EmiIngredient of(SizedIngredient ingredient) {
        return EmiIngredient.of(ingredient.ingredient(), ingredient.count());
    }
}
