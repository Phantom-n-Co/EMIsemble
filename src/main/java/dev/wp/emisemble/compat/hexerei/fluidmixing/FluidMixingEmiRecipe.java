package dev.wp.emisemble.compat.hexerei.fluidmixing;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.wp.emisemble.porting.EmiRecipes;
import dev.wp.emisemble.porting.OwnFrameRecipe;
import dev.wp.emisemble.porting.RecipeSlots;
import net.joefoxe.hexerei.data.recipes.FluidMixingRecipe;
import net.joefoxe.hexerei.data.recipes.FluidMixingRecipe.HeatCondition;
import net.joefoxe.hexerei.util.HexereiTags;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

/** One fluid mixing page; both fluid categories share it. */
public final class FluidMixingEmiRecipe extends BasicEmiRecipe implements OwnFrameRecipe {
    private static final int TANK_CAPACITY = 2000;

    private static final int[][] ITEM_SLOTS = {
            {79, 14}, {101, 23}, {110, 45}, {101, 67}, {79, 76}, {57, 67}, {48, 45}, {57, 23}};

    private static final int HEAT_HOVER_X = 75;
    private static final int HEAT_HOVER_Y = 55;
    private static final int HEAT_HOVER_WIDTH = 24;
    private static final int HEAT_HOVER_HEIGHT = 18;

    private static final int TEXT_GREY = 0x999999;
    private static final int TEXT_SHIFT_HEATED = 0xAA6600;
    private static final int TEXT_SHIFT_BRACKET = 0xAAAA00;
    private static final int TEXT_HEAT_SOURCE = 0xCC5522;

    private final EmiStack inputFluid;
    private final EmiStack outputFluid;
    private final Component outputName;
    private final List<EmiIngredient> itemSlots;
    private final List<Component> inputFluidTooltip;
    private final List<Component> outputFluidTooltip;
    private final HeatCondition heat;
    private final boolean converts;

    public FluidMixingEmiRecipe(EmiRecipeCategory category, ResourceLocation id, FluidMixingRecipe recipe) {
        super(category, id, FluidMixingPageArt.WIDTH, FluidMixingPageArt.HEIGHT);

        FluidStack input = recipe.getLiquid();
        FluidStack output = recipe.getLiquidOutput();

        this.inputFluid = display(input);
        this.outputFluid = display(output);
        this.outputName = output.getHoverName();
        this.inputFluidTooltip = potionLines(input);
        this.outputFluidTooltip = potionLines(output);
        this.heat = recipe.getHeatCondition();
        this.converts = !output.isEmpty() && !FluidStack.isSameFluidSameComponents(input, output);

        this.itemSlots = new ArrayList<>(ITEM_SLOTS.length);
        List<Ingredient> ingredients = recipe.getIngredients();
        if (!this.inputFluid.isEmpty()) {
            this.inputs.add(this.inputFluid);
        }
        for (int i = 0; i < ITEM_SLOTS.length; i++) {
            Ingredient ingredient = i < ingredients.size() ? ingredients.get(i) : Ingredient.EMPTY;
            EmiIngredient emi = ingredient.isEmpty() ? EmiStack.EMPTY : EmiIngredient.of(ingredient);
            this.itemSlots.add(emi);
            if (!emi.isEmpty()) {
                this.inputs.add(emi);
            }
        }
        if (!this.outputFluid.isEmpty()) {
            this.outputs.add(this.outputFluid);
        }
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        FluidMixingPageArt.page(widgets);
        FluidMixingPageArt.outputName(widgets, outputName);

        FluidMixingPageArt.outputPanel(widgets, converts);
        if (converts) {
            FluidMixingPageArt.convertsFluidLabel(widgets);
        }

        FluidMixingPageArt.outputSocket(widgets);
        addTank(widgets, outputFluid, 148, 47, 12, 10, outputFluidTooltip).recipeContext(this);
        FluidMixingPageArt.outputSocket(widgets);

        FluidMixingPageArt.inputSocket(widgets);
        addTank(widgets, inputFluid, 16, 53, 16, 32, inputFluidTooltip);
        FluidMixingPageArt.inputSocket(widgets);

        for (int i = 0; i < ITEM_SLOTS.length; i++) {
            EmiIngredient ingredient = itemSlots.get(i);
            if (ingredient.isEmpty()) {
                RecipeSlots.addEmpty(widgets, ITEM_SLOTS[i][0], ITEM_SLOTS[i][1]);
            } else {
                RecipeSlots.add(widgets, ingredient, ITEM_SLOTS[i][0], ITEM_SLOTS[i][1]);
            }
        }

        if (heat != HeatCondition.NONE) {
            Component heatSource = heatSourceName();
            widgets.addTooltip((mouseX, mouseY) -> EmiRecipes.tooltip(heatTooltip(heatSource)),
                    HEAT_HOVER_X, HEAT_HOVER_Y, HEAT_HOVER_WIDTH, HEAT_HOVER_HEIGHT);
        }
    }

    private static SlotWidget addTank(WidgetHolder widgets, EmiStack fluid, int jeiX, int jeiY, int jeiWidth, int jeiHeight,
                                      List<Component> tooltip) {
        SlotWidget tank = widgets.addTank(fluid, jeiX - 1, jeiY - 1, jeiWidth + 2, jeiHeight + 2, TANK_CAPACITY)
                .drawBack(false);
        for (Component line : tooltip) {
            tank.appendTooltip(line);
        }
        return tank;
    }

    private static EmiStack display(FluidStack stack) {
        if (stack.isEmpty()) {
            return EmiStack.EMPTY;
        }
        return EmiStack.of(stack.getFluid(), stack.getComponentsPatch(), TANK_CAPACITY);
    }

    private static List<Component> potionLines(FluidStack stack) {
        List<Component> lines = new ArrayList<>();
        if (stack.isEmpty()) {
            return lines;
        }
        try {
            PotionContents contents = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
            contents.addPotionTooltip(lines::add, 1.0F, 20.0F);
        } catch (Throwable t) {
            lines.clear();
        }
        return lines;
    }

    private List<Component> heatTooltip(Component heatSource) {
        List<Component> lines = new ArrayList<>();
        lines.add(Component.translatable("tooltip.hexerei.heat_source").withStyle(grey()));
        Component shift = Component.translatable("tooltip.hexerei.shift");
        if (Screen.hasShiftDown()) {
            lines.add(Component.translatable("<%s>", shift.copy().withStyle(color(TEXT_SHIFT_HEATED)))
                    .withStyle(grey()));
            lines.add(Component.translatable("tooltip.hexerei.recipe_heated_1").withStyle(grey()));
            lines.add(Component.translatable("tooltip.hexerei.recipe_heated_2").withStyle(grey()));
            lines.add(Component.translatable("tooltip.hexerei.recipe_heated_3").withStyle(grey()));
            lines.add(Component.translatable("tooltip.hexerei.recipe_heated_4").withStyle(grey()));
            if (heatSource != null) {
                lines.add(Component.translatable("Heat source shown: - %s",
                        heatSource.copy().withStyle(color(TEXT_HEAT_SOURCE))).withStyle(grey()));
            }
        } else {
            lines.add(Component.translatable("[%s]", shift.copy().withStyle(color(TEXT_SHIFT_BRACKET)))
                    .withStyle(grey()));
            lines.add(Component.translatable("tooltip.hexerei.recipe_heated").withStyle(grey()));
        }
        return lines;
    }

    private static Component heatSourceName() {
        try {
            return BuiltInRegistries.BLOCK.getTag(HexereiTags.Blocks.HEAT_SOURCES)
                    .flatMap(tag -> tag.getRandomElement(RandomSource.create()))
                    .<Component>map(holder -> Component.translatable(holder.value().getDescriptionId()))
                    .orElse(null);
        } catch (Throwable t) {
            return null;
        }
    }

    private static Style grey() {
        return color(TEXT_GREY);
    }

    private static Style color(int rgb) {
        return Style.EMPTY.withColor(TextColor.fromRgb(rgb));
    }
}
