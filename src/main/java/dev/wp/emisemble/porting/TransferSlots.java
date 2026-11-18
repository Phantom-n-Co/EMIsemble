package dev.wp.emisemble.porting;

import net.minecraft.world.inventory.Slot;

import java.util.ArrayList;
import java.util.List;

/** The source list a fill reads from, in the order EMI's own handlers use. */
public final class TransferSlots {
    private TransferSlots() {}

    // EMI empties every crafting slot into the inventory before placing anything, so those slots are sources too.
    public static List<Slot> sources(List<Slot> inputs, List<Slot> inventory) {
        List<Slot> slots = new ArrayList<>(inputs.size() + inventory.size());
        slots.addAll(inputs);
        slots.addAll(inventory);
        return slots;
    }
}
