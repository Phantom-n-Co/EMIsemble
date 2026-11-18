package dev.wp.emisemble.compat.rechiseled;

import com.supermartijn642.rechiseled.Rechiseled;
import com.supermartijn642.rechiseled.api.chiseling.ChiselingBlockShape;
import com.supermartijn642.rechiseled.api.chiseling.ChiselingEntry;
import com.supermartijn642.rechiseled.api.chiseling.ChiselingRecipe;
import com.supermartijn642.rechiseled.api.chiseling.ChiselingRecipeManager;
import com.supermartijn642.rechiseled.api.chiseling.ItemWithWorth;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.porting.EmiRecipes;
import dev.wp.emisemble.porting.TranslatedCategory;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** {@code rechiseled:chiseling}: one page per chiseling recipe, split into EMI pages of the host's visible grid. */
public final class RechiseledCompat {
    // The host's uid is a JEI-only RecipeType.create and is registered nowhere, so there is no key to read it from.
    public static final EmiRecipeCategory CATEGORY = new TranslatedCategory(
            ResourceLocation.fromNamespaceAndPath("rechiseled", "chiseling"),
            EmiStack.of(Rechiseled.chisel),
            "rechiseled.recipe_category.title");

    private RechiseledCompat() {}

    public static int register(EmiRegistry registry) {
        registry.addCategory(CATEGORY);
        registry.addWorkstation(CATEGORY, EmiStack.of(Rechiseled.chisel));

        // The host keeps chiseling recipes outside the recipe manager, so its own manager is the only source.
        List<ChiselingRecipe> recipes = ChiselingRecipeManager.get(true).getAllRecipes();
        Set<ResourceLocation> used = new HashSet<>();
        int added = 0;
        for (int index = 0; index < recipes.size(); index++) {
            try {
                added += addPages(registry, recipes.get(index), used);
            } catch (Throwable t) {
                // A throw escaping register costs the plugin every category, so one recipe may only cost its pages.
                Emisemble.LOGGER.warn("Skipping rechiseled chiseling recipe #{}", index, t);
            }
        }

        Emisemble.LOGGER.info("rechiseled: {} chiseling recipes registered", added);
        return added;
    }

    private static int addPages(EmiRegistry registry, ChiselingRecipe recipe, Set<ResourceLocation> used) {
        List<ItemWithWorth> inputs = new ArrayList<>();
        List<List<ItemWithWorth>> slots = new ArrayList<>();

        // The host's own order: shape-major, so a page reads block, stairs and slab of one entry before the next.
        for (ChiselingBlockShape shape : ChiselingBlockShape.values()) {
            for (ChiselingEntry entry : recipe.entries()) {
                List<ItemWithWorth> slot = new ArrayList<>(2);
                if (entry.hasRegularItem(shape)) {
                    slot.add(entry.getRegularItem(shape));
                }
                if (entry.hasConnectingItem(shape)) {
                    slot.add(entry.getConnectingItem(shape));
                }
                if (slot.isEmpty()) {
                    continue;
                }
                slots.add(slot);
                inputs.addAll(slot);
            }
        }
        if (slots.isEmpty()) {
            return 0;
        }

        // Every page keeps the whole recipe as its input, so any of its items still reaches all of its pages.
        List<ChiselingEmiRecipe.Cell> cells = slots.stream().map(ChiselingEmiRecipe::cell).toList();
        int size = ChiselingEmiRecipe.PAGE_SIZE;
        int pages = (cells.size() + size - 1) / size;

        int added = 0;
        for (int page = 0; page < pages; page++) {
            int from = page * size;
            ResourceLocation id = idOf(slots.get(from), page, used);
            ChiselingEmiRecipe emi = new ChiselingEmiRecipe(CATEGORY, id, inputs,
                    cells.subList(from, Math.min(from + size, cells.size())));
            if (EmiRecipes.add(registry, emi)) {
                added++;
            }
        }
        return added;
    }

    /** The host's recipes carry neither an id nor a title, so a page is named after the first item it shows. */
    private static ResourceLocation idOf(List<ItemWithWorth> slot, int page, Set<ResourceLocation> used) {
        ResourceLocation item = BuiltInRegistries.ITEM.getKey(slot.getFirst().item());
        String base = item == null ? "unknown" : item.toString().replace(':', '/');
        String suffix = page == 0 ? "" : "/" + (page + 1);
        ResourceLocation id = id(base, suffix);
        // The host merges recipes that share an item, but a pack can still hand two of them the same entry.
        for (int n = 2; !used.add(id); n++) {
            id = id(base + "_" + n, suffix);
        }
        return id;
    }

    private static ResourceLocation id(String path, String suffix) {
        return ResourceLocation.fromNamespaceAndPath("rechiseled", "/chiseling/" + path + suffix);
    }
}
