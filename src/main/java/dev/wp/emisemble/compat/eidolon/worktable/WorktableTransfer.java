package dev.wp.emisemble.compat.eidolon.worktable;

import alexthw.eidolon_repraised.gui.WorktableContainer;
import alexthw.eidolon_repraised.registries.Registry;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.recipe.handler.StandardRecipeHandler;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.porting.TransferSlots;
import net.minecraft.world.inventory.Slot;

import java.util.List;

/** Shift-click into the magic workbench: the 13 machine sockets, and vanilla crafting for its core grid. */
public final class WorktableTransfer {
    private static final int CORE_FIRST = 1;
    private static final int SOCKETS_END = 14;
    private static final int CORE_END = 10;

    private WorktableTransfer() {}

    public static void register(EmiRegistry registry) {
        registry.addRecipeHandler(Registry.WORKTABLE_CONTAINER.get(), new WorktableHandler());
        registry.addRecipeHandler(Registry.WORKTABLE_CONTAINER.get(), new CraftingHandler());

        Emisemble.LOGGER.info("eidolon_repraised: 2 worktable transfer handlers registered");
    }

    private static final class WorktableHandler implements StandardRecipeHandler<WorktableContainer> {
        @Override
        public List<Slot> getInputSources(WorktableContainer menu) {
            return TransferSlots.sources(menu.slots.subList(CORE_FIRST, SOCKETS_END),
                    menu.slots.subList(SOCKETS_END, menu.slots.size()));
        }

        @Override
        public List<Slot> getCraftingSlots(WorktableContainer menu) {
            return menu.slots.subList(CORE_FIRST, SOCKETS_END);
        }

        @Override
        public Slot getOutputSlot(WorktableContainer menu) {
            return menu.getSlot(0);
        }

        @Override
        public boolean supportsRecipe(EmiRecipe recipe) {
            return recipe.getCategory() == WorktableCategory.CATEGORY;
        }
    }

    private static final class CraftingHandler implements StandardRecipeHandler<WorktableContainer> {
        @Override
        public List<Slot> getInputSources(WorktableContainer menu) {
            return TransferSlots.sources(menu.slots.subList(CORE_FIRST, CORE_END),
                    menu.slots.subList(SOCKETS_END, menu.slots.size()));
        }

        @Override
        public List<Slot> getCraftingSlots(WorktableContainer menu) {
            return menu.slots.subList(CORE_FIRST, CORE_END);
        }

        @Override
        public Slot getOutputSlot(WorktableContainer menu) {
            return menu.getSlot(0);
        }

        @Override
        public boolean supportsRecipe(EmiRecipe recipe) {
            return recipe.getCategory() == VanillaEmiRecipeCategories.CRAFTING;
        }
    }
}
