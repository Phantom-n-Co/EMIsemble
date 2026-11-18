package dev.wp.emisemble.compat.immersiveengineering;

import blusunrize.immersiveengineering.api.crafting.cache.CachedRecipeList;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.porting.EmiRecipes;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * IE's recipe cache, the list the host hands to JEI: the recipe manager holds one placeholder for its generated lists.
 */
public final class IeRecipes {
    private IeRecipes() {}

    public static <T extends Recipe<?>> int addAll(EmiRegistry registry, CachedRecipeList<T> recipes,
            Function<RecipeHolder<T>, EmiRecipe> factory) {
        return addAll(registry, recipes, recipe -> true, factory);
    }

    /** Returns how many pages landed; 0 while there is no client level (the host reads the same level JEI does). */
    public static <T extends Recipe<?>> int addAll(EmiRegistry registry, CachedRecipeList<T> recipes,
            Predicate<T> include, Function<RecipeHolder<T>, EmiRecipe> factory) {
        int added = 0;
        for (RecipeHolder<T> holder : all(recipes)) {
            try {
                if (include.test(holder.value()) && EmiRecipes.add(registry, factory.apply(holder))) {
                    added++;
                }
            } catch (Throwable t) {
                Emisemble.LOGGER.warn("immersiveengineering: skipping recipe {}", holder.id(), t);
            }
        }
        return added;
    }

    /**
     * The flattened set, for a category that has to see all of it before it can register anything - a family of
     * generated recipes becomes one page, which needs the whole family in hand first.
     */
    public static <T extends Recipe<?>> List<RecipeHolder<T>> all(CachedRecipeList<T> recipes) {
        Level level = Minecraft.getInstance().level;
        if (level == null) {
            Emisemble.LOGGER.warn("immersiveengineering: no client level, skipping one recipe set");
            return List.of();
        }
        try {
            return recipes.getRecipes(level);
        } catch (Throwable t) {
            Emisemble.LOGGER.warn("immersiveengineering: could not read IE's recipe cache", t);
            return List.of();
        }
    }
}
