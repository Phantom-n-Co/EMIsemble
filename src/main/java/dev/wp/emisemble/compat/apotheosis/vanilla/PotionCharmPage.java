package dev.wp.emisemble.compat.apotheosis.vanilla;

import dev.emi.emi.api.recipe.EmiCraftingRecipe;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.recipe.PotionCharmRecipe;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// Extends the crafting page rather than the recipe interface: the filler and the 2x2 inventory check test for it.
/** The pattern with its potions and results cycling on one seed, so the charm shown is the one the potion makes. */
final class PotionCharmPage extends EmiCraftingRecipe {
    private static final int OUTPUT_X = 92;
    private static final int OUTPUT_Y = 14;

    private final int unique;
    private final List<EmiStack> outputs;
    private final List<Holder.Reference<Potion>> potions;
    private final Item[] potionItems;

    private PotionCharmPage(List<EmiIngredient> inputs, List<EmiStack> outputs, ResourceLocation id,
            List<Holder.Reference<Potion>> potions, Item[] potionItems) {
        super(inputs, outputs.get(0), id, false);
        // EMI re-seeds a generated slot from (second ^ unique); one value per page is what keeps the slots in step.
        this.unique = id.hashCode();
        this.outputs = List.copyOf(outputs);
        this.potions = List.copyOf(potions);
        this.potionItems = potionItems;
    }

    static PotionCharmPage of(ResourceLocation id, PotionCharmRecipe recipe, List<Holder.Reference<Potion>> potions) {
        List<Ingredient> ingredients = recipe.getIngredients();
        Item charm = Apoth.Items.POTION_CHARM.value();
        List<EmiIngredient> inputs = new ArrayList<>(9);

        Item[] potionItems = new Item[9];
        for (int i = 0; i < 9; i++) {
            Ingredient ingredient = i < ingredients.size() ? ingredients.get(i) : Ingredient.EMPTY;
            Item potionItem = potionItem(ingredient);
            if (potionItem == null) {
                inputs.add(EmiIngredient.of(ingredient));
                continue;
            }
            potionItems[i] = potionItem;
            // Every potion in the slot's own item, so looking any of them up opens this page.
            List<EmiIngredient> variants = new ArrayList<>(potions.size());
            for (Holder.Reference<Potion> potion : potions) {
                variants.add(EmiStack.of(PotionContents.createItemStack(potionItem, potion)));
            }
            inputs.add(EmiIngredient.of(variants));
        }

        List<EmiStack> outputs = new ArrayList<>(potions.size());
        for (Holder.Reference<Potion> potion : potions) {
            outputs.add(EmiStack.of(PotionContents.createItemStack(charm, potion)));
        }
        return new PotionCharmPage(inputs, outputs, id, potions, potionItems);
    }

    private static Item potionItem(Ingredient ingredient) {
        for (ItemStack stack : ingredient.getItems()) {
            if (stack.has(DataComponents.POTION_CONTENTS)) {
                return stack.getItem();
            }
        }
        return null;
    }

    // The only draw either supplier makes: same unique and same second means same potion in and out.
    private Holder.Reference<Potion> nextPotion(Random random) {
        return this.potions.get(random.nextInt(this.potions.size()));
    }

    private EmiStack charm(Random random) {
        return EmiStack.of(PotionContents.createItemStack(Apoth.Items.POTION_CHARM.value(), nextPotion(random)));
    }

    private EmiIngredient potion(Random random, Item item) {
        return EmiStack.of(PotionContents.createItemStack(item, nextPotion(random)));
    }

    @Override
    public List<EmiStack> getOutputs() {
        return this.outputs;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addTexture(EmiTexture.EMPTY_ARROW, 60, 18);
        for (int i = 0; i < 9; i++) {
            int x = i % 3 * 18;
            int y = i / 3 * 18;
            Item potionItem = this.potionItems[i];
            if (potionItem == null) {
                widgets.addSlot(this.input.get(i), x, y);
            } else {
                widgets.addGeneratedSlot(random -> potion(random, potionItem), this.unique, x, y);
            }
        }
        widgets.addGeneratedSlot(this::charm, this.unique, OUTPUT_X, OUTPUT_Y).large(true).recipeContext(this);
    }
}
