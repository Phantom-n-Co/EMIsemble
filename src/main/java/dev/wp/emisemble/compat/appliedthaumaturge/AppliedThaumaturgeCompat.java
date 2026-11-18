package dev.wp.emisemble.compat.appliedthaumaturge;

import com.traverse.appliedthaumaturge.menu.ArcaneTermMenu;
import com.traverse.appliedthaumaturge.registry.ATItems;
import com.traverse.appliedthaumaturge.registry.ATMenus;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.recipe.handler.EmiRecipeHandler;
import dev.emi.emi.api.stack.EmiStack;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.compat.thaumaturge.workbench.ArcaneWorkbenchCompat;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;

/** Applied Thaumaturge ships no category of its own: its terminals only reach the arcane and crafting pages. */
public final class AppliedThaumaturgeCompat {
    private AppliedThaumaturgeCompat() {}

    public static void register(EmiRegistry registry) {
        // The arcane page is Thaumaturge's own: with that host disabled the catalysts would point at nothing.
        if (!Emisemble.isAvailable("thaumaturge")) {
            return;
        }
        EmiRecipeCategory arcane = ArcaneWorkbenchCompat.CATEGORY;
        workstation(registry, arcane, ATItems.ARCANE_TERMINAL.get());
        workstation(registry, arcane, ATItems.ARCANE_INSCRIBER.get());
        workstation(registry, arcane, ATItems.WIRELESS_ARCANE_TERMINAL.get());

        EmiRecipeCategory crafting = VanillaEmiRecipeCategories.CRAFTING;
        if (crafting != null) {
            workstation(registry, crafting, ATItems.ARCANE_TERMINAL.get());
            workstation(registry, crafting, ATItems.WIRELESS_ARCANE_TERMINAL.get());
        }

        // Three registrations in the host, two distinct handlers: the wireless terminal shares the wired one's menu.
        ArcaneTransferHandler<ArcaneTermMenu> terminal = ArcaneTransferHandler.terminal();
        handler(registry, ATMenus.ARCANE_TERMINAL, terminal);
        handler(registry, ATMenus.WIRELESS_ARCANE_TERMINAL, terminal);
        handler(registry, ATMenus.ARCANE_INSCRIBER, ArcaneTransferHandler.inscriber());
        Emisemble.LOGGER.info("appliedthaumaturge: arcane catalysts and transfer handlers registered");
    }

    private static void workstation(EmiRegistry registry, EmiRecipeCategory category, Item item) {
        try {
            registry.addWorkstation(category, EmiStack.of(item));
        } catch (Throwable t) {
            Emisemble.LOGGER.warn("appliedthaumaturge: could not add {} as a workstation",
                    BuiltInRegistries.ITEM.getKey(item), t);
        }
    }

    private static <M extends AbstractContainerMenu> void handler(
            EmiRegistry registry, MenuType<M> type, EmiRecipeHandler<M> handler) {
        try {
            registry.addRecipeHandler(type, handler);
        } catch (Throwable t) {
            Emisemble.LOGGER.warn("appliedthaumaturge: could not register a transfer handler for {}", type, t);
        }
    }
}
