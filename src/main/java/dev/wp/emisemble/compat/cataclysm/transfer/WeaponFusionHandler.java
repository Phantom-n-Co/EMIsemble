package dev.wp.emisemble.compat.cataclysm.transfer;

import com.github.L_Ender.cataclysm.init.ModMenu;
import com.github.L_Ender.cataclysm.inventory.WeaponfusionMenu;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.handler.StandardRecipeHandler;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.compat.cataclysm.weapon_infusion.WeaponInfusionCategory;
import net.minecraft.world.inventory.Slot;

import java.util.List;

/** Shift-click from the viewer into the mechanical fusion anvil, the one machine JEI adds a handler for. */
public final class WeaponFusionHandler {
    private WeaponFusionHandler() {}

    public static void register(EmiRegistry registry) {
        registry.addRecipeHandler(ModMenu.WEAPON_FUSION.get(), new Handler());

        Emisemble.LOGGER.info("cataclysm: 1 recipe transfer handler registered");
    }

    private static final class Handler implements StandardRecipeHandler<WeaponfusionMenu> {
        // JEI's slots 0-5 with the inventory from 9 take the anvil's result as an input and orphan six player slots.
        @Override
        public List<Slot> getInputSources(WeaponfusionMenu menu) {
            return menu.slots.subList(3, menu.slots.size());
        }

        @Override
        public List<Slot> getCraftingSlots(WeaponfusionMenu menu) {
            return menu.slots.subList(0, 2);
        }

        @Override
        public boolean supportsRecipe(EmiRecipe recipe) {
            return recipe.getCategory() == WeaponInfusionCategory.CATEGORY;
        }
    }
}
