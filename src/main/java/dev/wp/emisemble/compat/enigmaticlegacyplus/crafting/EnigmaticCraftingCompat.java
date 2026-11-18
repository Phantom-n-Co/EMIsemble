package dev.wp.emisemble.compat.enigmaticlegacyplus.crafting;

import auviotre.enigmatic.legacy.api.item.ITaintable;
import auviotre.enigmatic.legacy.contents.crafting.AnnihilatingRecipe;
import auviotre.enigmatic.legacy.contents.crafting.CursedShapedRecipe;
import auviotre.enigmatic.legacy.registries.EnigmaticComponents;
import auviotre.enigmatic.legacy.registries.EnigmaticItems;
import auviotre.enigmatic.legacy.registries.EnigmaticTags;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiCraftingRecipe;
import dev.emi.emi.api.stack.Comparison;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.ListEmiIngredient;
import dev.wp.emisemble.Emisemble;
import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * What the host does with a JEI category extension, done inside the vanilla crafting category: EMI has no extension
 * mechanism, so a page is re-registered under the recipe's own id with the inputs this mod chooses.
 */
public final class EnigmaticCraftingCompat {
    private static final ResourceLocation ANNIHILATING_ID =
            ResourceLocation.fromNamespaceAndPath("enigmaticlegacyplus", "annihilating_sword");

    private EnigmaticCraftingCompat() {}

    public static void register(EmiRegistry registry) {
        distinguishTaintedHearts(registry);
        int cursed = rewriteCursedRecipes(registry);
        int annihilating = addAnnihilatingPage(registry);
        Emisemble.LOGGER.info("enigmaticlegacyplus: {} cursed crafting pages re-rendered, {} annihilating page added",
                cursed, annihilating);
    }

    // EMI's default comparison calls every pair equal, so a tainted heart and a clean one would be one entry.
    private static void distinguishTaintedHearts(EmiRegistry registry) {
        addTaintedVariant(registry, EnigmaticItems.TWISTED_HEART);
        addTaintedVariant(registry, EnigmaticItems.PURE_HEART);
        addTaintedVariant(registry, EnigmaticItems.ABYSSAL_HEART);
    }

    /** The variant goes right after the clean heart, or it reads as an unrelated item at the end of the index. */
    private static void addTaintedVariant(EmiRegistry registry, DeferredItem<? extends Item> heart) {
        registry.setDefaultComparison(heart.get(), Comparison.compareComponents());
        ItemStack tainted = new ItemStack(heart.get());
        tainted.set(EnigmaticComponents.TAINTABLE.get(), true);
        EmiStack variant = EmiStack.of(tainted);
        registry.addEmiStackAfter(variant, stack -> heart.get().equals(stack.getKey()) && !stack.isEqual(variant));
    }

    private static int rewriteCursedRecipes(EmiRegistry registry) {
        HolderLookup.Provider registries = registryAccess();
        if (registries == null) {
            return 0;
        }
        int rewritten = 0;
        for (RecipeHolder<CraftingRecipe> holder : craftingRecipes(registry)) {
            if (!(holder.value() instanceof CursedShapedRecipe cursed)) {
                continue;
            }
            try {
                ResourceLocation id = holder.id();
                // Removal is applied at bake time, so the predicate has to spare the replacement or the page vanishes.
                registry.removeRecipes(r -> id.equals(r.getId()) && !(r instanceof Replacement));
                registry.addRecipe(new Replacement(grid(cursed),
                        EmiStack.of(cursed.getResultItem(registries).copy()), id));
                rewritten++;
            } catch (Throwable t) {
                Emisemble.LOGGER.warn("Skipping cursed crafting page {}", holder.id(), t);
            }
        }
        return rewritten;
    }

    /** The host's extension lays this grid out by hand: four tagged corners, the ingot, and two tainted hearts. */
    private static int addAnnihilatingPage(EmiRegistry registry) {
        HolderLookup.Provider registries = registryAccess();
        if (registries == null) {
            return 0;
        }
        for (RecipeHolder<CraftingRecipe> holder : craftingRecipes(registry)) {
            if (!(holder.value() instanceof AnnihilatingRecipe recipe)) {
                continue;
            }
            try {
                EmiIngredient abyssal = EmiIngredient.of(Ingredient.of(EnigmaticTags.Items.ABYSSAL_ITEMS));
                List<EmiIngredient> grid = List.of(
                        abyssal, EmiStack.of(EnigmaticItems.EVIL_INGOT.get()), abyssal,
                        EmiStack.EMPTY, tainted(EnigmaticItems.ABYSSAL_HEART.get()), EmiStack.EMPTY,
                        abyssal, tainted(EnigmaticItems.TWISTED_HEART.get()), abyssal);
                ResourceLocation id = holder.id();
                registry.removeRecipes(r -> id.equals(r.getId()) && !(r instanceof Replacement));
                registry.addRecipe(new Replacement(grid,
                        EmiStack.of(recipe.getResultItem(registries).copy()), id));
                return 1;
            } catch (Throwable t) {
                Emisemble.LOGGER.warn("Skipping annihilating crafting page {}", holder.id(), t);
                return 0;
            }
        }
        Emisemble.LOGGER.warn("enigmaticlegacyplus: no annihilating recipe found under {}", ANNIHILATING_ID);
        return 0;
    }

    /** The pattern laid into a 3-wide grid; the pattern's own width decides the row break, as in JEI's helper. */
    private static List<EmiIngredient> grid(CursedShapedRecipe recipe) {
        List<Ingredient> ingredients = recipe.pattern.ingredients();
        int width = recipe.pattern.width();
        List<EmiIngredient> grid = new ArrayList<>(9);
        for (int i = 0; i < 9; i++) {
            int x = i % 3;
            int y = i / 3;
            int source = y * width + x;
            grid.add(x < width && source < ingredients.size() ? tainted(ingredients.get(source)) : EmiStack.EMPTY);
        }
        return grid;
    }

    private static EmiIngredient tainted(Ingredient ingredient) {
        List<EmiStack> stacks = Arrays.stream(ingredient.getItems())
                .map(ItemStack::copy)
                .map(stack -> {
                    if (stack.getItem() instanceof ITaintable) {
                        stack.set(EnigmaticComponents.TAINTABLE.get(), true);
                    }
                    return EmiStack.of(stack);
                })
                .toList();
        if (stacks.isEmpty()) {
            return EmiStack.EMPTY;
        }
        return stacks.size() == 1 ? stacks.getFirst() : new ListEmiIngredient(stacks, 1);
    }

    private static EmiStack tainted(Item item) {
        ItemStack stack = new ItemStack(item);
        stack.set(EnigmaticComponents.TAINTABLE.get(), true);
        return EmiStack.of(stack);
    }

    private static List<RecipeHolder<CraftingRecipe>> craftingRecipes(EmiRegistry registry) {
        return registry.getRecipeManager().getAllRecipesFor(RecipeType.CRAFTING);
    }

    private static HolderLookup.Provider registryAccess() {
        return Minecraft.getInstance().level == null ? null : Minecraft.getInstance().level.registryAccess();
    }

/**
 * A crafting page that replaces one EMI already rendered: the type is what spares it from the removal predicate.
 */
    private static final class Replacement extends EmiCraftingRecipe {
        private Replacement(List<EmiIngredient> inputs, EmiStack output, ResourceLocation id) {
            super(inputs, output, id, false);
        }
    }
}
