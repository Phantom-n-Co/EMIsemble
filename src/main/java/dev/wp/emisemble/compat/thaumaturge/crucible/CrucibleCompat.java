package dev.wp.emisemble.compat.thaumaturge.crucible;

import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.leclowndu93150.thaumaturge.registry.TTRecipeTypes;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.wp.emisemble.porting.EmiRecipes;
import dev.wp.emisemble.porting.TranslatedCategory;

/** {@code thaumaturge:crucible}. */
public final class CrucibleCompat {
    public static final EmiRecipeCategory CATEGORY = new TranslatedCategory(
            EmiRecipes.categoryId(TTRecipeTypes.CRUCIBLE.get()), EmiStack.of(TTItems.CRUCIBLE.get()),
            "gui.thaumaturge.recipe_type.crucible");

    private CrucibleCompat() {}

    public static int register(EmiRegistry registry) {
        registry.addCategory(CATEGORY);
        registry.addWorkstation(CATEGORY, EmiStack.of(TTItems.CRUCIBLE.get()));
        return EmiRecipes.addAll(registry, TTRecipeTypes.CRUCIBLE.get(), CrucibleEmiRecipe::new);
    }
}
