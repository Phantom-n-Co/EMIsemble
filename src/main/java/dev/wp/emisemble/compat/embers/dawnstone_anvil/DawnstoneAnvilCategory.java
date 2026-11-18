package dev.wp.emisemble.compat.embers.dawnstone_anvil;

import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.ArrayList;

import com.rekindled.embers.RegistryManager;
import com.rekindled.embers.recipe.AnvilBreakdownRecipe;
import com.rekindled.embers.recipe.AnvilRepairMateriaRecipe;
import com.rekindled.embers.recipe.AnvilRepairRecipe;
import com.rekindled.embers.recipe.IDawnstoneAnvilRecipe;

import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.compat.embers.EmbersCompat;
import dev.wp.emisemble.porting.EmiRecipes;
import dev.wp.emisemble.porting.TranslatedCategory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

/** {@code embers:dawnstone_anvil}: a tool on the anvil's top slot worked with a material below it. */
public final class DawnstoneAnvilCategory {
    public static final String NAMESPACE = "embers";
    public static final ResourceLocation ID = EmiRecipes.categoryId(RegistryManager.DAWNSTONE_ANVIL_RECIPE.get());
    public static final EmiRecipeCategory CATEGORY = new TranslatedCategory(ID,
            EmiStack.of(RegistryManager.DAWNSTONE_ANVIL_ITEM.get()), "embers.jei.recipe.dawnstone_anvil");

    private DawnstoneAnvilCategory() {}

    public static int register(EmiRegistry registry) {
        registry.addCategory(CATEGORY);
        registry.addWorkstation(CATEGORY, EmiStack.of(RegistryManager.TINKER_HAMMER.get()));
        registry.addWorkstation(CATEGORY, EmiStack.of(RegistryManager.DAWNSTONE_ANVIL_ITEM.get()));
        registry.addWorkstation(CATEGORY, EmiStack.of(RegistryManager.AUTOMATIC_HAMMER_ITEM.get()));

        int added = groupedPages(registry);
        Emisemble.LOGGER.info("embers: {} dawnstone anvil recipes registered", added);
        return added;
    }

    // One page per recipe and row shape, not per variant: the host's split reaches hundreds of pages, and cycling the
    // rows in lockstep shows the same content. A breakdown has no top row, hence the shape key.
    private static int groupedPages(EmiRegistry registry) {
        int added = 0;
        List<RecipeHolder<IDawnstoneAnvilRecipe>> holders;
        try {
            holders = registry.getRecipeManager().getAllRecipesFor(RegistryManager.DAWNSTONE_ANVIL_RECIPE.get());
        } catch (Throwable t) {
            Emisemble.LOGGER.warn("embers: could not read the dawnstone anvil recipes", t);
            return 0;
        }
        for (RecipeHolder<IDawnstoneAnvilRecipe> holder : holders) {
            try {
                added += pagesFor(registry, holder);
            } catch (Throwable t) {
                Emisemble.LOGGER.warn("embers: skipping anvil page {}", holder.id(), t);
            }
        }
        return added;
    }

    private static int pagesFor(EmiRegistry registry, RecipeHolder<IDawnstoneAnvilRecipe> holder) {
        List<IDawnstoneAnvilRecipe> variants = EmbersCompat.visualVariants(holder);
        if (variants.isEmpty()) {
            return 0;
        }
        int added = 0;
        if (holder.value() instanceof AnvilBreakdownRecipe) {
            // One page per recovered item: the 342 variants collapse to 52 outputs, and the input cycles on each page.
            for (Map.Entry<String, List<IDawnstoneAnvilRecipe>> group : group(variants, DawnstoneAnvilCategory::output).entrySet()) {
                ResourceLocation id = synthetic(holder, "out", group.getKey());
                added += EmiRecipes.add(registry, new DawnstoneAnvilPage(id, group.getValue())) ? 1 : 0;
            }
            return added;
        }
        if (holder.value() instanceof AnvilRepairRecipe || holder.value() instanceof AnvilRepairMateriaRecipe) {
            // The repair ingredient is the top row: grouping by it puts the tools one material repairs on one page, and
            // the materia repair - one ingredient for all 605 tools - collapses to a single page.
            Map<String, List<IDawnstoneAnvilRecipe>> byIngredient = group(variants, DawnstoneAnvilCategory::top);
            for (Map.Entry<String, List<IDawnstoneAnvilRecipe>> entry : byIngredient.entrySet()) {
                ResourceLocation id = byIngredient.size() == 1 ? holder.id() : synthetic(holder, "in", entry.getKey());
                added += EmiRecipes.add(registry, new DawnstoneAnvilPage(id, entry.getValue())) ? 1 : 0;
            }
            return added;
        }
        Map<String, List<IDawnstoneAnvilRecipe>> shapes = group(variants, DawnstoneAnvilCategory::shape);
        for (Map.Entry<String, List<IDawnstoneAnvilRecipe>> entry : shapes.entrySet()) {
            ResourceLocation id = shapes.size() == 1 ? holder.id() : synthetic(holder, "shape", entry.getKey());
            added += EmiRecipes.add(registry, new DawnstoneAnvilPage(id, entry.getValue())) ? 1 : 0;
        }
        return added;
    }

    private static Map<String, List<IDawnstoneAnvilRecipe>> group(List<IDawnstoneAnvilRecipe> variants,
            java.util.function.Function<IDawnstoneAnvilRecipe, String> key) {
        Map<String, List<IDawnstoneAnvilRecipe>> grouped = new LinkedHashMap<>();
        for (IDawnstoneAnvilRecipe variant : variants) {
            grouped.computeIfAbsent(key.apply(variant), ignored -> new ArrayList<>()).add(variant);
        }
        return grouped;
    }

    private static String shape(IDawnstoneAnvilRecipe variant) {
        return (has(variant.getDisplayInputTop()) ? "T" : "") + (has(variant.getDisplayInputBottom()) ? "B" : "")
                + (has(variant.getDisplayOutput()) ? "O" : "");
    }



    private static String top(IDawnstoneAnvilRecipe variant) {
        StringBuilder key = new StringBuilder();
        for (ItemStack stack : variant.getDisplayInputTop()) {
            if (!stack.isEmpty()) {
                key.append(stack.getItem()).append(';');
            }
        }
        return key.toString();
    }

    private static String output(IDawnstoneAnvilRecipe variant) {
        StringBuilder key = new StringBuilder();
        for (ItemStack stack : variant.getDisplayOutput()) {
            if (!stack.isEmpty()) {
                key.append(stack.getItem()).append(';');
            }
        }
        return key.toString();
    }

    private static ResourceLocation synthetic(RecipeHolder<IDawnstoneAnvilRecipe> holder, String kind, String key) {
        return ResourceLocation.fromNamespaceAndPath(holder.id().getNamespace(),
                "/" + holder.id().getPath() + "/" + kind + "_" + Integer.toHexString(key.hashCode()));
    }

    private static boolean has(List<ItemStack> stacks) {
        for (ItemStack stack : stacks) {
            if (!stack.isEmpty()) {
                return true;
            }
        }
        return false;
    }
}
