package dev.wp.emisemble.compat.appliedthaumaturge;

import com.traverse.appliedthaumaturge.menu.ArcaneInscriberMenu;
import com.traverse.appliedthaumaturge.menu.ArcaneTermMenu;
import com.traverse.appliedthaumaturge.network.ArcaneTransferPacket;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.recipe.handler.EmiCraftContext;
import dev.emi.emi.api.recipe.handler.StandardRecipeHandler;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Widget;
import dev.wp.emisemble.compat.thaumaturge.workbench.ArcaneWorkbenchCompat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

/**
 * Both menus assemble on the server out of ME storage, so the fill is the host's own candidate packet rather than a
 * slot-by-slot move: the grid the player sees is only a mirror of the terminal's inventory.
 */
final class ArcaneTransferHandler<M extends AbstractContainerMenu> implements StandardRecipeHandler<M> {
    private static final int MAX_CANDIDATES = 32;
    private static final int ARCANE_INPUTS = 15;
    private static final int CRAFTING_INPUTS = 9;

    private final boolean crafting;

    private ArcaneTransferHandler(boolean crafting) {
        this.crafting = crafting;
    }

    static ArcaneTransferHandler<ArcaneTermMenu> terminal() {
        return new ArcaneTransferHandler<>(true);
    }

    static ArcaneTransferHandler<ArcaneInscriberMenu> inscriber() {
        return new ArcaneTransferHandler<>(false);
    }

    @Override
    public List<Slot> getInputSources(M menu) {
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return List.of();
        }
        // Only the player's own slots: the network is not a slot list, so it cannot be modelled as a source here.
        Inventory inventory = player.getInventory();
        return menu.slots.stream().filter(slot -> slot.container == inventory).toList();
    }

    @Override
    public List<Slot> getCraftingSlots(M menu) {
        // Empty on purpose: EMI moves items slot to slot, and the server fills the grid from the network instead.
        return List.of();
    }

    @Override
    public boolean supportsRecipe(EmiRecipe recipe) {
        if (recipe == null) {
            return false;
        }
        if (recipe.getCategory() == ArcaneWorkbenchCompat.CATEGORY) {
            return true;
        }
        EmiRecipeCategory vanilla = VanillaEmiRecipeCategories.CRAFTING;
        return crafting && vanilla != null && recipe.getCategory() == vanilla;
    }

    @Override
    public boolean canCraft(EmiRecipe recipe, EmiCraftContext<M> context) {
        // As in JEI: the transfer is offered unconditionally, the server decides whether storage can pay for it.
        return candidates(recipe) != null;
    }

    @Override
    public boolean craft(EmiRecipe recipe, EmiCraftContext<M> context) {
        List<List<ItemStack>> slots = candidates(recipe);
        if (slots == null) {
            return false;
        }
        PacketDistributor.sendToServer(new ArcaneTransferPacket(context.getScreenHandler().containerId, slots));
        return true;
    }

    @Override
    public void render(EmiRecipe recipe, EmiCraftContext<M> context, List<Widget> widgets, GuiGraphics draw) {
        // Shading what the player's inventory lacks would be wrong here: the terminal pays out of the network.
    }

    private List<List<ItemStack>> candidates(EmiRecipe recipe) {
        if (!supportsRecipe(recipe)) {
            return null;
        }
        boolean arcane = recipe.getCategory() == ArcaneWorkbenchCompat.CATEGORY;
        // An arcane page lists only the slots that hold something, and the fill is positional: spread them back out.
        List<EmiIngredient> inputs = arcane ? ArcaneWorkbenchCompat.slotInputs(recipe) : recipe.getInputs();
        if (inputs == null || inputs.isEmpty()) {
            return null;
        }
        int count = Math.min(inputs.size(), arcane ? ARCANE_INPUTS : CRAFTING_INPUTS);
        List<List<ItemStack>> slots = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            EmiIngredient input = inputs.get(i);
            slots.add(input.isEmpty() ? List.of() : input.getEmiStacks().stream()
                    .limit(MAX_CANDIDATES)
                    .map(EmiStack::getItemStack)
                    .filter(stack -> !stack.isEmpty())
                    .toList());
        }
        return slots;
    }
}
