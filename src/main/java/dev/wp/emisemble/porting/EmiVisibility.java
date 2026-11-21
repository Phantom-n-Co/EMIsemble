package dev.wp.emisemble.porting;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiStack;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

/** Query-time visibility for content a host hides behind player progress, keyed by the host that owns it. */
public final class EmiVisibility {
    private static final Map<String, Predicate<EmiStack>> stacks = new ConcurrentHashMap<>();
    private static final Map<String, Predicate<EmiRecipe>> recipes = new ConcurrentHashMap<>();
    private static volatile Predicate<EmiStack> stackFilter;
    private static volatile Predicate<EmiRecipe> recipeFilter;

    private EmiVisibility() {}

    /** Registering an owner again replaces its predicate, so a reload does not accumulate them. */
    public static void hideStacks(String owner, Predicate<EmiStack> predicate) {
        stacks.put(owner, predicate);
        List<Predicate<EmiStack>> all = List.copyOf(stacks.values());
        stackFilter = stack -> all.stream().anyMatch(p -> p.test(stack));
    }

    public static void hideRecipes(String owner, Predicate<EmiRecipe> predicate) {
        recipes.put(owner, predicate);
        List<Predicate<EmiRecipe>> all = List.copyOf(recipes.values());
        recipeFilter = recipe -> all.stream().anyMatch(p -> p.test(recipe));
    }

    public static Predicate<EmiStack> hiddenStack() {
        return stackFilter;
    }

    public static Predicate<EmiRecipe> hiddenRecipe() {
        return recipeFilter;
    }
}
