package dev.wp.emisemble.compat.enigmaticlegacyplus.spellstone;

import auviotre.enigmatic.legacy.contents.crafting.SpellstoneTableRecipe;
import auviotre.enigmatic.legacy.registries.EnigmaticItems;
import auviotre.enigmatic.legacy.registries.EnigmaticTags;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.ListEmiIngredient;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.wp.emisemble.porting.RecipeSlots;
import net.minecraft.client.Minecraft;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

/** One spellstone page, 132x80: seven ring cells round the core, the debris cost and the result. */
public final class SpellstoneEmiRecipe extends BasicEmiRecipe {
    private static final int[][] RING_CELLS = {
            {38, 52}, {31, 32}, {38, 12}, {58, 5}, {78, 12}, {85, 32}, {78, 52}
    };

    private static final int DEBRIS_X = 5;
    private static final int DEBRIS_Y = 32;
    private static final int CORE_X = 58;
    private static final int CORE_Y = 32;
    private static final int SPELL_X = 111;
    private static final int SPELL_Y = 32;
    private static final int OUTPUT_X = 58;
    private static final int OUTPUT_Y = 59;

    private static final String ALL_DIFFERENT_KEY = "gui.enigmaticlegacy.jei.spellstone_crafting.all_different";

    private final SpellstoneTableRecipe recipe;
    private final boolean fragmentation;
    private final List<EmiIngredient> ring = new ArrayList<>();
    private final EmiStack debris;
    private final EmiStack core;
    private final EmiIngredient spellstones;
    private final EmiStack output;

    public SpellstoneEmiRecipe(EmiRecipeCategory category, RecipeHolder<SpellstoneTableRecipe> holder) {
        this(category, holder.id(), holder.value());
    }

    public SpellstoneEmiRecipe(EmiRecipeCategory category, ResourceLocation id, SpellstoneTableRecipe recipe) {
        super(category, id, SpellstonePageArt.WIDTH, SpellstonePageArt.HEIGHT);
        this.recipe = recipe;
        // The fragmentation page has no json, so its id is the only thing that tells the two layouts apart.
        this.fragmentation = SpellstoneCompat.FRAGMENTATION_ID.equals(id);

        NonNullList<Ingredient> ingredients = recipe.getIngredients();
        int cells = Math.min(ingredients.size(), RING_CELLS.length);
        for (int i = 0; i < cells; i++) {
            Ingredient ingredient = ingredients.get(i);
            ring.add(recipe.isAllDifferent() ? rotated(ingredient, i) : EmiIngredient.of(ingredient));
        }

        this.debris = recipe.getCount() > 0
                ? EmiStack.of(EnigmaticItems.SPELLSTONE_DEBRIS.toStack(recipe.getCount()))
                : EmiStack.EMPTY;

        this.core = fragmentation ? EmiStack.EMPTY : EmiStack.of(EnigmaticItems.SPELLCORE.get());
        this.spellstones = fragmentation
                ? EmiIngredient.of(Ingredient.of(EnigmaticTags.Items.SPELLSTONES))
                : EmiStack.EMPTY;

        Level level = Minecraft.getInstance().level;
        ItemStack result = level == null ? ItemStack.EMPTY : recipe.getResultItem(level.registryAccess());
        this.output = EmiStack.of(result);

        inputs.addAll(ring);
        if (!debris.isEmpty()) {
            inputs.add(debris);
        }
        if (!core.isEmpty()) {
            inputs.add(core);
        }
        if (!spellstones.isEmpty()) {
            inputs.add(spellstones);
        }
        if (!output.isEmpty()) {
            outputs.add(output);
        }
    }

    // The list has to be explicit: EmiIngredient.of folds an all-tag ingredient back into a TagEmiIngredient and
    // re-reads the tag, which drops the rotation the host's all-different pages rely on.
    private static EmiIngredient rotated(Ingredient ingredient, int cell) {
        ItemStack[] items = ingredient.getItems();
        if (items.length < 2) {
            return EmiIngredient.of(ingredient);
        }
        List<EmiStack> stacks = new ArrayList<>(items.length);
        for (int j = 0; j < items.length; j++) {
            stacks.add(EmiStack.of(items[(j + cell) % items.length]));
        }
        return new ListEmiIngredient(stacks, 1L);
    }

    int ringCount() {
        return ring.size();
    }

    boolean hasDebris() {
        return !debris.isEmpty();
    }

    boolean fragmentation() {
        return fragmentation;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        SpellstonePageArt.page(widgets);

        for (int i = 0; i < RING_CELLS.length; i++) {
            EmiIngredient ingredient = i < ring.size() ? ring.get(i) : EmiStack.EMPTY;
            slot(widgets, ingredient, RING_CELLS[i][0], RING_CELLS[i][1]);
        }

        slot(widgets, debris, DEBRIS_X, DEBRIS_Y);

        if (fragmentation) {
            slot(widgets, spellstones, SPELL_X, SPELL_Y);
            slot(widgets, EmiStack.EMPTY, CORE_X, CORE_Y);
        } else {
            slot(widgets, core, CORE_X, CORE_Y);
        }

        // The result socket is what EMI roots the recipe tree at, and what it warns about when there is none.
        if (output.isEmpty()) {
            RecipeSlots.addEmpty(widgets, OUTPUT_X, OUTPUT_Y);
        } else {
            RecipeSlots.add(widgets, output, OUTPUT_X, OUTPUT_Y).recipeContext(this);
        }

        if (recipe.isAllDifferent()) {
            SpellstonePageArt.allDifferentBadge(widgets);
            widgets.addTooltipText(List.of(Component.translatable(ALL_DIFFERENT_KEY)),
                    SpellstonePageArt.ALL_DIFFERENT_X, SpellstonePageArt.ALL_DIFFERENT_Y,
                    SpellstonePageArt.ALL_DIFFERENT_SIZE, SpellstonePageArt.ALL_DIFFERENT_SIZE);
        }
    }

    private static void slot(WidgetHolder widgets, EmiIngredient ingredient, int jeiX, int jeiY) {
        if (ingredient.isEmpty()) {
            RecipeSlots.addEmpty(widgets, jeiX, jeiY);
        } else {
            RecipeSlots.add(widgets, ingredient, jeiX, jeiY);
        }
    }
}
