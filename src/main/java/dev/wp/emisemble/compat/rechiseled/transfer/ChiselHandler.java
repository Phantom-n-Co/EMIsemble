package dev.wp.emisemble.compat.rechiseled.transfer;

import com.supermartijn642.rechiseled.Rechiseled;
import com.supermartijn642.rechiseled.screen.ChiselContainer;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.handler.StandardRecipeHandler;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.compat.rechiseled.RechiseledCompat;
import dev.wp.emisemble.porting.TransferSlots;
import net.minecraft.world.inventory.Slot;

import java.util.List;

/**
 * The chisel has no result slot: slot 0 is the block stored on the chisel item itself and is consumed by the entry
 * the player picks from the variant list, so a fill only loads that socket.
 */
public final class ChiselHandler implements StandardRecipeHandler<ChiselContainer> {
    private static final int INPUT = 0;
    private static final int INVENTORY_START = 1;
    private static final int INVENTORY_SLOTS = 36;

    private ChiselHandler() {}

    public static int register(EmiRegistry registry) {
        try {
            registry.addRecipeHandler(Rechiseled.chisel_container, new ChiselHandler());
            return 1;
        } catch (Throwable t) {
            Emisemble.LOGGER.warn("rechiseled: could not register the chisel transfer handler", t);
            return 0;
        }
    }

    @Override
    public List<Slot> getInputSources(ChiselContainer menu) {
        return TransferSlots.sources(getCraftingSlots(menu), menu.slots.subList(
                Math.min(INVENTORY_START, menu.slots.size()),
                Math.min(INVENTORY_START + INVENTORY_SLOTS, menu.slots.size())));
    }

    @Override
    public List<Slot> getCraftingSlots(ChiselContainer menu) {
        return List.of(menu.slots.get(INPUT));
    }

    @Override
    public boolean supportsRecipe(EmiRecipe recipe) {
        return recipe.getCategory() == RechiseledCompat.CATEGORY;
    }
}
