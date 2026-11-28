package dev.wp.emisemble.compat.eidolon.dye;

import alexthw.eidolon_repraised.api.IDyeable;
import alexthw.eidolon_repraised.recipe.DyeRecipe;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiCraftingRecipe;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.wp.emisemble.Emisemble;
import dev.wp.emisemble.porting.EmiRecipes;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/** The host's dye recipes as the vanilla crafting page it re-skins: the robe in each of the sixteen colours. */
public final class DyeCategory {
    private DyeCategory() {}

    public static int register(EmiRegistry registry) {
        List<ResourceLocation> replaced = new ArrayList<>();
        List<DyePage> pages = new ArrayList<>();
        for (RecipeHolder<CraftingRecipe> holder : registry.getRecipeManager().getAllRecipesFor(RecipeType.CRAFTING)) {
            if (!(holder.value() instanceof DyeRecipe recipe)) {
                continue;
            }
            DyePage page;
            try {
                page = DyePage.of(holder.id(), recipe);
            } catch (Throwable t) {
                Emisemble.LOGGER.warn("eidolon_repraised: skipping dye page {}", holder.id(), t);
                continue;
            }
            if (page == null) {
                continue;
            }
            replaced.add(holder.id());
            pages.add(page);
        }
        if (pages.isEmpty()) {
            return 0;
        }
        // EMI already shows these under the vanilla crafting category, uncoloured; ours carries the dyed result.
        registry.removeRecipes(recipe -> recipe.getId() != null && replaced.contains(recipe.getId())
                && !(recipe instanceof DyePage));
        int added = 0;
        for (DyePage page : pages) {
            if (EmiRecipes.add(registry, page)) {
                added++;
            }
        }
        Emisemble.LOGGER.info("eidolon_repraised: {} dye pages registered", added);
        return added;
    }

    private static final class DyePage extends EmiCraftingRecipe {
        private static final int OUTPUT_X = 92;
        private static final int OUTPUT_Y = 14;

        private final int unique;
        private final List<ItemStack> dyes;
        private final List<EmiStack> variants;

        private DyePage(ResourceLocation id, List<EmiIngredient> inputs, List<ItemStack> dyes, List<EmiStack> variants) {
            super(inputs, variants.getFirst(), id, true);
            // One seed for both generated slots, so the dye shown is the colour the result is.
            this.unique = id.hashCode();
            this.dyes = List.copyOf(dyes);
            this.variants = List.copyOf(variants);
        }

        private static DyePage of(ResourceLocation id, DyeRecipe recipe) {
            ItemStack result = recipe.getResultItem(RegistryAccess.EMPTY);
            if (!(result.getItem() instanceof IDyeable dyeable) || recipe.getIngredients().isEmpty()) {
                return null;
            }
            List<Ingredient> ingredients = recipe.getIngredients();
            Map<DyeColor, ItemStack> byColor = new LinkedHashMap<>();
            for (ItemStack dye : ingredients.getFirst().getItems()) {
                DyeColor color = DyeColor.getColor(dye);
                if (color != null) {
                    byColor.putIfAbsent(color, dye);
                }
            }
            if (byColor.isEmpty()) {
                return null;
            }
            List<ItemStack> dyes = new ArrayList<>(byColor.values());
            List<EmiStack> variants = new ArrayList<>(byColor.size());
            for (DyeColor color : byColor.keySet()) {
                ItemStack dyed = result.copy();
                dyeable.onDye(dyed, color);
                variants.add(EmiStack.of(dyed));
            }
            List<EmiIngredient> inputs = new ArrayList<>(ingredients.size());
            inputs.add(EmiIngredient.of(dyes.stream().map(EmiStack::of).toList()));
            for (int i = 1; i < ingredients.size(); i++) {
                inputs.add(EmiIngredient.of(ingredients.get(i)));
            }
            return new DyePage(id, inputs, dyes, variants);
        }

        private int next(Random random) {
            return random.nextInt(dyes.size());
        }

        @Override
        public List<EmiStack> getOutputs() {
            return variants;
        }

        @Override
        public void addWidgets(WidgetHolder widgets) {
            widgets.addTexture(EmiTexture.EMPTY_ARROW, 60, 18);
            widgets.addTexture(EmiTexture.SHAPELESS, 97, 0);
            widgets.addGeneratedSlot(random -> EmiStack.of(dyes.get(next(random))), unique, 0, 0);
            for (int i = 1; i < 9; i++) {
                EmiIngredient input = i < this.input.size() ? this.input.get(i) : EmiStack.EMPTY;
                widgets.addSlot(input, i % 3 * 18, i / 3 * 18);
            }
            widgets.addGeneratedSlot(random -> variants.get(next(random)), unique, OUTPUT_X, OUTPUT_Y)
                    .large(true).recipeContext(this);
        }
    }
}
