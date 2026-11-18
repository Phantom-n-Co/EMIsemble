package dev.wp.emisemble.compat.apotheosis.salvaging;

import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.affix.salvaging.SalvagingRecipe;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.porting.EmiRecipes;
import dev.wp.emisemble.porting.TranslatedCategory;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** {@code apotheosis:salvaging}, the salvaging table's recipes. */
public final class SalvagingCompat {
    public static final EmiRecipeCategory CATEGORY = new TranslatedCategory(
            EmiRecipes.categoryId(Apoth.RecipeTypes.SALVAGING),
            EmiStack.of(Apoth.Items.SALVAGING_TABLE.value()),
            "title.apotheosis.salvaging");

    private SalvagingCompat() {}

    public static int register(EmiRegistry registry) {
        registry.addCategory(CATEGORY);
        registry.addWorkstation(CATEGORY, EmiStack.of(Apoth.Items.SALVAGING_TABLE.value()));
        // No transfer handler: the host registers none, and its one menu button salvages all twelve inputs at once.

        // The host id-sorts before handing its list to JEI, so both viewers page in the same order.
        List<RecipeHolder<SalvagingRecipe>> holders =
                new ArrayList<>(registry.getRecipeManager().getAllRecipesFor(Apoth.RecipeTypes.SALVAGING));
        holders.sort(Comparator.comparing(RecipeHolder::id));

        int added = 0;
        int empty = 0;
        for (RecipeHolder<SalvagingRecipe> holder : holders) {
            // The constructor reads host ingredients, so it sits inside the guard: a throw escaping register would cost
            // this plugin every category.
            try {
                SalvagingEmiRecipe recipe = new SalvagingEmiRecipe(CATEGORY, holder);
                if (!recipe.isDisplayable()) {
                    empty++;
                    continue;
                }
                if (EmiRecipes.add(registry, recipe)) {
                    added++;
                }
            } catch (Throwable t) {
                Emisemble.LOGGER.warn("Skipping salvaging recipe {}", holder.id(), t);
            }
        }

        if (empty > 0) {
            Emisemble.LOGGER.warn("apotheosis: {} salvaging recipes had no resolvable input and were skipped", empty);
        }
        Emisemble.LOGGER.info("apotheosis: {} salvaging recipes registered", added);
        return added;
    }
}
