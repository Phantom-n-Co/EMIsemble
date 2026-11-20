package dev.wp.emisemble.compat.chipped.transfer;

import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.handler.EmiCraftContext;
import dev.emi.emi.api.recipe.handler.StandardRecipeHandler;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.compat.chipped.ChippedCompat;
import earth.terrarium.chipped.common.menus.WorkbenchMenu;
import earth.terrarium.chipped.common.network.NetworkHandler;
import earth.terrarium.chipped.common.network.ServerboundCraftPacket;
import earth.terrarium.chipped.common.registry.ModMenuTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * The workbench holds nothing: its slots are the player's inventory, and picking a variant rewrites that stack in place.
 * So the fill is a click on that stack, and the craft is the host's own packet rather than a result-slot take.
 */
public final class WorkbenchHandler implements StandardRecipeHandler<WorkbenchMenu> {
    private WorkbenchHandler() {}

    public static int register(EmiRegistry registry) {
        try {
            registry.addRecipeHandler(ModMenuTypes.WORKBENCH.get(), new WorkbenchHandler());
            return 1;
        } catch (Throwable t) {
            Emisemble.LOGGER.warn("chipped: could not register the workbench transfer handler", t);
            return 0;
        }
    }

    @Override
    public List<Slot> getInputSources(WorkbenchMenu menu) {
        return List.copyOf(menu.slots);
    }

    // Nothing is placed anywhere: the input has to already sit in the inventory for the host to rewrite it.
    @Override
    public List<Slot> getCraftingSlots(WorkbenchMenu menu) {
        return List.of();
    }

    @Override
    public boolean supportsRecipe(EmiRecipe recipe) {
        return recipe != null && recipe.getCategory() == ChippedCompat.CATEGORY;
    }

    @Override
    public boolean canCraft(EmiRecipe recipe, EmiCraftContext<WorkbenchMenu> context) {
        EmiIngredient input = input(recipe);
        return input != null && !output(recipe).isEmpty() && source(context.getScreenHandler(), input) != null;
    }

    @Override
    public boolean craft(EmiRecipe recipe, EmiCraftContext<WorkbenchMenu> context) {
        Minecraft client = Minecraft.getInstance();
        WorkbenchMenu menu = context.getScreenHandler();
        EmiIngredient input = input(recipe);
        ItemStack output = output(recipe);
        Slot source = input == null ? null : source(menu, input);
        if (client.player == null || client.gameMode == null || source == null || output.isEmpty()) {
            return false;
        }
        // The click is what makes the server build its variant list, and the packet is checked against that list.
        client.gameMode.handleInventoryMouseClick(menu.containerId, source.index, 0, ClickType.PICKUP, client.player);
        if (context.getDestination() != EmiCraftContext.Destination.NONE) {
            NetworkHandler.CHANNEL.sendToServer(new ServerboundCraftPacket(output, context.getAmount() > 1));
        }
        return true;
    }

    private static EmiIngredient input(EmiRecipe recipe) {
        for (EmiIngredient input : recipe.getInputs()) {
            if (!input.isEmpty()) {
                return input;
            }
        }
        return null;
    }

    // A page lists one tagged set, so the variant it crafts has to be picked: EMI's page order starts at the first.
    private static ItemStack output(EmiRecipe recipe) {
        for (EmiStack output : recipe.getOutputs()) {
            ItemStack stack = output.getItemStack();
            if (!stack.isEmpty()) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    private static Slot source(WorkbenchMenu menu, EmiIngredient input) {
        for (Slot slot : menu.slots) {
            ItemStack stack = slot.getItem();
            if (stack.isEmpty()) {
                continue;
            }
            EmiStack held = EmiStack.of(stack);
            for (EmiStack candidate : input.getEmiStacks()) {
                if (!candidate.isEmpty() && candidate.isEqual(held)) {
                    return slot;
                }
            }
        }
        return null;
    }
}
