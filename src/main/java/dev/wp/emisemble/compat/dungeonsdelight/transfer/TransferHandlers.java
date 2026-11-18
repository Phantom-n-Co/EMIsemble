package dev.wp.emisemble.compat.dungeonsdelight.transfer;

import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.handler.StandardRecipeHandler;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.compat.dungeonsdelight.monster_cooking.MonsterCookingCategory;
import dev.wp.emisemble.porting.TransferSlots;
import net.minecraft.world.inventory.Slot;
import net.yirmiri.dungeonsdelight.common.block.monster_pot.MonsterPotMenu;
import net.yirmiri.dungeonsdelight.core.registry.DDMenuTypes;

import java.util.List;

/** Shift-click from the monster cooking page into the monster pot. */
public final class TransferHandlers {
    private TransferHandlers() {}

    public static void register(EmiRegistry registry) {
        registry.addRecipeHandler(DDMenuTypes.MONSTER_POT.get(), new MonsterPotHandler());

        Emisemble.LOGGER.info("dungeonsdelight: 1 recipe transfer handler registered");
    }

    private static final class MonsterPotHandler implements StandardRecipeHandler<MonsterPotMenu> {
        // JEI moves into slots 0-5, the six inputs, and sources from 9-44, the player's inventory and hotbar.
        @Override
        public List<Slot> getInputSources(MonsterPotMenu menu) {
            // The pot's own six slots first: EMI empties them into the inventory before it fills.
            return TransferSlots.sources(menu.slots.subList(0, 6), menu.slots.subList(9, 45));
        }

        @Override
        public List<Slot> getCraftingSlots(MonsterPotMenu menu) {
            return menu.slots.subList(0, 6);
        }

        @Override
        public boolean supportsRecipe(EmiRecipe recipe) {
            return recipe.getCategory() == MonsterCookingCategory.CATEGORY;
        }
    }
}
