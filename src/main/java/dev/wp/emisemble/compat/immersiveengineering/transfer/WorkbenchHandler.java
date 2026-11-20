package dev.wp.emisemble.compat.immersiveengineering.transfer;

import blusunrize.immersiveengineering.api.crafting.BlueprintCraftingRecipe;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.common.gui.IESlot;
import blusunrize.immersiveengineering.common.gui.ModWorkbenchContainer;
import blusunrize.immersiveengineering.common.register.IEMenuTypes;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.handler.EmiCraftContext;
import dev.emi.emi.api.recipe.handler.StandardRecipeHandler;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.compat.immersiveengineering.blueprint.BlueprintCategory;
import dev.wp.emisemble.compat.immersiveengineering.blueprint.BlueprintEmiRecipe;
import dev.wp.emisemble.porting.ClickSlots;
import dev.wp.emisemble.porting.ClientTicks;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * The Engineer's Workbench crafts blueprint recipes out of six material slots, and IE gives JEI no transfer for it. This
 * fills those slots and then takes the page's output slot, whose take is what spends the materials.
 */
public final class WorkbenchHandler implements StandardRecipeHandler<ModWorkbenchContainer> {
    private static final int BLUEPRINT_SLOT = 0;
    private static final int MAX_PAGES = 16;

    private WorkbenchHandler() {}

    public static int register(EmiRegistry registry) {
        try {
            registry.addRecipeHandler(IEMenuTypes.MOD_WORKBENCH.getType(), new WorkbenchHandler());
            return 1;
        } catch (Throwable t) {
            Emisemble.LOGGER.warn("immersiveengineering: could not register the workbench transfer handler", t);
            return 0;
        }
    }

    // The blueprint in slot 0 is what builds the six input slots, so a workbench without one has nothing to fill.
    @Override
    public List<Slot> getInputSources(ModWorkbenchContainer menu) {
        List<Slot> slots = new ArrayList<>(inputs(menu));
        if (!menu.slots.isEmpty()) {
            slots.add(menu.slots.get(BLUEPRINT_SLOT));
        }
        slots.addAll(inventory(menu));
        return slots;
    }

    @Override
    public List<Slot> getCraftingSlots(ModWorkbenchContainer menu) {
        return inputs(menu);
    }

    @Override
    public boolean supportsRecipe(EmiRecipe recipe) {
        return recipe != null && recipe.getCategory() == BlueprintCategory.CATEGORY;
    }

    @Override
    public boolean canCraft(EmiRecipe recipe, EmiCraftContext<ModWorkbenchContainer> context) {
        BlueprintCraftingRecipe host = host(recipe);
        if (host == null) {
            return false;
        }
        ModWorkbenchContainer menu = context.getScreenHandler();
        if (plan(menu, host) != null) {
            return true;
        }
        ItemStack blueprint = blueprint(recipe);
        return !blueprint.isEmpty() && !holds(menu, blueprint) && blueprintSlot(menu, blueprint) != null
                && materials(menu, host);
    }

    @Override
    public boolean craft(EmiRecipe recipe, EmiCraftContext<ModWorkbenchContainer> context) {
        return attempt(recipe, context, true);
    }

    /**
     * The blueprint in slot 0 is what makes the six input slots exist, and inserting one rebinds the menu, so a fill
     * that has to place it finishes on a later tick when the new slots are there.
     */
    private static boolean attempt(EmiRecipe recipe, EmiCraftContext<ModWorkbenchContainer> context, boolean mayDefer) {
        Minecraft client = Minecraft.getInstance();
        ModWorkbenchContainer menu = context.getScreenHandler();
        BlueprintCraftingRecipe host = host(recipe);
        if (client.player == null || client.gameMode == null || host == null || !menu.getCarried().isEmpty()) {
            return false;
        }
        ItemStack blueprint = blueprint(recipe);
        if (!blueprint.isEmpty() && !holds(menu, blueprint)) {
            Slot source = blueprintSlot(menu, blueprint);
            if (!mayDefer || source == null) {
                return false;
            }
            ClickSlots.quickMove(menu, source);
            ClientTicks.after(2, () -> {
                if (Minecraft.getInstance().screen == context.getScreen()) {
                    attempt(recipe, context, false);
                }
            });
            return true;
        }
        Plan plan = plan(menu, host);
        if (plan == null) {
            return false;
        }
        for (Move move : plan.moves()) {
            if (move.count() <= 0) {
                ClickSlots.move(menu, move.from(), move.to());
            } else {
                ClickSlots.moveCount(menu, move.from(), move.to(), move.count());
            }
        }
        if (context.getDestination() == EmiCraftContext.Destination.NONE) {
            return true;
        }
        return take(client, menu, plan.output(), context.getDestination());
    }

    private static ItemStack blueprint(EmiRecipe recipe) {
        for (EmiIngredient catalyst : recipe.getCatalysts()) {
            if (!catalyst.isEmpty() && !catalyst.getEmiStacks().isEmpty()) {
                return catalyst.getEmiStacks().get(0).getItemStack();
            }
        }
        return ItemStack.EMPTY;
    }

    private static boolean holds(ModWorkbenchContainer menu, ItemStack blueprint) {
        if (menu.slots.isEmpty()) {
            return false;
        }
        ItemStack held = menu.slots.get(BLUEPRINT_SLOT).getItem();
        return !held.isEmpty() && ItemStack.isSameItemSameComponents(held, blueprint);
    }

    private static Slot blueprintSlot(ModWorkbenchContainer menu, ItemStack blueprint) {
        for (Slot slot : inventory(menu)) {
            ItemStack stack = slot.getItem();
            if (!stack.isEmpty() && ItemStack.isSameItemSameComponents(stack, blueprint)) {
                return slot;
            }
        }
        return null;
    }

    private static boolean materials(ModWorkbenchContainer menu, BlueprintCraftingRecipe host) {
        List<Slot> slots = new ArrayList<>(inputs(menu));
        slots.addAll(inventory(menu));
        for (IngredientWithSize wanted : host.inputs) {
            int available = 0;
            for (Slot slot : slots) {
                ItemStack stack = slot.getItem();
                if (!stack.isEmpty() && wanted.test(stack)) {
                    available += stack.getCount();
                }
            }
            if (available < wanted.getCount()) {
                return false;
            }
        }
        return true;
    }

    private static boolean take(Minecraft client, ModWorkbenchContainer menu, Slot slot,
                                EmiCraftContext.Destination destination) {
        IESlot.BlueprintOutput output = (IESlot.BlueprintOutput) slot;
        // IE shows one page of outputs at a time and only those can be taken; the page button is the only way over.
        for (int i = 0; i < MAX_PAGES && !output.isOnPage(); i++) {
            client.gameMode.handleInventoryButtonClick(menu.containerId, 2);
        }
        if (!output.isOnPage()) {
            return false;
        }
        if (destination == EmiCraftContext.Destination.CURSOR) {
            ClickSlots.pickup(menu, slot);
        } else {
            ClickSlots.quickMove(menu, slot);
        }
        return true;
    }

    private static BlueprintCraftingRecipe host(EmiRecipe recipe) {
        return recipe instanceof BlueprintEmiRecipe page ? page.host() : null;
    }

    private static List<Slot> inputs(ModWorkbenchContainer menu) {
        List<Slot> slots = new ArrayList<>(6);
        for (Slot slot : menu.slots) {
            if (slot instanceof IESlot.BlueprintInput) {
                slots.add(slot);
            }
        }
        slots.sort(Comparator.comparingInt(Slot::getSlotIndex));
        return slots;
    }

    private static List<Slot> inventory(ModWorkbenchContainer menu) {
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return List.of();
        }
        List<Slot> slots = new ArrayList<>();
        for (Slot slot : menu.slots) {
            if (slot.container == player.getInventory()) {
                slots.add(slot);
            }
        }
        return slots;
    }

    private static Slot output(ModWorkbenchContainer menu, BlueprintCraftingRecipe host) {
        for (Slot slot : menu.slots) {
            if (slot instanceof IESlot.BlueprintOutput out && out.recipe == host) {
                return slot;
            }
        }
        return null;
    }

    private record Move(Slot from, Slot to, int count) {}

    private record Plan(List<Move> moves, Slot output) {}

    /** What has to move where, worked out on copies so a fill that cannot finish moves nothing. */
    private static Plan plan(ModWorkbenchContainer menu, BlueprintCraftingRecipe host) {
        List<Slot> inputs = inputs(menu);
        Slot output = output(menu, host);
        if (output == null || inputs.isEmpty() || host.inputs.size() > inputs.size()) {
            return null;
        }
        List<Slot> inventory = inventory(menu);
        List<ItemStack> state = new ArrayList<>(inputs.size());
        for (Slot slot : inputs) {
            state.add(slot.getItem().copy());
        }
        List<ItemStack> held = new ArrayList<>(inventory.size());
        for (Slot slot : inventory) {
            held.add(slot.getItem().copy());
        }
        List<Move> moves = new ArrayList<>();

        for (IngredientWithSize wanted : host.inputs) {
            int missing = wanted.getCount() - count(state, wanted);
            while (missing > 0) {
                int source = firstMatch(held, wanted);
                int target = targetSlot(state, wanted);
                if (target < 0 && !evict(state, held, moves, inputs, inventory, wanted)) {
                    return null;
                }
                if (source < 0) {
                    return null;
                }
                target = targetSlot(state, wanted);
                if (target < 0) {
                    return null;
                }
                ItemStack sourceStack = held.get(source);
                ItemStack targetStack = state.get(target);
                int space = inputs.get(target).getMaxStackSize() - targetStack.getCount();
                int moved = Math.min(Math.min(missing, sourceStack.getCount()), space);
                if (moved <= 0) {
                    return null;
                }
                state.set(target, targetStack.isEmpty()
                        ? sourceStack.copyWithCount(moved) : targetStack.copyWithCount(targetStack.getCount() + moved));
                held.set(source, sourceStack.getCount() == moved
                        ? ItemStack.EMPTY : sourceStack.copyWithCount(sourceStack.getCount() - moved));
                moves.add(new Move(inventory.get(source), inputs.get(target), moved));
                missing -= moved;
            }
        }
        return new Plan(List.copyOf(moves), output);
    }

    private static int count(List<ItemStack> stacks, IngredientWithSize wanted) {
        int total = 0;
        for (ItemStack stack : stacks) {
            if (!stack.isEmpty() && wanted.test(stack)) {
                total += stack.getCount();
            }
        }
        return total;
    }

    private static int firstMatch(List<ItemStack> stacks, IngredientWithSize wanted) {
        for (int i = 0; i < stacks.size(); i++) {
            ItemStack stack = stacks.get(i);
            if (!stack.isEmpty() && wanted.test(stack)) {
                return i;
            }
        }
        return -1;
    }

    /** A slot the wanted items can go into: one holding them with room, else an empty one. */
    private static int targetSlot(List<ItemStack> state, IngredientWithSize wanted) {
        int empty = -1;
        for (int i = 0; i < state.size(); i++) {
            ItemStack stack = state.get(i);
            if (stack.isEmpty()) {
                if (empty < 0) {
                    empty = i;
                }
            } else if (wanted.test(stack) && stack.getCount() < stack.getMaxStackSize()) {
                return i;
            }
        }
        return empty;
    }

    /** Frees the first input slot the recipe cannot use, so a workbench full of other materials can still be filled. */
    private static boolean evict(List<ItemStack> state, List<ItemStack> held, List<Move> moves, List<Slot> inputs,
                                 List<Slot> inventory, IngredientWithSize wanted) {
        for (int i = 0; i < state.size(); i++) {
            ItemStack stack = state.get(i);
            if (stack.isEmpty() || wanted.test(stack)) {
                continue;
            }
            for (int j = 0; j < held.size(); j++) {
                ItemStack into = held.get(j);
                if (into.isEmpty() || ItemStack.isSameItemSameComponents(into, stack)
                        && into.getCount() + stack.getCount() <= inventory.get(j).getMaxStackSize()) {
                    moves.add(new Move(inputs.get(i), inventory.get(j), 0));
                    held.set(j, into.isEmpty()
                            ? stack.copy() : into.copyWithCount(into.getCount() + stack.getCount()));
                    state.set(i, ItemStack.EMPTY);
                    return true;
                }
            }
        }
        return false;
    }
}
