package dev.wp.emisemble.compat.mythsandlegends.stone_sanctum;

import com.ovinter.mythsandlegends.client.gui.menu.StoneSanctumMenu;
import com.ovinter.mythsandlegends.registry.MLMenus;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.handler.StandardRecipeHandler;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.porting.TransferSlots;
import net.minecraft.world.inventory.Slot;

import java.util.List;

/**
 * The ritual table: six sockets in the page's own order — the "fuel" slot 0 only accepts the cursed core — and the
 * effigy in slot 6 is what a take produces.
 */
public final class StoneSanctumHandler implements StandardRecipeHandler<StoneSanctumMenu> {
    private static final int INPUT_SLOTS = 6;
    private static final int RESULT = 6;
    private static final int INVENTORY_START = 7;
    private static final int INVENTORY_SLOTS = 36;

    private StoneSanctumHandler() {}

    public static int register(EmiRegistry registry) {
        try {
            registry.addRecipeHandler(MLMenus.STONE_SANCTUM_TABLE.get(), new StoneSanctumHandler());
            return 1;
        } catch (Throwable t) {
            Emisemble.LOGGER.warn("mythsandlegends: could not register the stone sanctum transfer handler", t);
            return 0;
        }
    }

    @Override
    public List<Slot> getInputSources(StoneSanctumMenu menu) {
        return TransferSlots.sources(getCraftingSlots(menu), menu.slots.subList(
                Math.min(INVENTORY_START, menu.slots.size()),
                Math.min(INVENTORY_START + INVENTORY_SLOTS, menu.slots.size())));
    }

    // The page lists the core then the five parts, which is the menu's own socket order, so the slots go through
    // unshuffled; slot 0 is what the host calls the fuel slot but it only accepts the cursed core.
    @Override
    public List<Slot> getCraftingSlots(StoneSanctumMenu menu) {
        return menu.slots.subList(0, Math.min(INPUT_SLOTS, menu.slots.size()));
    }

    @Override
    public Slot getOutputSlot(StoneSanctumMenu menu) {
        return menu.slots.size() > RESULT ? menu.slots.get(RESULT) : null;
    }

    @Override
    public boolean supportsRecipe(EmiRecipe recipe) {
        return recipe.getCategory() == StoneSanctumCategory.CATEGORY;
    }
}
