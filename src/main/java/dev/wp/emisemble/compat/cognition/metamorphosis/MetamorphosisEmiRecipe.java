package dev.wp.emisemble.compat.cognition.metamorphosis;

import com.cyanogen.cognition.recipe.MolecularMetamorpherRecipe;
import com.cyanogen.cognition.registries.RegisterItems;
import com.cyanogen.cognition.utils.ExperienceUtils;
import com.cyanogen.cognition.utils.RecipeUtils;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.TextWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.wp.emisemble.compat.cognition.CognitionCompat;
import dev.wp.emisemble.porting.AlphaTextures;
import dev.wp.emisemble.porting.RecipeSlots;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.ArrayList;
import java.util.List;

public final class MetamorphosisEmiRecipe extends BasicEmiRecipe {
    private static final int ARROW_X = 108;
    private static final int ARROW_Y = 47;
    private static final int ARROW_WIDTH = 29;
    private static final int ARROW_HEIGHT = 5;
    private static final int ARROW_V = 87;
    private static final int ARROW_MS = 100 * 50;
    private static final int INPUT_ONE_X = 19;
    private static final int INPUT_ONE_Y = 35;
    private static final int INPUT_TWO_X = 50;
    private static final int INPUT_TWO_Y = 52;
    private static final int INPUT_THREE_X = 70;
    private static final int INPUT_THREE_Y = 18;
    private static final int OUTPUT_X = 140;
    private static final int OUTPUT_Y = 35;
    private static final int BAR_X = 110;
    private static final int BAR_Y = 75;
    private static final int BAR_WIDTH = 61;
    private static final int BAR_HEIGHT = 10;
    private static final int BAR_V = 92;
    private static final int BAR_INSET = 2;
    private static final int LEVEL_X = 145;
    private static final int LEVEL_Y = 76;
    private static final int TOOLTIP_X = 109;
    private static final int TOOLTIP_Y = 73;
    private static final int TOOLTIP_WIDTH = 67;
    private static final int TOOLTIP_HEIGHT = 14;
    private static final int TICKS_PER_SECOND = 20;
    private static final String NAME_ANY_KEY = "jei.cognition.name.any_item";

    private final EmiIngredient first;
    private final EmiIngredient second;
    private final EmiIngredient third;
    private final EmiIngredient result;
    private final int cost;
    private final int processTime;
    private final boolean nameFormatting;

    private MetamorphosisEmiRecipe(EmiRecipeCategory category, ResourceLocation id, EmiIngredient first,
                                   EmiIngredient second, EmiIngredient third, List<EmiStack> results, int cost,
                                   int processTime, boolean nameFormatting) {
        super(category, id, MetamorphosisCategory.WIDTH, MetamorphosisCategory.HEIGHT);
        this.first = first;
        this.second = second;
        this.third = third;
        this.result = EmiIngredient.of(results);
        this.cost = cost;
        this.processTime = processTime;
        this.nameFormatting = nameFormatting;

        // An empty slot is drawn but never matched, so it stays out of the ingredient list.
        for (EmiIngredient input : List.of(first, second, third)) {
            if (!input.isEmpty()) {
                inputs.add(input);
            }
        }
        outputs.addAll(results);
    }

    static MetamorphosisEmiRecipe of(EmiRecipeCategory category, RecipeHolder<MolecularMetamorpherRecipe> holder) {
        MolecularMetamorpherRecipe recipe = holder.value();
        return new MetamorphosisEmiRecipe(category, holder.id(),
                counted(recipe.ingredient1, recipe.count1), counted(recipe.ingredient2, recipe.count2),
                counted(recipe.ingredient3, recipe.count3), List.of(EmiStack.of(recipe.getResultItem(null))),
                recipe.getCost(), recipe.getProcessTime(), false);
    }

    /** The host's synthetic page: any item, a name tag and a dye or format item naming a dummy sword. */
    static MetamorphosisEmiRecipe nameFormatting(EmiRecipeCategory category) {
        MolecularMetamorpherRecipe template = RecipeUtils.getEmptyNameFormattingRecipe();
        ItemStack dummy = new ItemStack(RegisterItems.DUMMY_SWORD.get());
        dummy.set(DataComponents.ITEM_NAME, Component.translatable(NAME_ANY_KEY));

        List<Item> dyes = RecipeUtils.getValidDyes();
        List<Item> formats = RecipeUtils.getValidFormattingItems();
        List<EmiStack> swatches = new ArrayList<>();
        dyes.forEach(dye -> swatches.add(EmiStack.of(dye)));
        formats.forEach(format -> swatches.add(EmiStack.of(format)));

        List<EmiStack> names = new ArrayList<>();
        for (Item dye : dyes) {
            if (dye instanceof DyeItem dyeItem) {
                names.add(named(dummy, ChatFormatting.getById(
                        RecipeUtils.dyeColorToTextColor(dyeItem.getDyeColor().getId()))));
            }
        }
        // The format list is indexed by position, which is how the host maps an item to its style.
        for (int i = 0; i < formats.size(); i++) {
            names.add(named(dummy, ChatFormatting.getByCode(RecipeUtils.itemToFormat(i))));
        }

        return new MetamorphosisEmiRecipe(category,
                ResourceLocation.fromNamespaceAndPath("cognition", "/item_name_formatting"),
                EmiStack.of(dummy), EmiStack.of(new ItemStack(Items.NAME_TAG)), EmiIngredient.of(swatches), names,
                template.getCost(), template.getProcessTime(), true);
    }

    public boolean isNameFormatting() {
        return nameFormatting;
    }

    private static EmiStack named(ItemStack dummy, ChatFormatting style) {
        ItemStack named = dummy.copy();
        named.set(DataComponents.ITEM_NAME, Component.translatable(NAME_ANY_KEY).withStyle(style));
        return EmiStack.of(named);
    }

    private static EmiIngredient counted(Ingredient ingredient, int count) {
        return ingredient.isEmpty() ? EmiStack.EMPTY : EmiIngredient.of(ingredient, count);
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        AlphaTextures.add(widgets, MetamorphosisCategory.TEXTURE, 256, 0, 0,
                MetamorphosisCategory.WIDTH, MetamorphosisCategory.HEIGHT, 0, 0);
        widgets.addAnimatedTexture(MetamorphosisCategory.TEXTURE, ARROW_X, ARROW_Y, ARROW_WIDTH, ARROW_HEIGHT, 0,
                ARROW_V, ARROW_MS, true, false, false);

        RecipeSlots.add(widgets, first, INPUT_ONE_X, INPUT_ONE_Y);
        RecipeSlots.add(widgets, second, INPUT_TWO_X, INPUT_TWO_Y);
        RecipeSlots.add(widgets, third, INPUT_THREE_X, INPUT_THREE_Y);
        RecipeSlots.add(widgets, result, OUTPUT_X, OUTPUT_Y).recipeContext(this);

        int level = ExperienceUtils.xpToLevels(cost);
        double progress = ExperienceUtils.getProgressToNextLevel(cost, level);
        int filled = (int) (BAR_WIDTH * (progress == 0 ? 1 : progress)) + BAR_INSET;
        AlphaTextures.add(widgets, MetamorphosisCategory.TEXTURE, 256, BAR_X, BAR_Y, filled, BAR_HEIGHT, 0, BAR_V);
        widgets.addText(CognitionCompat.green(level), LEVEL_X, LEVEL_Y, 0xFFFFFF, true)
                .horizontalAlign(TextWidget.Alignment.CENTER);
        widgets.addTooltipText(List.of(
                Component.translatable("jei.cognition.metamorpher.cost_xp", CognitionCompat.green(cost)),
                Component.translatable("jei.cognition.metamorpher.process_time",
                        Component.literal(processTime / TICKS_PER_SECOND + "s").withStyle(ChatFormatting.GOLD))),
                TOOLTIP_X, TOOLTIP_Y, TOOLTIP_WIDTH, TOOLTIP_HEIGHT);
    }
}
