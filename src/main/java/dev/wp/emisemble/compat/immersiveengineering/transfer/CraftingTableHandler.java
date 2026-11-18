package dev.wp.emisemble.compat.immersiveengineering.transfer;

import blusunrize.immersiveengineering.common.gui.CraftingTableMenu;
import blusunrize.immersiveengineering.common.register.IEMenuTypes;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.recipe.handler.StandardRecipeHandler;
import dev.wp.emisemble.Emisemble;
import net.minecraft.world.inventory.Slot;

import java.util.List;

/**
 * The engineer's crafting table is a plain 3x3 grid in a menu of its own, so EMI has no handler for it and the fill
 * button never appears. The host registers exactly this transfer handler with JEI, at slots 1..9 with the result at 0.
 */
public final class CraftingTableHandler implements StandardRecipeHandler<CraftingTableMenu> {
    private static final int GRID_START = 1;
    private static final int GRID_END = 10;
    private static final int RESULT = 0;

    private CraftingTableHandler() {}

    public static int register(EmiRegistry registry) {
        try {
            registry.addRecipeHandler(IEMenuTypes.CRAFTING_TABLE.getType(), new CraftingTableHandler());
            return 1;
        } catch (Throwable t) {
            Emisemble.LOGGER.warn("immersiveengineering: could not register the crafting table transfer handler", t);
            return 0;
        }
    }

    // Grid and inventory alike, as EMI's own crafting handler does: an input already in the grid is a valid source.
    @Override
    public List<Slot> getInputSources(CraftingTableMenu menu) {
        return menu.slots.size() > GRID_END ? menu.slots.subList(GRID_START, menu.slots.size()) : List.of();
    }

    @Override
    public List<Slot> getCraftingSlots(CraftingTableMenu menu) {
        return menu.slots.size() > GRID_END ? menu.slots.subList(GRID_START, GRID_END) : List.of();
    }

    @Override
    public Slot getOutputSlot(CraftingTableMenu menu) {
        return menu.slots.isEmpty() ? null : menu.slots.get(RESULT);
    }

    @Override
    public boolean supportsRecipe(EmiRecipe recipe) {
        return recipe.getCategory() == VanillaEmiRecipeCategories.CRAFTING && recipe.supportsRecipeTree();
    }
}
