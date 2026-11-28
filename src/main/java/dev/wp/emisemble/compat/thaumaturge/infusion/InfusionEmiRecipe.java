package dev.wp.emisemble.compat.thaumaturge.infusion;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.recipe.IInfusionRecipe;
import com.leclowndu93150.thaumaturge.content.spell.item.SpellPartIngredient;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.wp.emisemble.compat.thaumaturge.AspectEmiStack;
import dev.wp.emisemble.compat.thaumaturge.ThaumaturgeCompat;
import dev.wp.emisemble.porting.EmiRecipes;
import dev.wp.emisemble.porting.RecipeSlots;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.ArrayList;
import java.util.List;

/** The infusion page: plate, catalyst, components on a circle, aspect row and instability label. */
public final class InfusionEmiRecipe<R extends Recipe<?> & IInfusionRecipe> extends BasicEmiRecipe {
    private static final int WIDTH = 146;
    private static final int HEIGHT = 170;

    // JEI coordinates; RecipeSlots applies the -1 every slot needs.
    private static final int OUTPUT_X = 65;
    private static final int OUTPUT_Y = 7;
    private static final int CATALYST_X = 65;
    private static final int CATALYST_Y = 75;
    private static final float CIRCLE_RADIUS = 40.0F;
    private static final int ASPECT_CENTRE_X = 76;
    private static final int ASPECT_Y = 135;
    private static final int ASPECT_STEP = 22;

    private static final int INSTABILITY_LABEL_CENTRE_X = 73;
    private static final int INSTABILITY_LABEL_Y = 158;
    private static final int INSTABILITY_LEVEL_CAP = 5;
    private static final int PAGE_TEXT_COLOR = 0xFF504030;

    // The barrier is a drawable, not a slot, so it keeps the ingredient rect's top-left corner.
    private static final int GATE_ICON_X = 92;
    private static final int GATE_ICON_Y = 9;

    private static final ChatFormatting[] INSTABILITY_COLORS = {
            ChatFormatting.DARK_BLUE,
            ChatFormatting.BLUE,
            ChatFormatting.DARK_PURPLE,
            ChatFormatting.YELLOW,
            ChatFormatting.GOLD,
            ChatFormatting.DARK_RED
    };

    private final R recipe;
    private final EmiStack output;
    private final EmiIngredient catalyst;
    private final List<EmiIngredient> components;
    private final List<AspectInstance> aspects;

    public InfusionEmiRecipe(EmiRecipeCategory category, RecipeHolder<R> holder) {
        super(category, holder.id(), WIDTH, HEIGHT);
        this.recipe = holder.value();
        this.output = EmiStack.of(recipe.resultItem());
        this.catalyst = ingredientOf(recipe.catalyst());
        this.components = new ArrayList<>();
        for (Ingredient component : recipe.components()) {
            components.add(ingredientOf(component));
        }
        this.aspects = recipe.aspects().sortedByAmount();

        inputs.add(catalyst);
        inputs.addAll(components);
        for (AspectInstance aspect : aspects) {
            inputs.add(new AspectEmiStack(aspect.aspect(), aspect.amount()));
        }
        if (!output.isEmpty()) {
            outputs.add(output);
        }
    }

    /** A spell-part ingredient matches every focus; JEI shows the stamps so the focus draws with its parts. */
    private static EmiIngredient ingredientOf(Ingredient ingredient) {
        if (ingredient.getCustomIngredient() instanceof SpellPartIngredient focus) {
            List<EmiIngredient> stacks = new ArrayList<>();
            for (ItemStack stack : focus.displayStacks()) {
                stacks.add(EmiStack.of(stack));
            }
            return EmiIngredient.of(stacks);
        }
        return EmiIngredient.of(ingredient);
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        ThaumaturgeCompat.art(widgets, 30, 40, 86, 86, 413, 154);
        ThaumaturgeCompat.art(widgets, 57, 0, 32, 32, 40, 6);

        RecipeSlots.add(widgets, output, OUTPUT_X, OUTPUT_Y).recipeContext(this);
        RecipeSlots.add(widgets, catalyst, CATALYST_X, CATALYST_Y);

        // Starts at -90 degrees and walks clockwise, so the first component sits above the catalyst.
        float rotation = -90.0F;
        int count = components.size();
        for (EmiIngredient component : components) {
            float angle = (float) (rotation / 180.0F * Math.PI);
            int x = CATALYST_X + (int) (Mth.cos(angle) * CIRCLE_RADIUS);
            int y = CATALYST_Y + (int) (Mth.sin(angle) * CIRCLE_RADIUS);
            RecipeSlots.add(widgets, component, x, y);
            rotation += 360.0F / count;
        }

        int centre = aspects.size() * ASPECT_STEP / 2;
        for (int i = 0; i < aspects.size(); i++) {
            AspectInstance aspect = aspects.get(i);
            // Bound to the page so a favourited cost keeps its amount: EMI only forces counts on recipe-bound favourites.
            RecipeSlots.add(widgets, new AspectEmiStack(aspect.aspect(), aspect.amount()),
                    ASPECT_CENTRE_X - centre + i * ASPECT_STEP, ASPECT_Y).recipeContext(this);
        }

        int level = Math.min(INSTABILITY_LEVEL_CAP, recipe.instability() / 2);
        Component levelName = Component.translatable("gui.thaumaturge.infusion.instability." + level)
                .withStyle(INSTABILITY_COLORS[level]);
        Component label = Component.translatable("gui.thaumaturge.infusion.instability", levelName);
        int labelWidth = Minecraft.getInstance().font.width(label);
        widgets.addText(label, INSTABILITY_LABEL_CENTRE_X - labelWidth / 2, INSTABILITY_LABEL_Y, PAGE_TEXT_COLOR, false);

        if (!recipe.doesPassGate(Minecraft.getInstance().player)) {
            widgets.addDrawable(GATE_ICON_X - 1, GATE_ICON_Y - 1, 16, 16,
                    (GuiGraphics draw, int mouseX, int mouseY, float delta) -> draw.renderItem(Items.BARRIER.getDefaultInstance(), 0, 0));
        }

        widgets.addTooltip(this::tooltip, GATE_ICON_X - 1, GATE_ICON_Y - 1, 16, 16);
    }

    private List<ClientTooltipComponent> tooltip(int mouseX, int mouseY) {
        if (recipe.doesPassGate(Minecraft.getInstance().player)) {
            return List.of();
        }
        return recipe.researchGate()
                .map(gate -> EmiRecipes.tooltip(ThaumaturgeCompat.missingResearch(gate)))
                .orElse(List.of());
    }
}
