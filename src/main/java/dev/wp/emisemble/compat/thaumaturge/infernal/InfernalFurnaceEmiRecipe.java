package dev.wp.emisemble.compat.thaumaturge.infernal;

import com.leclowndu93150.thaumaturge.content.infernalfurnace.InfernalBonus;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.wp.emisemble.compat.thaumaturge.ThaumaturgeCompat;
import dev.wp.emisemble.porting.EmiRecipes;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.ArrayList;
import java.util.List;

/** A smeltable input, its smelting result and one bonus slot per entry of the item's bonus list. */
public final class InfernalFurnaceEmiRecipe extends BasicEmiRecipe {
    private static final int WIDTH = 144;
    private static final int HEIGHT = 108;

    // EMI-space: JEI's ingredient rects (22,7), (96,55) and (123,9+20i), one pixel up and left.
    private static final int INPUT_SLOT_X = 21;
    private static final int INPUT_SLOT_Y = 6;
    private static final int RESULT_SLOT_X = 95;
    private static final int RESULT_SLOT_Y = 54;
    private static final int BONUS_SLOT_X = 122;
    private static final int BONUS_SLOT_Y = 8;
    private static final int BONUS_SLOT_STEP = 20;

    private static final String CHANCE_KEY = "gui.jei.category.compostable.chance";
    private static final String COUNT_KEY = "jei.thaumaturge.infernal_furnace.count";

    private final Ingredient input;
    private final ItemStack result;
    private final List<InfernalBonus> bonuses;

    public InfernalFurnaceEmiRecipe(ResourceLocation id, Ingredient input, ItemStack result, List<InfernalBonus> bonuses) {
        super(InfernalFurnaceCompat.INFERNAL_FURNACE, id, WIDTH, HEIGHT);
        this.input = input;
        this.result = result;
        this.bonuses = bonuses;

        inputs.add(EmiIngredient.of(input));
        outputs.add(EmiStack.of(result));
        for (InfernalBonus bonus : bonuses) {
            for (Holder<Item> bonusItem : bonus.items()) {
                outputs.add(EmiStack.of(bonusItem.value()));
            }
        }
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        // Art before slots: JEI draws the category first, or the result frame paints over its item.
        ThaumaturgeCompat.art(widgets, 89, 48, 30, 30, 41, 7);
        ThaumaturgeCompat.art(widgets, 39, 9, 26, 26, 199, 168);
        ThaumaturgeCompat.art(widgets, 21, 33, 67, 60, 445, 452);

        widgets.addSlot(EmiIngredient.of(input), INPUT_SLOT_X, INPUT_SLOT_Y).drawBack(false);
        widgets.addSlot(EmiStack.of(result), RESULT_SLOT_X, RESULT_SLOT_Y).drawBack(false);

        for (int index = 0; index < bonuses.size(); index++) {
            InfernalBonus bonus = bonuses.get(index);
            int y = BONUS_SLOT_Y + index * BONUS_SLOT_STEP;
            widgets.addSlot(bonusIngredient(bonus), BONUS_SLOT_X, y).drawBack(false);
            widgets.addTooltip((mouseX, mouseY) -> tooltip(bonus), BONUS_SLOT_X, y, 18, 18);
        }
    }

    /** The bonus stacks are rolled on top of the result, not craftable outputs, so the page has no tree. */
    @Override
    public boolean supportsRecipeTree() {
        return false;
    }

    // JEI cycles an entry's holders inside a single output slot, so one slot per bonus entry.
    private static EmiIngredient bonusIngredient(InfernalBonus bonus) {
        return EmiIngredient.of(bonus.items().stream().<EmiIngredient>map(holder -> EmiStack.of(holder.value())).toList());
    }

    private static List<ClientTooltipComponent> tooltip(InfernalBonus bonus) {
        List<Component> lines = new ArrayList<>(2);
        lines.add(chanceLine(bonus).withStyle(ChatFormatting.GRAY));
        lines.add(Component.translatable(COUNT_KEY, countText(bonus)).withStyle(ChatFormatting.GRAY));
        return EmiRecipes.tooltip(lines);
    }

    // The host feeds JEI's composting-chance key; without JEMI there is no translation, so fall back to its English.
    private static MutableComponent chanceLine(InfernalBonus bonus) {
        float percent = bonus.chance() * 100.0F;
        if (I18n.exists(CHANCE_KEY)) {
            return Component.translatable(CHANCE_KEY, percent);
        }
        return Component.literal("Chance: " + percent + "%");
    }

    private static String countText(InfernalBonus bonus) {
        int min = bonus.count().getMinValue();
        int max = bonus.count().getMaxValue();
        return min == max ? Integer.toString(min) : min + "-" + max;
    }
}
