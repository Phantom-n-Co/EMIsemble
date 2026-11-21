package dev.wp.emisemble.compat.hexerei.woodcutter;

import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.handler.EmiCraftContext;
import dev.emi.emi.api.recipe.handler.StandardRecipeHandler;
import dev.emi.emi.api.stack.EmiStack;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.porting.ClientTicks;
import dev.wp.emisemble.porting.TransferSlots;
import net.joefoxe.hexerei.container.ModContainers;
import net.joefoxe.hexerei.container.WoodcutterContainer;
import net.joefoxe.hexerei.data.recipes.WoodcutterRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * The woodcutter is a stonecutter: the log socket takes the page's input, and the menu's button index is what fills the
 * result slot, whose take spends the log.
 */
public final class WoodcutterHandler implements StandardRecipeHandler<WoodcutterContainer> {
    private static final int INPUT = 0;
    private static final int RESULT = 1;
    private static final int INVENTORY_START = 2;
    private static final int INVENTORY_SLOTS = 36;

    private WoodcutterHandler() {}

    public static int register(EmiRegistry registry) {
        try {
            registry.addRecipeHandler(ModContainers.WOODCUTTER_CONTAINER.get(), new WoodcutterHandler());
            return 1;
        } catch (Throwable t) {
            Emisemble.LOGGER.warn("hexerei: could not register the woodcutter transfer handler", t);
            return 0;
        }
    }

    @Override
    public List<Slot> getInputSources(WoodcutterContainer menu) {
        return TransferSlots.sources(getCraftingSlots(menu), menu.slots.subList(
                Math.min(INVENTORY_START, menu.slots.size()),
                Math.min(INVENTORY_START + INVENTORY_SLOTS, menu.slots.size())));
    }

    @Override
    public List<Slot> getCraftingSlots(WoodcutterContainer menu) {
        return List.of(menu.slots.get(INPUT));
    }

    @Override
    public Slot getOutputSlot(WoodcutterContainer menu) {
        return menu.slots.get(RESULT);
    }

    @Override
    public boolean supportsRecipe(EmiRecipe recipe) {
        return recipe.getCategory() == WoodcutterCategory.CATEGORY;
    }

    @Override
    public boolean craft(EmiRecipe recipe, EmiCraftContext<WoodcutterContainer> context) {
        if (!StandardRecipeHandler.super.craft(recipe, context)) {
            return false;
        }
        // The button index selects a recipe, and the client only sees the placed log after the server's sync, so the
        // index and the take both happen on a later tick.
        ClientTicks.after(2, () -> select(recipe, context, 3));
        return true;
    }

    private static void select(EmiRecipe recipe, EmiCraftContext<WoodcutterContainer> context, int attempts) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.gameMode == null || !open(context)) {
            return;
        }
        WoodcutterContainer menu = context.getScreenHandler();
        int index = indexOf(menu, recipe);
        if (index < 0) {
            if (attempts > 0) {
                ClientTicks.after(1, () -> select(recipe, context, attempts - 1));
            }
            return;
        }
        client.gameMode.handleInventoryButtonClick(menu.containerId, index);
        if (context.getDestination() != EmiCraftContext.Destination.NONE) {
            ClientTicks.after(2, () -> take(recipe, context, 4));
        }
    }

    private static void take(EmiRecipe recipe, EmiCraftContext<WoodcutterContainer> context, int attempts) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.gameMode == null || !open(context)) {
            return;
        }
        WoodcutterContainer menu = context.getScreenHandler();
        // Until the selection lands the result still holds the default recipe's output, and taking that crafts the wrong item.
        if (!isPageOutput(menu, recipe)) {
            if (attempts > 0) {
                ClientTicks.after(1, () -> take(recipe, context, attempts - 1));
            }
            return;
        }
        ClickType type = context.getDestination() == EmiCraftContext.Destination.CURSOR
                ? ClickType.PICKUP : ClickType.QUICK_MOVE;
        client.gameMode.handleInventoryMouseClick(menu.containerId, RESULT, 0, type, client.player);
    }

    private static boolean isPageOutput(WoodcutterContainer menu, EmiRecipe recipe) {
        ItemStack result = menu.slots.get(RESULT).getItem();
        return !result.isEmpty() && !recipe.getOutputs().isEmpty()
                && result.getItem() == recipe.getOutputs().get(0).getKey();
    }

    private static boolean open(EmiCraftContext<WoodcutterContainer> context) {
        return Minecraft.getInstance().screen == context.getScreen();
    }

    // The host's own list is the one the server filters the index against: its data recipes plus the generated
    // woodcutting variants, minus the ones the log count cannot pay for. Its ids are synthetic, so the output matches.
    private static int indexOf(WoodcutterContainer menu, EmiRecipe recipe) {
        Level level = Minecraft.getInstance().level;
        if (level == null || recipe.getOutputs().isEmpty()) {
            return -1;
        }
        EmiStack wanted = recipe.getOutputs().get(0);
        List<RecipeHolder<WoodcutterRecipe>> recipes = menu.getRecipes();
        for (int i = 0; i < recipes.size(); i++) {
            if (EmiStack.of(recipes.get(i).value().getResultItem(level.registryAccess())).isEqual(wanted)) {
                return i;
            }
        }
        return -1;
    }
}
