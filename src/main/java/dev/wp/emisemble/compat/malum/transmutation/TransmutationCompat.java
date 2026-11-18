package dev.wp.emisemble.compat.malum.transmutation;

import com.sammy.malum.common.recipe.UnchainedTransmutationRecipe;
import com.sammy.malum.registry.common.item.MalumItems;
import com.sammy.malum.registry.common.recipe.MalumRecipeTypes;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.porting.EmiRecipes;
import dev.wp.emisemble.porting.TranslatedCategory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** {@code malum:spirit_transmutation}: one page per recipe group, not per recipe. */
public final class TransmutationCompat {
    static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("malum", "textures/gui/spirit_transmutation_jei.png");
    static final int SHEET = 256;
    // Trimmed to the card the art paints inside the host's 142x83 page, then less the 4px: 140x81 of art, 132x73 page.
    static final int WIDTH = 132;
    static final int HEIGHT = 73;
    static final int INPUT_X = 24;
    static final int INPUT_Y = 23;
    static final int OUTPUT_X = 89;
    static final int OUTPUT_Y = 23;

    // The host's JEI uid, which is not its recipe type key: EmiRecipes.categoryId would report
    // malum:unchained_transmutation and JEMI would keep its shim alongside this category.
    public static final ResourceLocation CATEGORY_ID =
            ResourceLocation.fromNamespaceAndPath("malum", "spirit_transmutation");

    public static final EmiRecipeCategory CATEGORY = new TranslatedCategory(
            CATEGORY_ID,
            EmiStack.of(MalumItems.ARCANE_SPIRIT.get()),
            "malum.jei.spirit_transmutation");

    private TransmutationCompat() {}

    public static int register(EmiRegistry registry) {
        registry.addCategory(CATEGORY);
        registry.addWorkstation(CATEGORY, EmiStack.of(MalumItems.SOULWOOD_TOTEM_BASE.get()));
        int pages = addPages(registry);
        Emisemble.LOGGER.info("malum: {} spirit transmutation recipes registered", pages);
        return pages;
    }

    // One page per group and one per ungrouped recipe, as the host wraps them: a ring only makes sense shown together.
    private static int addPages(EmiRegistry registry) {
        List<RecipeHolder<UnchainedTransmutationRecipe>> recipes;
        try {
            recipes = registry.getRecipeManager().getAllRecipesFor(MalumRecipeTypes.UNCHAINED_TRANSMUTATION.get());
        } catch (Throwable t) {
            Emisemble.LOGGER.warn("malum: could not read the spirit transmutation recipes", t);
            return 0;
        }

        Map<String, List<RecipeHolder<UnchainedTransmutationRecipe>>> groups = new LinkedHashMap<>();
        List<RecipeHolder<UnchainedTransmutationRecipe>> loose = new ArrayList<>();
        for (RecipeHolder<UnchainedTransmutationRecipe> holder : recipes) {
            UnchainedTransmutationRecipe recipe = holder.value();
            // The host drops these inside the wrapper instead, which leaves a page with two empty sockets.
            if (recipe.output.isEmpty() || recipe.ingredient.isEmpty()) {
                continue;
            }
            if (recipe.getGroup().isEmpty()) {
                loose.add(holder);
            } else {
                groups.computeIfAbsent(recipe.getGroup(), key -> new ArrayList<>()).add(holder);
            }
        }

        int pages = 0;
        Set<ResourceLocation> used = new LinkedHashSet<>();
        for (Map.Entry<String, List<RecipeHolder<UnchainedTransmutationRecipe>>> group : groups.entrySet()) {
            pages += page(registry, groupId(group.getKey(), used), group.getValue());
        }
        for (RecipeHolder<UnchainedTransmutationRecipe> holder : loose) {
            pages += page(registry, holder.id(), List.of(holder));
        }
        return pages;
    }

    private static int page(EmiRegistry registry, ResourceLocation id,
                            List<RecipeHolder<UnchainedTransmutationRecipe>> group) {
        return EmiRecipes.add(registry, TransmutationEmiRecipe.of(id, group)) ? 1 : 0;
    }

    /** A group name is string data from the recipe file, so it needs lowercasing and a path that survives it. */
    private static ResourceLocation groupId(String group, Set<ResourceLocation> used) {
        String path = "/spirit_transmutation/" + group.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_./-]", "_");
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath("malum", path);
        for (int suffix = 2; !used.add(id); suffix++) {
            id = ResourceLocation.fromNamespaceAndPath("malum", path + "_" + suffix);
        }
        return id;
    }
}
