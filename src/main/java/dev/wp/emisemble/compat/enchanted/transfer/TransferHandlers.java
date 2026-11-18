package dev.wp.emisemble.compat.enchanted.transfer;

import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.handler.StandardRecipeHandler;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.compat.enchanted.byproduct.ByproductCategory;
import dev.wp.emisemble.compat.enchanted.distilling.DistillingCategory;
import dev.wp.emisemble.compat.enchanted.spinning.SpinningCategory;
import net.favouriteless.enchanted.common.init.EMenuTypes;
import net.favouriteless.enchanted.common.menus.DistilleryMenu;
import net.favouriteless.enchanted.common.menus.SpinningWheelMenu;
import net.favouriteless.enchanted.common.menus.WitchOvenMenu;
import net.minecraft.world.inventory.Slot;

import java.util.List;

/** Shift-click from the viewer into the three machines that carry a transfer handler in JEI. */
public final class TransferHandlers {
    private TransferHandlers() {}

    public static void register(EmiRegistry registry) {
        registry.addRecipeHandler(EMenuTypes.WITCH_OVEN.get(), new WitchOvenHandler());
        registry.addRecipeHandler(EMenuTypes.SPINNING_WHEEL.get(), new SpinningWheelHandler());
        registry.addRecipeHandler(EMenuTypes.DISTILLERY.get(), new DistilleryHandler());

        Emisemble.LOGGER.info("enchanted: 3 recipe transfer handlers registered");
    }

    private static final class WitchOvenHandler implements StandardRecipeHandler<WitchOvenMenu> {
        @Override
        public List<Slot> getInputSources(WitchOvenMenu menu) {
            return menu.slots.subList(5, menu.slots.size());
        }

        @Override
        public List<Slot> getCraftingSlots(WitchOvenMenu menu) {
            return menu.slots.subList(0, 1);
        }

        @Override
        public boolean supportsRecipe(EmiRecipe recipe) {
            return recipe.getCategory() == ByproductCategory.CATEGORY;
        }
    }

    private static final class SpinningWheelHandler implements StandardRecipeHandler<SpinningWheelMenu> {
        @Override
        public List<Slot> getInputSources(SpinningWheelMenu menu) {
            return menu.slots.subList(4, menu.slots.size());
        }

        @Override
        public List<Slot> getCraftingSlots(SpinningWheelMenu menu) {
            return menu.slots.subList(0, 3);
        }

        @Override
        public boolean supportsRecipe(EmiRecipe recipe) {
            return recipe.getCategory() == SpinningCategory.CATEGORY;
        }
    }

    private static final class DistilleryHandler implements StandardRecipeHandler<DistilleryMenu> {
        // JEI's handler starts the inventory at slot 5, which is one of the distillery's own output slots; 7 is the player's.
        @Override
        public List<Slot> getInputSources(DistilleryMenu menu) {
            return menu.slots.subList(7, menu.slots.size());
        }

        @Override
        public List<Slot> getCraftingSlots(DistilleryMenu menu) {
            return menu.slots.subList(0, 3);
        }

        @Override
        public boolean supportsRecipe(EmiRecipe recipe) {
            return recipe.getCategory() == DistillingCategory.CATEGORY;
        }
    }
}
