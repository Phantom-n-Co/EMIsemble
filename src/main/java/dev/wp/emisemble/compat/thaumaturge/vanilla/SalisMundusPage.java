package dev.wp.emisemble.compat.thaumaturge.vanilla;

import com.leclowndu93150.thaumaturge.content.recipe.SalisMundusRecipe;
import com.leclowndu93150.thaumaturge.content.taint.item.EssentiaCrystalFactory;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiCraftingRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.wp.emisemble.Emisemble;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.ArrayList;
import java.util.List;

/** Salis mundus on EMI's vanilla crafting category: EMI drops every CustomRecipe, so the page has to be rebuilt. */
public final class SalisMundusPage {
    private static final int CRYSTAL_SLOTS = 3;

    private SalisMundusPage() {}

    public static void register(EmiRegistry registry) {
        List<RecipeHolder<CraftingRecipe>> recipes;
        try {
            recipes = registry.getRecipeManager().getAllRecipesFor(RecipeType.CRAFTING);
        } catch (Throwable t) {
            Emisemble.LOGGER.warn("thaumaturge: could not read the crafting recipes for the salis mundus page", t);
            return;
        }

        int added = 0;
        for (RecipeHolder<CraftingRecipe> holder : recipes) {
            if (!(holder.value() instanceof SalisMundusRecipe)) {
                continue;
            }
            try {
                registry.addRecipe(new EmiCraftingRecipe(
                        inputs(),
                        EmiStack.of(new ItemStack(TTItems.SALIS_MUNDUS.get())),
                        idOf(holder),
                        false));
                added++;
            } catch (Throwable t) {
                Emisemble.LOGGER.warn("Skipping the salis mundus page for {}", holder.id(), t);
            }
        }
        if (added == 0) {
            Emisemble.LOGGER.warn("thaumaturge: no salis mundus recipe found, the crafting page is missing");
        } else {
            Emisemble.LOGGER.info("thaumaturge: {} salis mundus crafting pages registered", added);
        }
    }

    private static List<EmiIngredient> inputs() {
        List<EmiIngredient> inputs = new ArrayList<>(3 + CRYSTAL_SLOTS);
        inputs.add(EmiStack.of(Items.FLINT));
        inputs.add(EmiStack.of(Items.BOWL));
        inputs.add(EmiStack.of(Items.REDSTONE));

        // One crystal slot per column, each rotated so the same aspect does not repeat straight down.
        List<EmiStack> crystals = crystalStacks();
        for (int slot = 0; slot < CRYSTAL_SLOTS; slot++) {
            inputs.add(EmiIngredient.of(rotated(crystals, slot)));
        }
        return inputs;
    }

    private static List<EmiStack> crystalStacks() {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            List<ItemStack> variants = EssentiaCrystalFactory.discoveredCrystals(player);
            if (!variants.isEmpty()) {
                return variants.stream().map(EmiStack::of).toList();
            }
        }
        return List.of(EmiStack.of(TTItems.ESSENTIA_CRYSTAL.get()));
    }

    private static List<EmiStack> rotated(List<EmiStack> stacks, int offset) {
        if (stacks.size() <= 1) {
            return stacks;
        }
        List<EmiStack> shifted = new ArrayList<>(stacks.size());
        for (int i = 0; i < stacks.size(); i++) {
            shifted.add(stacks.get((i + offset) % stacks.size()));
        }
        return shifted;
    }

    // Synthetic: removing the custom recipe by id would also delete the replacement, EMI applies that at bake time.
    private static ResourceLocation idOf(RecipeHolder<CraftingRecipe> holder) {
        return ResourceLocation.fromNamespaceAndPath("thaumaturge",
                "/salis_mundus/" + holder.id().getNamespace() + "/" + holder.id().getPath());
    }
}
