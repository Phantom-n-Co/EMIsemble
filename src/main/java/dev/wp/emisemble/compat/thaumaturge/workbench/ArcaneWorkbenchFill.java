package dev.wp.emisemble.compat.thaumaturge.workbench;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.content.recipe.workbench.ArcaneCraftingRecipe;
import com.leclowndu93150.thaumaturge.content.workbench.MenuArcaneWorkbench;
import com.leclowndu93150.thaumaturge.content.workbench.SlotCrystalEssentia;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.wp.emisemble.porting.ClickSlots;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Fills the workbench in place: cells are topped up where they are, and a crystal stack worth more than the recipe's
 * cost stays untouched, where EMI's filler would pull it out and hand back only the cost.
 */
final class ArcaneWorkbenchFill {
    private static final int GRID_CELLS = 9;

    private final List<EmiIngredient> targets;
    private final int[] costs;
    private final List<Slot> cells = new ArrayList<>(GRID_CELLS);
    private final List<Slot> crystals = new ArrayList<>();
    private final List<Slot> inventory = new ArrayList<>();
    private final Map<Slot, ItemStack> contents = new IdentityHashMap<>();
    private final List<Step> steps = new ArrayList<>();

    private ArcaneWorkbenchFill(EmiRecipe recipe, MenuArcaneWorkbench menu) {
        this.targets = targets(recipe);
        this.costs = costs(recipe);
        for (int i = 0; i < GRID_CELLS; i++) {
            cells.add(menu.slots.get(ArcaneWorkbenchCompat.GRID_START + i));
        }
        List<ResourceKey<IAspect>> primals = MenuArcaneWorkbench.PRIMAL_ORDER;
        for (int i = 0; i < primals.size(); i++) {
            crystals.add(menu.slots.get(ArcaneWorkbenchCompat.CRYSTAL_START + i));
        }
        int end = Math.min(ArcaneWorkbenchCompat.INVENTORY_END, menu.slots.size());
        for (int i = ArcaneWorkbenchCompat.INVENTORY_START; i < end; i++) {
            inventory.add(menu.slots.get(i));
        }
        for (Slot slot : cells) {
            contents.put(slot, slot.getItem().copy());
        }
        for (Slot slot : crystals) {
            contents.put(slot, slot.getItem().copy());
        }
        for (Slot slot : inventory) {
            contents.put(slot, slot.getItem().copy());
        }
    }

    static boolean canFill(EmiRecipe recipe, MenuArcaneWorkbench menu, int amount) {
        return plan(recipe, menu, amount) != null;
    }

    static boolean fill(EmiRecipe recipe, MenuArcaneWorkbench menu, int amount) {
        ArcaneWorkbenchFill plan = plan(recipe, menu, amount);
        if (plan == null) {
            return false;
        }
        for (Step step : plan.steps) {
            if (step.count() <= 0) {
                ClickSlots.move(menu, step.from(), step.to());
            } else {
                ClickSlots.moveCount(menu, step.from(), step.to(), step.count());
            }
        }
        return true;
    }

    private static ArcaneWorkbenchFill plan(EmiRecipe recipe, MenuArcaneWorkbench menu, int amount) {
        if (menu.slots.size() < ArcaneWorkbenchCompat.INVENTORY_START) {
            return null;
        }
        ArcaneWorkbenchFill probe = new ArcaneWorkbenchFill(recipe, menu);
        if (probe.targets == null || probe.costs == null) {
            return null;
        }
        // The batch estimate can be optimistic (tags, slot caps), so keep the largest count that actually plans.
        int limit = Math.min(probe.batches(amount), 64);
        for (int batches = limit; batches >= 1; batches--) {
            ArcaneWorkbenchFill fill = new ArcaneWorkbenchFill(recipe, menu);
            if (fill.grid(batches) && fill.crystals(batches)) {
                return fill;
            }
        }
        return null;
    }

    /** The nine cells the recipe wants, pattern at the page's own origin; EMPTY means the cell has to end empty. */
    private static List<EmiIngredient> targets(EmiRecipe recipe) {
        List<EmiIngredient> want = new ArrayList<>(Collections.nCopies(GRID_CELLS, EmiStack.EMPTY));
        if (recipe.getCategory() == ArcaneWorkbenchCompat.CATEGORY) {
            ArcaneCraftingRecipe host = ArcaneWorkbenchCompat.hostRecipe(recipe);
            if (host == null) {
                return null;
            }
            List<Optional<Ingredient>> grid = ArcaneWorkbenchEmiRecipe.grid(host);
            for (int i = 0; i < GRID_CELLS && i < grid.size(); i++) {
                Optional<Ingredient> ingredient = grid.get(i);
                if (ingredient.isPresent()) {
                    want.set(i, EmiIngredient.of(ingredient.get()));
                }
            }
            return want;
        }
        List<EmiIngredient> inputs = recipe.getInputs();
        if (inputs.size() > GRID_CELLS) {
            return null;
        }
        for (int i = 0; i < inputs.size(); i++) {
            want.set(i, inputs.get(i));
        }
        return want;
    }

    /** Crystal cost per primal; a slot the recipe does not cost is never touched. */
    private static int[] costs(EmiRecipe recipe) {
        List<ResourceKey<IAspect>> primals = MenuArcaneWorkbench.PRIMAL_ORDER;
        int[] cost = new int[primals.size()];
        if (recipe.getCategory() != ArcaneWorkbenchCompat.CATEGORY) {
            return cost;
        }
        ArcaneCraftingRecipe host = ArcaneWorkbenchCompat.hostRecipe(recipe);
        if (host == null) {
            return null;
        }
        for (AspectInstance instance : host.crystalCost().entries()) {
            for (int i = 0; i < primals.size(); i++) {
                if (instance.aspect().is(primals.get(i))) {
                    cost[i] += instance.amount();
                    break;
                }
            }
        }
        return cost;
    }

    private int batches(int amount) {
        List<EmiStack> picks = new ArrayList<>(GRID_CELLS);
        for (int i = 0; i < GRID_CELLS; i++) {
            picks.add(pick(i));
        }
        int max = Integer.MAX_VALUE;
        for (int i = 0; i < GRID_CELLS; i++) {
            EmiStack pick = picks.get(i);
            if (pick == null) {
                continue;
            }
            // A stack a recipe uses in several cells is spent once per cell, so it pays for one batch per copy.
            int copies = 0;
            for (EmiStack other : picks) {
                if (other != null && other.isEqual(pick)) {
                    copies++;
                }
            }
            max = Math.min(max, (count(pick, cells) + count(pick, inventory)) / copies);
            max = Math.min(max, cells.get(i).getMaxStackSize());
        }
        for (int i = 0; i < costs.length; i++) {
            if (costs[i] > 0) {
                max = Math.min(max, crystalsAvailable(i) / costs[i]);
                max = Math.min(max, crystals.get(i).getMaxStackSize() / costs[i]);
            }
        }
        if (max == Integer.MAX_VALUE) {
            max = 1;
        }
        return amount == Integer.MAX_VALUE ? max : Math.min(max, amount + present());
    }

    /** The stack with the most to spend, as EMI's filler resolves a cell. */
    private EmiStack pick(int cell) {
        EmiStack best = null;
        int bestCount = -1;
        for (EmiStack candidate : targets.get(cell).getEmiStacks()) {
            if (candidate.isEmpty()) {
                continue;
            }
            int available = count(candidate, cells) + count(candidate, inventory);
            if (available > bestCount) {
                bestCount = available;
                best = candidate;
            }
        }
        return best;
    }

    /** Whole batches already laid out, so a fill of one more batch means one more, as EMI counts it. */
    private int present() {
        int present = Integer.MAX_VALUE;
        for (int i = 0; i < GRID_CELLS; i++) {
            EmiIngredient target = targets.get(i);
            ItemStack current = at(cells.get(i));
            if (target.isEmpty()) {
                if (!current.isEmpty()) {
                    return 0;
                }
                continue;
            }
            if (!matches(target, current)) {
                return 0;
            }
            present = Math.min(present, current.getCount());
        }
        for (int i = 0; i < costs.length; i++) {
            if (costs[i] > 0) {
                ResourceKey<IAspect> aspect = MenuArcaneWorkbench.PRIMAL_ORDER.get(i);
                int have = SlotCrystalEssentia.isValidCrystal(at(crystals.get(i)), aspect) ? at(crystals.get(i)).getCount() : 0;
                present = Math.min(present, have / costs[i]);
            }
        }
        return present == Integer.MAX_VALUE ? 0 : present;
    }

    private boolean grid(int batches) {
        for (int i = 0; i < GRID_CELLS; i++) {
            Slot cell = cells.get(i);
            ItemStack current = at(cell);
            if (current.isEmpty() || matches(targets.get(i), current)) {
                continue;
            }
            int home = home(i, current);
            if (home >= 0) {
                move(cell, cells.get(home));
            } else if (!evict(cell)) {
                return false;
            }
        }
        for (int i = 0; i < GRID_CELLS; i++) {
            EmiIngredient target = targets.get(i);
            if (target.isEmpty()) {
                continue;
            }
            Slot cell = cells.get(i);
            int missing = batches - Math.min(at(cell).getCount(), batches);
            if (missing > 0 && !topUp(cell, target, missing, batches)) {
                return false;
            }
        }
        return true;
    }

    private int home(int cell, ItemStack stack) {
        for (int j = 0; j < GRID_CELLS; j++) {
            if (j == cell || targets.get(j).isEmpty() || !at(cells.get(j)).isEmpty()) {
                continue;
            }
            if (matches(targets.get(j), stack)) {
                return j;
            }
        }
        return -1;
    }

    private boolean topUp(Slot cell, EmiIngredient target, int missing, int batches) {
        for (Slot other : cells) {
            if (other == cell || !matches(target, at(other))) {
                continue;
            }
            // A cell only has spares above the batch count; taking below it steals what was just placed there.
            missing -= moveCount(other, cell, Math.min(missing, at(other).getCount() - batches));
            if (missing <= 0) {
                return true;
            }
        }
        for (Slot slot : inventory) {
            if (matches(target, at(slot))) {
                missing -= moveCount(slot, cell, missing);
            }
            if (missing <= 0) {
                return true;
            }
        }
        return false;
    }

    private boolean crystals(int batches) {
        List<ResourceKey<IAspect>> primals = MenuArcaneWorkbench.PRIMAL_ORDER;
        for (int i = 0; i < crystals.size(); i++) {
            if (costs[i] <= 0) {
                continue;
            }
            Slot slot = crystals.get(i);
            ItemStack current = at(slot);
            if (current.isEmpty() || SlotCrystalEssentia.isValidCrystal(current, primals.get(i))) {
                continue;
            }
            int home = crystalHome(i, current);
            if (home >= 0) {
                move(slot, crystals.get(home));
            } else if (!evict(slot)) {
                return false;
            }
        }
        for (int i = 0; i < crystals.size(); i++) {
            if (costs[i] <= 0) {
                continue;
            }
            Slot slot = crystals.get(i);
            int want = costs[i] * batches;
            int have = SlotCrystalEssentia.isValidCrystal(at(slot), primals.get(i)) ? at(slot).getCount() : 0;
            int missing = want - have;
            if (missing > 0 && !topUpCrystals(slot, primals.get(i), missing)) {
                return false;
            }
        }
        return true;
    }

    private int crystalHome(int slot, ItemStack stack) {
        List<ResourceKey<IAspect>> primals = MenuArcaneWorkbench.PRIMAL_ORDER;
        for (int j = 0; j < crystals.size(); j++) {
            if (j == slot || costs[j] <= 0 || !at(crystals.get(j)).isEmpty()) {
                continue;
            }
            if (SlotCrystalEssentia.isValidCrystal(stack, primals.get(j))) {
                return j;
            }
        }
        return -1;
    }

    private boolean topUpCrystals(Slot slot, ResourceKey<IAspect> aspect, int missing) {
        for (int j = 0; j < crystals.size(); j++) {
            if (crystals.get(j) == slot || costs[j] > 0) {
                continue;
            }
            if (!SlotCrystalEssentia.isValidCrystal(at(crystals.get(j)), aspect)) {
                continue;
            }
            missing -= moveCount(crystals.get(j), slot, missing);
            if (missing <= 0) {
                return true;
            }
        }
        for (Slot inv : inventory) {
            if (!SlotCrystalEssentia.isValidCrystal(at(inv), aspect)) {
                continue;
            }
            missing -= moveCount(inv, slot, missing);
            if (missing <= 0) {
                return true;
            }
        }
        return false;
    }

    private boolean evict(Slot cell) {
        ItemStack stack = at(cell);
        if (stack.isEmpty()) {
            return true;
        }
        for (Slot slot : inventory) {
            ItemStack current = at(slot);
            if (!current.isEmpty() && ItemStack.isSameItemSameComponents(current, stack)
                    && current.getCount() + stack.getCount() <= slot.getMaxStackSize()) {
                move(cell, slot);
                return true;
            }
        }
        for (Slot slot : inventory) {
            if (at(slot).isEmpty()) {
                move(cell, slot);
                return true;
            }
        }
        return false;
    }

    private static boolean matches(EmiIngredient target, ItemStack stack) {
        if (target == null || target.isEmpty() || stack.isEmpty()) {
            return false;
        }
        EmiStack item = EmiStack.of(stack);
        for (EmiStack candidate : target.getEmiStacks()) {
            if (!candidate.isEmpty() && candidate.isEqual(item)) {
                return true;
            }
        }
        return false;
    }

    private int count(EmiStack pick, List<Slot> slots) {
        int total = 0;
        for (Slot slot : slots) {
            ItemStack stack = at(slot);
            if (!stack.isEmpty() && EmiStack.of(stack).isEqual(pick)) {
                total += stack.getCount();
            }
        }
        return total;
    }

    private int crystalsAvailable(int primal) {
        ResourceKey<IAspect> aspect = MenuArcaneWorkbench.PRIMAL_ORDER.get(primal);
        int total = 0;
        for (Slot slot : crystals) {
            if (SlotCrystalEssentia.isValidCrystal(at(slot), aspect)) {
                total += at(slot).getCount();
            }
        }
        for (Slot slot : inventory) {
            if (SlotCrystalEssentia.isValidCrystal(at(slot), aspect)) {
                total += at(slot).getCount();
            }
        }
        return total;
    }

    private ItemStack at(Slot slot) {
        ItemStack stack = contents.get(slot);
        return stack == null ? ItemStack.EMPTY : stack;
    }

    private void move(Slot from, Slot to) {
        ItemStack source = at(from);
        ItemStack destination = at(to);
        if (source.isEmpty() || from == to) {
            return;
        }
        if (!destination.isEmpty() && !ItemStack.isSameItemSameComponents(source, destination)) {
            contents.put(from, destination.copy());
            contents.put(to, source.copy());
            steps.add(new Step(from, to, 0));
            return;
        }
        int space = to.getMaxStackSize() - destination.getCount();
        int moved = Math.min(space, source.getCount());
        if (moved <= 0) {
            return;
        }
        contents.put(to, destination.isEmpty()
                ? source.copyWithCount(moved) : destination.copyWithCount(destination.getCount() + moved));
        contents.put(from, source.getCount() == moved ? ItemStack.EMPTY : source.copyWithCount(source.getCount() - moved));
        steps.add(new Step(from, to, 0));
    }

    private int moveCount(Slot from, Slot to, int count) {
        ItemStack source = at(from);
        ItemStack destination = at(to);
        if (source.isEmpty() || count <= 0 || from == to) {
            return 0;
        }
        if (!destination.isEmpty() && !ItemStack.isSameItemSameComponents(source, destination)) {
            return 0;
        }
        int moved = Math.min(Math.min(count, source.getCount()), to.getMaxStackSize() - destination.getCount());
        if (moved <= 0) {
            return 0;
        }
        contents.put(to, destination.isEmpty()
                ? source.copyWithCount(moved) : destination.copyWithCount(destination.getCount() + moved));
        contents.put(from, source.getCount() == moved ? ItemStack.EMPTY : source.copyWithCount(source.getCount() - moved));
        steps.add(new Step(from, to, moved));
        return moved;
    }

    private record Step(Slot from, Slot to, int count) {}
}
