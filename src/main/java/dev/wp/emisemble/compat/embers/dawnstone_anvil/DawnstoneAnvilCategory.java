package dev.wp.emisemble.compat.embers.dawnstone_anvil;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.function.Function;

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
import net.minecraft.core.registries.BuiltInRegistries;
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
        Set<ResourceLocation> used = new HashSet<>();
        List<RecipeHolder<IDawnstoneAnvilRecipe>> holders;
        try {
            holders = registry.getRecipeManager().getAllRecipesFor(RegistryManager.DAWNSTONE_ANVIL_RECIPE.get());
        } catch (Throwable t) {
            Emisemble.LOGGER.warn("embers: could not read the dawnstone anvil recipes", t);
            return 0;
        }
        for (RecipeHolder<IDawnstoneAnvilRecipe> holder : holders) {
            try {
                added += pagesFor(registry, holder, used);
            } catch (Throwable t) {
                Emisemble.LOGGER.warn("embers: skipping anvil page {}", holder.id(), t);
            }
        }
        return added;
    }

    private static int pagesFor(EmiRegistry registry, RecipeHolder<IDawnstoneAnvilRecipe> holder,
            Set<ResourceLocation> used) {
        List<IDawnstoneAnvilRecipe> variants = variants(holder);
        if (variants.isEmpty()) {
            return 0;
        }
        int added = 0;
        if (holder.value() instanceof AnvilBreakdownRecipe) {
            // One page per recovered item: the 342 variants collapse to 52 outputs, and the input cycles on each page.
            for (Map.Entry<String, List<IDawnstoneAnvilRecipe>> group : group(variants, DawnstoneAnvilCategory::output).entrySet()) {
                ResourceLocation id = synthetic(holder, "out", group.getKey(), used);
                added += EmiRecipes.add(registry, new DawnstoneAnvilPage(id, group.getValue())) ? 1 : 0;
            }
            return added;
        }
        if (holder.value() instanceof AnvilRepairRecipe || holder.value() instanceof AnvilRepairMateriaRecipe) {
            // The repair ingredient is the top row: grouping by it puts the tools one material repairs on one page, and
            // the materia repair - one ingredient for all 605 tools - collapses to a single page.
            Map<String, List<IDawnstoneAnvilRecipe>> byIngredient = group(variants, DawnstoneAnvilCategory::top);
            for (Map.Entry<String, List<IDawnstoneAnvilRecipe>> entry : byIngredient.entrySet()) {
                ResourceLocation id = byIngredient.size() == 1 ? holder.id()
                        : synthetic(holder, "in", entry.getKey(), used);
                added += EmiRecipes.add(registry, new DawnstoneAnvilPage(id, entry.getValue())) ? 1 : 0;
            }
            return added;
        }
        Map<String, List<IDawnstoneAnvilRecipe>> shapes = group(variants, DawnstoneAnvilCategory::shape);
        for (Map.Entry<String, List<IDawnstoneAnvilRecipe>> entry : shapes.entrySet()) {
            ResourceLocation id = shapes.size() == 1 ? holder.id() : synthetic(holder, "shape", entry.getKey(), used);
            added += EmiRecipes.add(registry, new DawnstoneAnvilPage(id, entry.getValue())) ? 1 : 0;
        }
        return added;
    }

    // The host's generators are all or nothing, so a fallback rebuilds the two it can when theirs throws.
    private static List<IDawnstoneAnvilRecipe> variants(RecipeHolder<IDawnstoneAnvilRecipe> holder) {
        List<IDawnstoneAnvilRecipe> variants = EmbersCompat.visualVariants(holder);
        if (variants.isEmpty() && holder.value() instanceof AnvilRepairRecipe) {
            Emisemble.LOGGER.info("embers: rebuilding the repair list for {} item by item", holder.id());
            variants = DawnstoneAnvilVariants.repair(holder.id());
        } else if (variants.isEmpty() && holder.value() instanceof AnvilBreakdownRecipe) {
            Emisemble.LOGGER.info("embers: rebuilding the breakdown list for {} item by item", holder.id());
            variants = DawnstoneAnvilVariants.breakdown(holder.id());
        }
        List<IDawnstoneAnvilRecipe> displayable = new ArrayList<>(variants.size());
        for (IDawnstoneAnvilRecipe variant : variants) {
            if (has(variant.getDisplayInputTop()) || has(variant.getDisplayInputBottom())
                    || has(variant.getDisplayOutput())) {
                displayable.add(variant);
            }
        }
        // JEI draws a variant with no slots as an empty page; the count is logged once per recipe that has any.
        if (displayable.size() != variants.size()) {
            Emisemble.LOGGER.info("embers: {} {} of {} anvil variants with nothing to show", holder.id(),
                    variants.size() - displayable.size(), variants.size());
        }
        return displayable;
    }

    private static Map<String, List<IDawnstoneAnvilRecipe>> group(List<IDawnstoneAnvilRecipe> variants,
            Function<IDawnstoneAnvilRecipe, String> key) {
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
        return key(variant.getDisplayInputTop());
    }

    private static String output(IDawnstoneAnvilRecipe variant) {
        return key(variant.getDisplayOutput());
    }

    // The group key is the row's registry ids in row order; a repair tag can name hundreds of items, so it is joined
    // with a character no registry path may contain and shortened before it reaches an id.
    private static String key(List<ItemStack> stacks) {
        StringBuilder key = new StringBuilder();
        for (ItemStack stack : stacks) {
            if (stack.isEmpty()) {
                continue;
            }
            if (!key.isEmpty()) {
                key.append(';');
            }
            key.append(BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath());
        }
        return key.toString();
    }

    private static String idText(String key) {
        int separator = key.indexOf(';');
        if (separator < 0) {
            return key;
        }
        return key.substring(0, separator) + "_" + Integer.toHexString(key.hashCode());
    }

    private static ResourceLocation synthetic(RecipeHolder<IDawnstoneAnvilRecipe> holder, String kind, String key,
            Set<ResourceLocation> used) {
        String base = "/" + holder.id().getPath() + "/" + kind + "_" + idText(key).replaceAll("[^a-z0-9._-]", "_");
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(holder.id().getNamespace(), base);
        for (int suffix = 2; !used.add(id); suffix++) {
            id = ResourceLocation.fromNamespaceAndPath(holder.id().getNamespace(), base + "_" + suffix);
        }
        return id;
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
