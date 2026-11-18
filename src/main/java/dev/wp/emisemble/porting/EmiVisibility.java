package dev.wp.emisemble.porting;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiStack;

import java.util.function.Predicate;

/** Query-time visibility for content a host hides behind player progress. */
public final class EmiVisibility {
    private static volatile Predicate<EmiStack> hiddenStack;
    private static volatile Predicate<EmiRecipe> hiddenRecipe;

    private EmiVisibility() {}

    public static void hideStacks(Predicate<EmiStack> predicate) {
        hiddenStack = predicate;
    }

    public static void hideRecipes(Predicate<EmiRecipe> predicate) {
        hiddenRecipe = predicate;
    }

    public static Predicate<EmiStack> hiddenStack() {
        return hiddenStack;
    }

    public static Predicate<EmiRecipe> hiddenRecipe() {
        return hiddenRecipe;
    }
}
