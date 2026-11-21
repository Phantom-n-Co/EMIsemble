package dev.wp.emisemble.compat.immersiveengineering.transfer;

import blusunrize.immersiveengineering.common.gui.IESlot;
import blusunrize.immersiveengineering.common.gui.ModWorkbenchContainer;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

final class WorkbenchCraft {
    // EmiCraftContext.Destination's ordinals; the server side has no viewer to import.
    private static final int CURSOR = 1;
    private static final int NEXT_PAGE = 2;
    private static final int MAX_BATCHES = 64;

    private WorkbenchCraft() {}

    static void apply(ServerPlayer player, WorkbenchCraftPayload payload) {
        if (!(player.containerMenu instanceof ModWorkbenchContainer menu) || menu.containerId != payload.containerId()
                || !menu.stillValid(player) || payload.slot() < 0 || payload.slot() >= menu.slots.size()
                || !(menu.slots.get(payload.slot()) instanceof IESlot.BlueprintOutput output)) {
            return;
        }
        boolean cursor = payload.destination() == CURSOR;
        if (!onPage(menu, output, player) || cursor && !menu.getCarried().isEmpty()
                && !ItemStack.isSameItemSameComponents(menu.getCarried(), output.getItem())) {
            return;
        }
        int batches = Math.min(Math.min(payload.amount(), MAX_BATCHES), output.recipe.getMaxCrafted(inputs(menu)));
        for (int i = 0; i < batches; i++) {
            if (output.getItem().isEmpty()) {
                return;
            }
            int before = total(inputs(menu));
            menu.clicked(output.index, 0, ClickType.PICKUP, player);
            if (!cursor) {
                stow(menu, player);
            }
            if (total(inputs(menu)) >= before) {
                break;
            }
        }
        menu.broadcastChanges();
    }

    private static boolean onPage(ModWorkbenchContainer menu, IESlot.BlueprintOutput output, ServerPlayer player) {
        for (int i = 0; i < menu.slots.size() && !menu.isOutputSlotOnPage(output); i++) {
            menu.clickMenuButton(player, NEXT_PAGE);
        }
        return menu.isOutputSlotOnPage(output);
    }

    private static void stow(ModWorkbenchContainer menu, ServerPlayer player) {
        ItemStack carried = menu.getCarried();
        if (!carried.isEmpty()) {
            menu.setCarried(ItemStack.EMPTY);
            player.getInventory().placeItemBackInInventory(carried);
        }
    }

    private static NonNullList<ItemStack> inputs(ModWorkbenchContainer menu) {
        NonNullList<ItemStack> stacks = NonNullList.create();
        for (Slot slot : menu.slots) {
            if (slot instanceof IESlot.BlueprintInput) {
                stacks.add(slot.getItem());
            }
        }
        return stacks;
    }

    private static int total(NonNullList<ItemStack> stacks) {
        int total = 0;
        for (ItemStack stack : stacks) {
            total += stack.getCount();
        }
        return total;
    }
}
