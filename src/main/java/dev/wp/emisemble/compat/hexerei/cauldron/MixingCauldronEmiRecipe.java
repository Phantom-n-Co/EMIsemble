package dev.wp.emisemble.compat.hexerei.cauldron;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.wp.emisemble.porting.EmiRecipes;
import dev.wp.emisemble.porting.OwnFrameRecipe;
import dev.wp.emisemble.porting.RecipeSlots;
import net.joefoxe.hexerei.data.recipes.FluidMixingRecipe.HeatCondition;
import net.joefoxe.hexerei.data.recipes.MixingCauldronRecipe;
import net.joefoxe.hexerei.data.recipes.MoonPhases.MoonCondition;
import net.joefoxe.hexerei.util.HexereiTags;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

/** One {@code hexerei:mixingcauldron} page: eight sockets, a fluid gauge, and the heat and moon tooltips. */
public final class MixingCauldronEmiRecipe extends BasicEmiRecipe implements OwnFrameRecipe {
    private static final int FLUID_CAPACITY = 2000;

    private static final int[][] ITEM_SLOTS = {
            {79, 14}, {101, 23}, {110, 45}, {101, 67}, {79, 76}, {57, 67}, {48, 45}, {57, 23}
    };

    private static final int OUTPUT_X = 152;
    private static final int OUTPUT_Y = 45;
    private static final int OUTPUT_Y_CONVERTING = 26;

    private static final int HEAT_HOVER_X = 75;
    private static final int HEAT_HOVER_Y = 55;
    private static final int MOON_HOVER_X = 75;
    private static final int MOON_HOVER_Y = 31;
    private static final int HOVER_WIDTH = 24;
    private static final int HOVER_HEIGHT = 18;

    private static final int TEXT_GREY = 10066329;
    private static final int TEXT_SHIFT_WARM = 11167232;
    private static final int TEXT_SHIFT_COLD = 11184640;
    private static final int TEXT_HEAT_SOURCE = 13391138;

    private final Component title;
    private final EmiStack output;
    private final EmiStack inputFluid;
    private final EmiStack outputFluid;
    private final List<EmiIngredient> itemSlots;
    private final List<Component> inputFluidTooltip;
    private final List<Component> outputFluidTooltip;
    private final HeatCondition heat;
    private final MoonCondition moon;
    private final boolean convertsFluid;

    public MixingCauldronEmiRecipe(EmiRecipeCategory category, ResourceLocation id, MixingCauldronRecipe recipe) {
        super(category, id, CauldronPageArt.WIDTH, CauldronPageArt.HEIGHT);
        FluidStack liquid = recipe.getLiquid();
        FluidStack liquidOutput = recipe.getLiquidOutput();

        this.title = recipe.getOutput().getHoverName();
        this.output = EmiStack.of(recipe.getOutput());
        this.heat = recipe.getHeatCondition();
        this.moon = recipe.getMoonCondition();

        this.inputFluid = fluid(liquid, Math.min(recipe.getFluidLevelsConsumed(), FLUID_CAPACITY));
        this.convertsFluid = !liquidOutput.isEmpty()
                && !FluidStack.isSameFluidSameComponents(liquid, liquidOutput);
        this.outputFluid = convertsFluid ? fluid(liquidOutput, FLUID_CAPACITY) : EmiStack.EMPTY;
        this.inputFluidTooltip = potionLines(liquid);
        this.outputFluidTooltip = convertsFluid ? potionLines(liquidOutput) : List.of();

        this.itemSlots = new ArrayList<>(ITEM_SLOTS.length);
        for (int i = 0; i < ITEM_SLOTS.length; i++) {
            Ingredient ingredient = i < recipe.getIngredients().size() ? recipe.getIngredients().get(i) : Ingredient.EMPTY;
            EmiIngredient stack = ingredient.isEmpty() ? EmiStack.EMPTY : EmiIngredient.of(ingredient);
            itemSlots.add(stack);
            if (!stack.isEmpty()) {
                inputs.add(stack);
            }
        }
        if (!inputFluid.isEmpty()) {
            inputs.add(inputFluid);
        }
        outputs.add(output);
        if (!outputFluid.isEmpty()) {
            outputs.add(outputFluid);
        }
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        CauldronPageArt.page(widgets);
        CauldronPageArt.moonPhase(widgets, moon);
        CauldronPageArt.title(widgets, title);
        CauldronPageArt.outputPanel(widgets, convertsFluid);

        if (convertsFluid) {
            CauldronPageArt.convertsLabel(widgets);
            addTank(widgets, outputFluid, CauldronPageArt.OUTPUT_TANK_X, CauldronPageArt.OUTPUT_TANK_Y,
                    CauldronPageArt.OUTPUT_TANK_WIDTH, CauldronPageArt.OUTPUT_TANK_HEIGHT, outputFluidTooltip)
                    .recipeContext(this);
            CauldronPageArt.outputRim(widgets);
        }
        if (!inputFluid.isEmpty()) {
            addTank(widgets, inputFluid, CauldronPageArt.INPUT_TANK_X, CauldronPageArt.INPUT_TANK_Y,
                    CauldronPageArt.INPUT_TANK_WIDTH, CauldronPageArt.INPUT_TANK_HEIGHT, inputFluidTooltip);
            CauldronPageArt.inputGauge(widgets);
        }

        for (int i = 0; i < ITEM_SLOTS.length; i++) {
            EmiIngredient ingredient = itemSlots.get(i);

            if (!ingredient.isEmpty()) {
                RecipeSlots.add(widgets, ingredient, ITEM_SLOTS[i][0], ITEM_SLOTS[i][1]);
            }
        }

        RecipeSlots.add(widgets, output, OUTPUT_X, convertsFluid ? OUTPUT_Y_CONVERTING : OUTPUT_Y).recipeContext(this);

        if (heat != HeatCondition.NONE) {
            Component heatSource = heatSource();
            widgets.addTooltip((mouseX, mouseY) -> EmiRecipes.tooltip(heatTooltip(heatSource)),
                    HEAT_HOVER_X, HEAT_HOVER_Y, HOVER_WIDTH, HOVER_HEIGHT);
        }
        if (moon != MoonCondition.NONE) {
            widgets.addTooltip((mouseX, mouseY) -> EmiRecipes.tooltip(moonTooltip()),
                    MOON_HOVER_X, MOON_HOVER_Y, HOVER_WIDTH, HOVER_HEIGHT);
        }
    }

    private static SlotWidget addTank(WidgetHolder widgets, EmiStack stack, int x, int y, int width, int height,
                                      List<Component> tooltip) {
        // TankWidget frames the fluid one pixel inside itself; the +/-1 cancels that so it lands on JEI's rect.
        SlotWidget tank = widgets.addTank(stack, x - 1, y - 1, width + 2, height + 2, FLUID_CAPACITY).drawBack(false);
        for (Component line : tooltip) {
            tank.appendTooltip(line);
        }
        return tank;
    }

    private static EmiStack fluid(FluidStack stack, int amount) {
        if (stack.isEmpty() || amount <= 0) {
            return EmiStack.EMPTY;
        }
        return EmiStack.of(stack.getFluid(), stack.getComponentsPatch(), amount);
    }

    private static List<Component> potionLines(FluidStack stack) {
        if (stack.isEmpty()) {
            return List.of();
        }
        List<Component> lines = new ArrayList<>();
        try {
            PotionContents contents = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
            contents.addPotionTooltip(lines::add, 1.0F, 20.0F);
        } catch (Throwable t) {
            lines.clear();
        }
        return lines;
    }

    private static Component heatSource() {
        try {
            for (Holder<Block> holder : BuiltInRegistries.BLOCK.getTagOrEmpty(HexereiTags.Blocks.HEAT_SOURCES)) {
                return Component.translatable(holder.value().getDescriptionId());
            }
        } catch (Throwable t) {
            return null;
        }
        return null;
    }

    private static List<Component> heatTooltip(Component heatSource) {
        List<Component> tooltip = new ArrayList<>();
        tooltip.add(Component.translatable("tooltip.hexerei.heat_source"));
        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("<%s>",
                    Component.translatable("tooltip.hexerei.shift").withStyle(Style.EMPTY.withColor(TextColor.fromRgb(TEXT_SHIFT_WARM))))
                    .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(TEXT_GREY))));
            for (int i = 1; i <= 4; i++) {
                tooltip.add(Component.translatable("tooltip.hexerei.recipe_heated_" + i)
                        .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(TEXT_GREY))));
            }
            if (heatSource != null) {
                tooltip.add(Component.translatable("tooltip.hexerei.heat_source_shown",
                        heatSource.copy().withStyle(Style.EMPTY.withColor(TextColor.fromRgb(TEXT_HEAT_SOURCE))))
                        .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(TEXT_GREY))));
            }
        } else {
            tooltip.add(Component.translatable("[%s]",
                    Component.translatable("tooltip.hexerei.shift").withStyle(Style.EMPTY.withColor(TextColor.fromRgb(TEXT_SHIFT_COLD))))
                    .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(TEXT_GREY))));
            tooltip.add(Component.translatable("tooltip.hexerei.recipe_heated")
                    .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(TEXT_GREY))));
        }
        return tooltip;
    }

    private List<Component> moonTooltip() {
        List<Component> tooltip = new ArrayList<>();
        tooltip.add(Component.translatable("tooltip.hexerei.moon_phase"));
        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("<%s>",
                    Component.translatable("tooltip.hexerei.shift").withStyle(Style.EMPTY.withColor(TextColor.fromRgb(TEXT_SHIFT_WARM))))
                    .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(TEXT_GREY))));
            tooltip.add(Component.translatable("tooltip.hexerei.recipe_moon_1")
                    .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(TEXT_GREY))));
        } else {
            tooltip.add(Component.translatable("[%s]",
                    Component.translatable("tooltip.hexerei.shift").withStyle(Style.EMPTY.withColor(TextColor.fromRgb(TEXT_SHIFT_COLD))))
                    .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(TEXT_GREY))));
            tooltip.add(Component.translatable("tooltip.hexerei.recipe_moon", Component.translatable(moon.getNameTranslated()))
                    .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(TEXT_GREY))));
        }
        return tooltip;
    }
}
