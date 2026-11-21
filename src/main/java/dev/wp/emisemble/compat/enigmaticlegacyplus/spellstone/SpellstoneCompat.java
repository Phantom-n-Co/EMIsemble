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

import java.util.ArrayList;
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
        private static final int DEBRIS = 0;
        private static final int SPELLSTONE = 1;
        private static final int CORE = 2;
        private static final int RING_START = 3;
        private static final int RESULT = 10;
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
        public List<Slot> getCraftingSlots(EmiRecipe recipe, SpellstoneTableMenu menu) {
            if (!(recipe instanceof SpellstoneEmiRecipe page)) {
                return getCraftingSlots(menu);
            }
            // EMI puts input i in craftingSlots[i] and the page orders its inputs as ring, debris, then core or
            // spellstones, which is neither the menu's order nor even the same length on every page.
            List<Slot> slots = new ArrayList<>(page.ringCount() + 2);
            for (int i = 0; i < page.ringCount(); i++) {
                slots.add(menu.slots.get(RING_START + i));
            }
            if (page.hasDebris()) {
                slots.add(menu.slots.get(DEBRIS));
            }
            slots.add(menu.slots.get(page.fragmentation() ? SPELLSTONE : CORE));
            return slots;
        }

        @Override
        public Slot getOutputSlot(SpellstoneTableMenu menu) {
            return menu.slots.get(RESULT);
        }

        @Override
        public boolean supportsRecipe(EmiRecipe recipe) {
            return recipe.getCategory() == SPELLSTONE_CRAFTING;
        }
    }
}
