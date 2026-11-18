package dev.wp.emisemble.compat.thaumaturge.workbench;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.content.recipe.workbench.ArcaneCraftingRecipe;
import com.leclowndu93150.thaumaturge.content.workbench.MenuArcaneWorkbench;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.leclowndu93150.thaumaturge.registry.TTMenus;
import com.leclowndu93150.thaumaturge.registry.TTRecipeTypes;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.recipe.handler.StandardRecipeHandler;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.porting.EmiRecipes;
import dev.wp.emisemble.porting.TranslatedCategory;
import dev.wp.emisemble.porting.TransferSlots;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/** {@code thaumaturge:arcane_workbench}: the 3x3 arcane grid, its catalytic vanilla crafting and the fill handler. */
public final class ArcaneWorkbenchCompat {
    public static final EmiRecipeCategory CATEGORY = new TranslatedCategory(
            EmiRecipes.categoryId(TTRecipeTypes.ARCANE.get()), EmiStack.of(TTItems.ARCANE_WORKBENCH.get()),
            "jei.thaumaturge.category.arcane_workbench");

    // JEI: addRecipeTransferHandler(MenuArcaneWorkbench, ARCANE_WORKBENCH, CRAFTING, 1, 9, 17, 36).
    private static final int RESULT_SLOT = 0;
    private static final int GRID_START = 1;
    private static final int GRID_CELLS = 9;
    private static final int CRYSTAL_START = 10;
    private static final int INVENTORY_START = 17;
    private static final int INVENTORY_END = 53;
    private static final int WORKBENCH_SLOTS = GRID_CELLS + 6;

    private ArcaneWorkbenchCompat() {}

    public static int register(EmiRegistry registry) {
        registry.addCategory(CATEGORY);
        registry.addWorkstation(CATEGORY, EmiStack.of(TTItems.ARCANE_WORKBENCH.get()));
        int recipes = EmiRecipes.addAll(registry, TTRecipeTypes.ARCANE.get(), ArcaneWorkbenchEmiRecipe::of);

        // The table also accepts vanilla crafting recipes, which JEI grants it as a second catalyst.
        EmiRecipeCategory crafting = VanillaEmiRecipeCategories.CRAFTING;
        if (crafting != null) {
            registry.addWorkstation(crafting, EmiStack.of(TTItems.ARCANE_WORKBENCH.get()));
        }
        try {
            registry.addRecipeHandler(TTMenus.ARCANE_WORKBENCH.get(), new Handler());
        } catch (Throwable t) {
            Emisemble.LOGGER.warn("thaumaturge: could not register the arcane workbench transfer handler", t);
        }
        return recipes;
    }

    /**
     * The page's inputs spread over the workbench's fifteen slots, index = slot, so a machine that fills positionally
     * can take them; null when the page is not an arcane page of ours.
     */
    public static List<EmiIngredient> slotInputs(EmiRecipe recipe) {
        if (recipe == null || recipe.getCategory() != CATEGORY) {
            return null;
        }
        ArcaneCraftingRecipe host = hostRecipe(recipe);
        if (host == null) {
            return null;
        }
        List<Integer> indices = inventorySlots(host);
        List<EmiIngredient> inputs = recipe.getInputs();
        if (indices.size() != inputs.size()) {
            return null;
        }
        List<EmiIngredient> slots = new ArrayList<>(Collections.nCopies(WORKBENCH_SLOTS, EmiStack.EMPTY));
        for (int i = 0; i < indices.size(); i++) {
            slots.set(indices.get(i), inputs.get(i));
        }
        return slots;
    }

    // The page lists only the cells that hold something, so the slot each input belongs to has to be recomputed.
    private static List<Integer> inventorySlots(ArcaneCraftingRecipe host) {
        List<Integer> indices = new ArrayList<>(WORKBENCH_SLOTS);
        List<Optional<Ingredient>> grid = ArcaneWorkbenchEmiRecipe.grid(host);
        for (int cell = 0; cell < grid.size(); cell++) {
            if (grid.get(cell).isPresent()) {
                indices.add(cell);
            }
        }
        List<AspectInstance> costs = host.crystalCost().entries();
        List<ResourceKey<IAspect>> primals = MenuArcaneWorkbench.PRIMAL_ORDER;
        for (int i = 0; i < primals.size(); i++) {
            if (hasCostFor(costs, primals.get(i))) {
                indices.add(GRID_CELLS + i);
            }
        }
        return indices;
    }

    private static ArcaneCraftingRecipe hostRecipe(EmiRecipe recipe) {
        // The page keeps its host recipe private, but its id is the recipe's own, so the lookup resolves it.
        RecipeHolder<?> holder = recipe.getBackingRecipe();
        return holder != null && holder.value() instanceof ArcaneCraftingRecipe arcane ? arcane : null;
    }

    private static boolean hasCostFor(List<AspectInstance> costs, ResourceKey<IAspect> aspect) {
        for (AspectInstance cost : costs) {
            if (cost.aspect().is(aspect)) {
                return true;
            }
        }
        return false;
    }

    private static final class Handler implements StandardRecipeHandler<MenuArcaneWorkbench> {
        @Override
        public List<Slot> getInputSources(MenuArcaneWorkbench menu) {
            // Grid and crystals first: the filler empties them into the inventory, so what the table holds is a source.
            return TransferSlots.sources(getCraftingSlots(menu),
                    range(menu, INVENTORY_START, INVENTORY_END - INVENTORY_START));
        }

        @Override
        public List<Slot> getCraftingSlots(MenuArcaneWorkbench menu) {
            // The clearing list has to be the superset: grid plus every crystal slot.
            List<Slot> slots = new ArrayList<>(GRID_CELLS + MenuArcaneWorkbench.PRIMAL_ORDER.size());
            slots.addAll(range(menu, GRID_START, GRID_CELLS));
            slots.addAll(range(menu, CRYSTAL_START, MenuArcaneWorkbench.PRIMAL_ORDER.size()));
            return slots;
        }

        @Override
        public List<Slot> getCraftingSlots(EmiRecipe recipe, MenuArcaneWorkbench menu) {
            if (recipe == null) {
                return List.of();
            }
            if (recipe.getCategory() != CATEGORY) {
                // A vanilla crafting page: its inputs are already row-major over the nine cells.
                return range(menu, GRID_START, GRID_CELLS);
            }

            ArcaneCraftingRecipe host = hostRecipe(recipe);
            if (host == null) {
                return List.of();
            }

            // Positional: EMI puts input i in plan[i], so the plan must follow the page's input order.
            List<Integer> indices = inventorySlots(host);
            List<Slot> plan = new ArrayList<>(indices.size());
            for (int index : indices) {
                int slot = index < GRID_CELLS ? GRID_START + index : CRYSTAL_START + index - GRID_CELLS;
                if (slot < menu.slots.size()) {
                    plan.add(menu.slots.get(slot));
                }
            }

            // A short plan would drop ingredients into the wrong slots, which is worse than refusing the fill.
            if (plan.size() != recipe.getInputs().size()) {
                Emisemble.LOGGER.debug("thaumaturge: arcane workbench plan for {} has {} slots for {} inputs",
                        recipe.getId(), plan.size(), recipe.getInputs().size());
                return List.of();
            }
            return plan;
        }

        @Override
        public Slot getOutputSlot(MenuArcaneWorkbench menu) {
            // Taking from SlotArcaneResult is what runs the craft, wand and vis included.
            return menu.slots.isEmpty() ? null : menu.slots.get(RESULT_SLOT);
        }

        @Override
        public boolean supportsRecipe(EmiRecipe recipe) {
            if (recipe == null) {
                return false;
            }
            if (recipe.getCategory() == CATEGORY) {
                return true;
            }
            EmiRecipeCategory crafting = VanillaEmiRecipeCategories.CRAFTING;
            return crafting != null && recipe.getCategory() == crafting;
        }

        private static List<Slot> range(MenuArcaneWorkbench menu, int start, int count) {
            int end = Math.min(start + count, menu.slots.size());
            int from = Math.min(start, end);
            return menu.slots.subList(from, end);
        }
    }
}
