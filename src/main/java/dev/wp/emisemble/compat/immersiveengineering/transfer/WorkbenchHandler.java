package dev.wp.emisemble.compat.immersiveengineering.transfer;

import blusunrize.immersiveengineering.api.crafting.BlueprintCraftingRecipe;
import blusunrize.immersiveengineering.common.gui.IESlot;
import blusunrize.immersiveengineering.common.gui.ModWorkbenchContainer;
import blusunrize.immersiveengineering.common.register.IEMenuTypes;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.handler.EmiCraftContext;
import dev.emi.emi.api.recipe.handler.StandardRecipeHandler;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.compat.immersiveengineering.blueprint.BlueprintCategory;
import dev.wp.emisemble.compat.immersiveengineering.blueprint.BlueprintEmiRecipe;
import dev.wp.emisemble.porting.ClickSlots;
import dev.wp.emisemble.porting.ClientTicks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.registration.NetworkRegistry;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * The Engineer's Workbench crafts blueprint recipes out of six material slots, and IE gives JEI no transfer for it. This
 * fills those slots and then takes the page's output slot, whose take is what spends the materials.
 */
public final class WorkbenchHandler implements StandardRecipeHandler<ModWorkbenchContainer> {
    private static final int BLUEPRINT_SLOT = 0;
    // IE's ModWorkbenchContainer.OUTPUTS_PER_PAGE; the page buttons are button id 1 (back) and 2 (forward).
    private static final int OUTPUTS_PER_PAGE = 9;
    private static final int NEXT_PAGE = 2;
    private static final int REBIND_TICKS = 40;
    private static final int MAX_BATCHES = 64;
    private static final WorkbenchHandler INSTANCE = new WorkbenchHandler();

    private WorkbenchHandler() {}

    public static int register(EmiRegistry registry) {
        try {
            registry.addRecipeHandler(IEMenuTypes.MOD_WORKBENCH.getType(), INSTANCE);
            return 1;
        } catch (Throwable t) {
            Emisemble.LOGGER.warn("immersiveengineering: could not register the workbench transfer handler", t);
            return 0;
        }
    }

    // The blueprint in slot 0 is what builds the six input slots, so a workbench without one has nothing to fill.
    @Override
    public List<Slot> getInputSources(ModWorkbenchContainer menu) {
        List<Slot> slots = new ArrayList<>(inputs(menu));
        if (!menu.slots.isEmpty()) {
            slots.add(menu.slots.get(BLUEPRINT_SLOT));
        }
        slots.addAll(inventory(menu));
        return slots;
    }

    @Override
    public List<Slot> getCraftingSlots(ModWorkbenchContainer menu) {
        return inputs(menu);
    }

    @Override
    public boolean supportsRecipe(EmiRecipe recipe) {
        return recipe != null && recipe.getCategory() == BlueprintCategory.CATEGORY;
    }

    @Override
    public boolean canCraft(EmiRecipe recipe, EmiCraftContext<ModWorkbenchContainer> context) {
        ItemStack blueprint = blueprint(recipe);
        ModWorkbenchContainer menu = context.getScreenHandler();
        boolean reachable = !blueprint.isEmpty()
                && (holds(menu, blueprint) || blueprintSlot(menu, blueprint) != null);
        return host(recipe) != null && reachable && context.getInventory().canCraft(recipe);
    }

    /**
     * The blueprint in slot 0 is what makes the six input slots exist, and inserting one rebinds the menu, so a fill
     * that has to place it finishes on a later tick when the new slots are there.
     */
    @Override
    public boolean craft(EmiRecipe recipe, EmiCraftContext<ModWorkbenchContainer> context) {
        Minecraft client = Minecraft.getInstance();
        ModWorkbenchContainer menu = context.getScreenHandler();
        if (client.player == null || client.gameMode == null || host(recipe) == null) {
            return false;
        }
        ItemStack blueprint = blueprint(recipe);
        if (!blueprint.isEmpty() && !holds(menu, blueprint)) {
            Slot source = blueprintSlot(menu, blueprint);
            if (source == null) {
                return false;
            }
            ClickSlots.quickMove(menu, source);
            awaitRebind(recipe, context, 0);
            return true;
        }
        return fill(recipe, context);
    }

    /** EMI's filler prices the fill, so Craft All places what the sources pay for instead of one craft's worth. */
    private boolean fill(EmiRecipe recipe, EmiCraftContext<ModWorkbenchContainer> context) {
        if (!context.getScreenHandler().getCarried().isEmpty()) {
            return false;
        }
        EmiCraftContext<ModWorkbenchContainer> stage = new EmiCraftContext<>(context.getScreen(),
                context.getInventory(), context.getType(), EmiCraftContext.Destination.NONE, batches(context));
        if (!StandardRecipeHandler.super.craft(recipe, stage)) {
            return false;
        }
        return context.getDestination() == EmiCraftContext.Destination.NONE || take(recipe, context);
    }

    // The server owns the page the take is gated on; a station without this mod's channel gets the page walk below.
    private static boolean take(EmiRecipe recipe, EmiCraftContext<ModWorkbenchContainer> context) {
        ModWorkbenchContainer menu = context.getScreenHandler();
        Slot output = output(menu, host(recipe));
        if (output == null) {
            return false;
        }
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (connection != null && NetworkRegistry.hasChannel(connection, WorkbenchCraftPayload.TYPE.id())) {
            PacketDistributor.sendToServer(new WorkbenchCraftPayload(menu.containerId, output.index,
                    context.getDestination().ordinal(), batches(context)));
            return true;
        }
        takeHere(context, output, context.getDestination());
        return true;
    }

    private static int batches(EmiCraftContext<ModWorkbenchContainer> context) {
        return Math.max(1, Math.min(context.getAmount(), MAX_BATCHES));
    }

    private static void awaitRebind(EmiRecipe recipe, EmiCraftContext<ModWorkbenchContainer> context, int waited) {
        ClientTicks.after(1, () -> {
            if (Minecraft.getInstance().screen != context.getScreen()) {
                return;
            }
            if (holds(context.getScreenHandler(), blueprint(recipe))) {
                INSTANCE.fill(recipe, context);
            } else if (waited < REBIND_TICKS) {
                awaitRebind(recipe, context, waited + 1);
            }
        });
    }

    private static ItemStack blueprint(EmiRecipe recipe) {
        for (EmiIngredient catalyst : recipe.getCatalysts()) {
            if (!catalyst.isEmpty() && !catalyst.getEmiStacks().isEmpty()) {
                return catalyst.getEmiStacks().get(0).getItemStack();
            }
        }
        return ItemStack.EMPTY;
    }

    private static boolean holds(ModWorkbenchContainer menu, ItemStack blueprint) {
        if (menu.slots.isEmpty()) {
            return false;
        }
        ItemStack held = menu.slots.get(BLUEPRINT_SLOT).getItem();
        return !held.isEmpty() && ItemStack.isSameItemSameComponents(held, blueprint);
    }

    private static Slot blueprintSlot(ModWorkbenchContainer menu, ItemStack blueprint) {
        for (Slot slot : inventory(menu)) {
            ItemStack stack = slot.getItem();
            if (!stack.isEmpty() && ItemStack.isSameItemSameComponents(stack, blueprint)) {
                return slot;
            }
        }
        return null;
    }

    /**
     * IE only takes an output slot that is on the page the server shows, and the page is server state the client is
     * never told about, so this walks to the recipe's page and steps forward if that guess was off.
     */
    private static void takeHere(EmiCraftContext<ModWorkbenchContainer> context, Slot output,
                                 EmiCraftContext.Destination destination) {
        Minecraft client = Minecraft.getInstance();
        ModWorkbenchContainer menu = context.getScreenHandler();
        // IE decides the page from the recipe's own index, while the menu list is offset by the blueprint slot.
        for (int i = 0; i < output.getSlotIndex() / OUTPUTS_PER_PAGE; i++) {
            client.gameMode.handleInventoryButtonClick(menu.containerId, NEXT_PAGE);
        }
        int pages = outputs(menu) / OUTPUTS_PER_PAGE + 1;
        takeAndCheck(client, context, output.index, destination, Math.max(1, pages));
    }

    private static void takeAndCheck(Minecraft client, EmiCraftContext<ModWorkbenchContainer> context, int slot,
                                     EmiCraftContext.Destination destination, int triesLeft) {
        ModWorkbenchContainer menu = context.getScreenHandler();
        Slot output = menu.slots.get(slot);
        if (destination == EmiCraftContext.Destination.CURSOR) {
            ClickSlots.pickup(menu, output);
        } else {
            ClickSlots.quickMove(menu, output);
        }
        ClientTicks.after(2, () -> {
            if (Minecraft.getInstance().screen != context.getScreen() || triesLeft <= 1
                    || output.getItem().isEmpty()) {
                return;
            }
            client.gameMode.handleInventoryButtonClick(context.getScreenHandler().containerId, NEXT_PAGE);
            takeAndCheck(client, context, slot, destination, triesLeft - 1);
        });
    }

    private static int outputs(ModWorkbenchContainer menu) {
        int outputs = 0;
        for (Slot slot : menu.slots) {
            if (slot instanceof IESlot.BlueprintOutput) {
                outputs++;
            }
        }
        return outputs;
    }

    private static BlueprintCraftingRecipe host(EmiRecipe recipe) {
        return recipe instanceof BlueprintEmiRecipe page ? page.host() : null;
    }

    private static List<Slot> inputs(ModWorkbenchContainer menu) {
        List<Slot> slots = new ArrayList<>(6);
        for (Slot slot : menu.slots) {
            if (slot instanceof IESlot.BlueprintInput) {
                slots.add(slot);
            }
        }
        slots.sort(Comparator.comparingInt(Slot::getSlotIndex));
        return slots;
    }

    private static List<Slot> inventory(ModWorkbenchContainer menu) {
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return List.of();
        }
        List<Slot> slots = new ArrayList<>();
        for (Slot slot : menu.slots) {
            if (slot.container == player.getInventory()) {
                slots.add(slot);
            }
        }
        return slots;
    }

    private static Slot output(ModWorkbenchContainer menu, BlueprintCraftingRecipe host) {
        for (Slot slot : menu.slots) {
            if (slot instanceof IESlot.BlueprintOutput out && out.recipe == host) {
                return slot;
            }
        }
        return null;
    }
}
