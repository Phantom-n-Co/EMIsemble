package dev.wp.emisemble.compat.chipped;

import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.compat.chipped.transfer.WorkbenchHandler;
import dev.wp.emisemble.porting.EmiRecipes;
import dev.wp.emisemble.porting.TranslatedCategory;
import earth.terrarium.chipped.common.recipes.ChippedRecipe;
import earth.terrarium.chipped.common.registry.ModItems;
import earth.terrarium.chipped.common.registry.ModRecipeTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.ItemLike;

import java.util.ArrayList;
import java.util.List;

/** {@code chipped:workbench}: the host's recipe turns one tagged set into its candidates. */
public final class ChippedCompat {
    // The host's recipe type key is also its JEI uid, so registering under it is what drops JEMI's shim.
    public static final EmiRecipeCategory CATEGORY = new TranslatedCategory(
            EmiRecipes.categoryId(ModRecipeTypes.WORKBENCH.get()),
            EmiStack.of(ModItems.MASON_TABLE.get()),
            "container.chipped.workbench");

    private static final ItemLike[] STATIONS = {
            ModItems.BOTANIST_WORKBENCH.get(), ModItems.GLASSBLOWER.get(), ModItems.CARPENTERS_TABLE.get(),
            ModItems.LOOM_TABLE.get(), ModItems.MASON_TABLE.get(), ModItems.ALCHEMY_BENCH.get(),
            ModItems.TINKERING_TABLE.get()
    };

    private ChippedCompat() {}

    public static int register(EmiRegistry registry) {
        registry.addCategory(CATEGORY);
        for (ItemLike station : STATIONS) {
            registry.addWorkstation(CATEGORY, EmiStack.of(station));
        }

        int added = 0;
        for (RecipeHolder<ChippedRecipe> holder : registry.getRecipeManager()
                .getAllRecipesFor(ModRecipeTypes.WORKBENCH.get())) {
            try {
                added += addPages(registry, holder);
            } catch (Throwable t) {
                // A throw escaping register costs the plugin every category, so one holder may only cost its pages.
                Emisemble.LOGGER.warn("Skipping chipped workbench recipe {}", holder.id(), t);
            }
        }

        Emisemble.LOGGER.info("chipped: {} workbench recipes registered", added);
        WorkbenchHandler.register(registry);
        return added;
    }

    /** The host gives every ingredient its own page and scrolls its candidates; an EMI page cannot scroll. */
    private static int addPages(EmiRegistry registry, RecipeHolder<ChippedRecipe> holder) {
        List<Ingredient> ingredients = holder.value().ingredients();
        int added = 0;

        for (int index = 0; index < ingredients.size(); index++) {
            Ingredient ingredient = ingredients.get(index);
            List<EmiStack> candidates = new ArrayList<>();
            for (ItemStack stack : ingredient.getItems()) {
                if (!stack.isEmpty()) {
                    candidates.add(EmiStack.of(stack));
                }
            }
            if (candidates.isEmpty()) {
                continue;
            }

            // Every page keeps the whole tag as its input, so any candidate of the set still finds all its pages.
            EmiIngredient input = EmiIngredient.of(ingredient);
            int size = WorkbenchEmiRecipe.PAGE_SIZE;
            int pages = (candidates.size() + size - 1) / size;
            for (int page = 0; page < pages; page++) {
                int from = page * size;
                WorkbenchEmiRecipe recipe = new WorkbenchEmiRecipe(CATEGORY, idOf(holder.id(), index, page), input,
                        candidates.subList(from, Math.min(from + size, candidates.size())));
                if (EmiRecipes.add(registry, recipe)) {
                    added++;
                }
            }
        }
        return added;
    }

    /** The host's recipe id plus the ingredient's place in it: neither the tag nor the page has an id of its own. */
    private static ResourceLocation idOf(ResourceLocation holderId, int index, int page) {
        String suffix = page == 0 ? "" : "/" + (page + 1);
        return ResourceLocation.fromNamespaceAndPath(holderId.getNamespace(),
                "/workbench/" + holderId.getPath() + "/" + index + suffix);
    }
}
