package dev.wp.emisemble.compat.hexerei.cauldron;

import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.handler.EmiCraftContext;
import dev.emi.emi.api.recipe.handler.StandardRecipeHandler;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.wp.emisemble.Emisemble;
import net.joefoxe.hexerei.container.MixingCauldronContainer;
import net.joefoxe.hexerei.container.ModContainers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class CauldronTransfer implements StandardRecipeHandler<MixingCauldronContainer> {
        private static final int PLAYER_SLOTS = 36;
        private static final int SOCKET_START = 37;
    private static final int SOCKET_COUNT = 8;

    public static void register(EmiRegistry registry) {
        try {
            registry.addRecipeHandler(ModContainers.MIXING_CAULDRON_CONTAINER.get(), new CauldronTransfer());
        } catch (Throwable t) {
            Emisemble.LOGGER.warn("hexerei: could not register the cauldron transfer handler", t);
        }
    }

    @Override
    public List<Slot> getInputSources(MixingCauldronContainer menu) {
        return menu.slots.subList(0, PLAYER_SLOTS);
    }

    @Override
    public List<Slot> getCraftingSlots(MixingCauldronContainer menu) {
        return menu.slots.subList(SOCKET_START, SOCKET_START + SOCKET_COUNT);
    }

    @Override
    public boolean supportsRecipe(EmiRecipe recipe) {
        return recipe.getCategory() == MixingCauldronCategory.CATEGORY;
    }

    @Override
    public boolean canCraft(EmiRecipe recipe, EmiCraftContext<MixingCauldronContainer> context) {
        try {
            return plan(recipe, context.getScreenHandler()) != null;
        } catch (Throwable t) {
            return false;
        }
    }

    @Override
    public boolean craft(EmiRecipe recipe, EmiCraftContext<MixingCauldronContainer> context) {
        MixingCauldronContainer menu = context.getScreenHandler();
        MultiPlayerGameMode mode = Minecraft.getInstance().gameMode;
        Player player = Minecraft.getInstance().player;
        if (mode == null || player == null || !menu.getCarried().isEmpty()) {
            return false;
        }

        Plan plan;
        try {
            plan = plan(recipe, menu);
        } catch (Throwable t) {
            return false;
        }
        if (plan == null) {
            return false;
        }

        List<Slot> sockets = sockets(menu);
        boolean moved = false;
        for (Slot leftover : plan.leftovers()) {
            mode.handleInventoryMouseClick(menu.containerId, leftover.index, 0, ClickType.QUICK_MOVE, player);
            moved = true;
        }
        for (int i = 0; i < plan.pending().size(); i++) {
            Slot socket = firstEmpty(sockets);
            if (socket == null) {
                break;
            }
            placeOne(mode, menu, plan.sources().get(i), socket, player);
            moved = true;
        }
        return moved;
    }

        private static Plan plan(EmiRecipe recipe, MixingCauldronContainer menu) {
        List<EmiIngredient> wanted = itemInputs(recipe);
        if (wanted.isEmpty()) {
            return null;
        }

        List<Slot> sockets = sockets(menu);
        List<Slot> leftovers = new ArrayList<>();
        for (Slot socket : sockets) {
            ItemStack stack = socket.getItem();
            if (stack.isEmpty()) {
                continue;
            }
            int match = indexOfMatch(wanted, stack);
            if (match >= 0) {
                wanted.remove(match);
            } else {
                leftovers.add(socket);
            }
        }

        List<Slot> sources = new ArrayList<>();
        List<Slot> playerSlots = menu.slots.subList(0, PLAYER_SLOTS);
        int[] remaining = new int[playerSlots.size()];
        for (int i = 0; i < playerSlots.size(); i++) {
            remaining[i] = playerSlots.get(i).getItem().getCount();
        }
        for (EmiIngredient input : wanted) {
            Slot source = take(playerSlots, remaining, input);
            if (source == null) {
                return null;
            }
            sources.add(source);
        }
        return new Plan(List.copyOf(wanted), List.copyOf(sources), List.copyOf(leftovers));
    }

        private static List<EmiIngredient> itemInputs(EmiRecipe recipe) {
        List<EmiIngredient> items = new ArrayList<>();
        for (EmiIngredient input : recipe.getInputs()) {
            if (input.isEmpty() || !isItem(input)) {
                continue;
            }
            items.add(input);
        }
        return items;
    }

    private static boolean isItem(EmiIngredient input) {
        for (EmiStack stack : input.getEmiStacks()) {
            if (!stack.getItemStack().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private static int indexOfMatch(List<EmiIngredient> ingredients, ItemStack stack) {
        for (int i = 0; i < ingredients.size(); i++) {
            if (matches(ingredients.get(i), stack)) {
                return i;
            }
        }
        return -1;
    }

    private static boolean matches(EmiIngredient input, ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        EmiStack item = EmiStack.of(stack);
        for (EmiStack candidate : input.getEmiStacks()) {
            if (!candidate.isEmpty() && candidate.isEqual(item)) {
                return true;
            }
        }
        return false;
    }

        private static Slot take(List<Slot> playerSlots, int[] remaining, EmiIngredient input) {
        for (int i = 0; i < playerSlots.size(); i++) {
            if (remaining[i] <= 0) {
                continue;
            }
            if (matches(input, playerSlots.get(i).getItem())) {
                remaining[i]--;
                return playerSlots.get(i);
            }
        }
        return null;
    }

        private static void placeOne(MultiPlayerGameMode mode, MixingCauldronContainer menu, Slot source, Slot socket,
            Player player) {
        int count = source.getItem().getCount();
        mode.handleInventoryMouseClick(menu.containerId, source.index, 0, ClickType.PICKUP, player);
        if (count <= 1) {
            mode.handleInventoryMouseClick(menu.containerId, socket.index, 0, ClickType.PICKUP, player);
        } else {
            mode.handleInventoryMouseClick(menu.containerId, socket.index, 1, ClickType.PICKUP, player);
            mode.handleInventoryMouseClick(menu.containerId, source.index, 0, ClickType.PICKUP, player);
        }
    }

    private static List<Slot> sockets(MixingCauldronContainer menu) {
        return menu.slots.subList(SOCKET_START, SOCKET_START + SOCKET_COUNT);
    }

    private static Slot firstEmpty(List<Slot> slots) {
        for (Slot slot : slots) {
            if (slot.getItem().isEmpty()) {
                return slot;
            }
        }
        return null;
    }

    private record Plan(List<EmiIngredient> pending, List<Slot> sources, List<Slot> leftovers) {}
}
