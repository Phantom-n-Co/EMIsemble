package dev.wp.emisemble.compat.enigmaticlegacyplus.spellstone;

import auviotre.enigmatic.legacy.contents.crafting.SpellstoneTableRecipe;
import auviotre.enigmatic.legacy.contents.gui.SpellstoneTableMenu;
import auviotre.enigmatic.legacy.registries.EnigmaticBlocks;
import auviotre.enigmatic.legacy.registries.EnigmaticItems;
import auviotre.enigmatic.legacy.registries.EnigmaticMenus;
import auviotre.enigmatic.legacy.registries.EnigmaticRecipes;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.handler.StandardRecipeHandler;
import dev.emi.emi.api.stack.EmiStack;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.porting.EmiRecipes;
import dev.wp.emisemble.porting.TranslatedCategory;
import dev.wp.emisemble.porting.TransferSlots;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.List;

/** {@code enigmaticlegacyplus:spellstone_crafting}. */
public final class SpellstoneCompat {
    public static final EmiRecipeCategory SPELLSTONE_CRAFTING = new TranslatedCategory(
            EmiRecipes.categoryId(EnigmaticRecipes.SPELLSTONE_CRAFTING.get()),
            EmiStack.of(EnigmaticItems.SPELLCORE.get()), "gui.enigmaticlegacy.jei.spellstone_crafting");

    public static final ResourceLocation FRAGMENTATION_ID = ResourceLocation.fromNamespaceAndPath(
            SPELLSTONE_CRAFTING.getId().getNamespace(), "/spellstone_fragmentation");

    private static final int DEBRIS_FROM_SPELLSTONE = 4;

    private SpellstoneCompat() {}

    public static int register(EmiRegistry registry) {
        registry.addCategory(SPELLSTONE_CRAFTING);
        registry.addWorkstation(SPELLSTONE_CRAFTING, EmiStack.of(EnigmaticBlocks.SPELLSTONE_TABLE.toStack()));

        RecipeType<SpellstoneTableRecipe> type = EnigmaticRecipes.SPELLSTONE_CRAFTING.get();
        int recipes = EmiRecipes.addAll(registry, type,
                holder -> new SpellstoneEmiRecipe(SPELLSTONE_CRAFTING, holder));
        int added = recipes + (addFragmentationPage(registry) ? 1 : 0);

        try {
            registry.addRecipeHandler(EnigmaticMenus.SPELLSTONE_TABLE_MENU.get(), new Handler());
        } catch (Throwable t) {
            Emisemble.LOGGER.warn("enigmaticlegacyplus: could not register the spellstone transfer handler", t);
        }

        Emisemble.LOGGER.info("enigmaticlegacyplus: {} spellstone_crafting recipes registered", added);
        return added;
    }

    /** The page the host's {@code JEIHandler} appends to the json recipes; nothing describes it. */
    private static boolean addFragmentationPage(EmiRegistry registry) {
        try {
            SpellstoneTableRecipe recipe = new SpellstoneTableRecipe(
                    EnigmaticItems.SPELLSTONE_DEBRIS.toStack(DEBRIS_FROM_SPELLSTONE), 0, NonNullList.create(), false);
            return EmiRecipes.add(registry, new SpellstoneEmiRecipe(SPELLSTONE_CRAFTING, FRAGMENTATION_ID, recipe));
        } catch (Throwable t) {
            Emisemble.LOGGER.warn("Skipping enigmaticlegacyplus spellstone_fragmentation page", t);
            return false;
        }
    }

    /** JEI's {@code addRecipeTransferHandler(SpellstoneTableMenu, …, 0, 10, 11, 36)} in EMI's shape. */
    private static final class Handler implements StandardRecipeHandler<SpellstoneTableMenu> {
        private static final int CRAFT_SLOTS = 10;
        private static final int INVENTORY_START = 11;
        private static final int INVENTORY_SLOTS = 36;

        @Override
        public List<Slot> getInputSources(SpellstoneTableMenu menu) {
            // The table's cells first, as EMI's filler empties them into the inventory before it fills.
            return TransferSlots.sources(getCraftingSlots(menu), menu.slots.subList(
                    Math.min(INVENTORY_START, menu.slots.size()),
                    Math.min(INVENTORY_START + INVENTORY_SLOTS, menu.slots.size())));
        }

        @Override
        public List<Slot> getCraftingSlots(SpellstoneTableMenu menu) {
            return menu.slots.subList(0, CRAFT_SLOTS);
        }

        @Override
        public boolean supportsRecipe(EmiRecipe recipe) {
            return recipe.getCategory() == SPELLSTONE_CRAFTING;
        }
    }
}
