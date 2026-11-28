package dev.wp.emisemble.compat.thaumaturge.crucible;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.content.recipe.crucible.CrucibleRecipe;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.wp.emisemble.compat.thaumaturge.AspectEmiStack;
import dev.wp.emisemble.compat.thaumaturge.ThaumaturgeCompat;
import dev.wp.emisemble.porting.EmiRecipes;
import dev.wp.emisemble.porting.RecipeSlots;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.List;

/** The crucible page: catalyst top left, result beside it, aspect cost centred underneath. */
public final class CrucibleEmiRecipe extends BasicEmiRecipe {
    private static final int WIDTH = 129;
    private static final int HEIGHT = 129;

    // JEI coordinates; RecipeSlots applies the -1 every slot needs.
    private static final int OUTPUT_X = 55;
    private static final int OUTPUT_Y = 8;
    private static final int CATALYST_X = 2;
    private static final int CATALYST_Y = 2;
    private static final int ASPECT_CENTRE_X = 66;
    private static final int ASPECT_Y = 66;
    private static final int ASPECT_STEP = 22;

    private final CrucibleRecipe recipe;
    private final EmiIngredient catalyst;
    private final List<AspectInstance> aspects;
    private final ItemStack output;

    public CrucibleEmiRecipe(RecipeHolder<CrucibleRecipe> holder) {
        super(CrucibleCompat.CATEGORY, holder.id(), WIDTH, HEIGHT);
        this.recipe = holder.value();
        this.output = recipe.rawResult();
        this.catalyst = EmiIngredient.of(recipe.catalyst());
        this.aspects = recipe.aspects().sortedByAmount();

        inputs.add(catalyst);
        for (AspectInstance aspect : aspects) {
            inputs.add(new AspectEmiStack(aspect.aspect(), aspect.amount()));
        }
        outputs.add(EmiStack.of(output));
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        // Frames come from the page texture, so EMI's own slot backgrounds stay off throughout.
        ThaumaturgeCompat.art(widgets, 9, 0, 109, 129, 2, 5);
        ThaumaturgeCompat.art(widgets, 16, 6, 26, 26, 199, 168);

        RecipeSlots.add(widgets, EmiStack.of(output), OUTPUT_X, OUTPUT_Y).recipeContext(this);
        RecipeSlots.add(widgets, catalyst, CATALYST_X, CATALYST_Y);

        int centre = aspects.size() * ASPECT_STEP / 2;
        for (int i = 0; i < aspects.size(); i++) {
            AspectInstance aspect = aspects.get(i);
            RecipeSlots.add(widgets, new AspectEmiStack(aspect.aspect(), aspect.amount()),
                    ASPECT_CENTRE_X - centre + i * ASPECT_STEP, ASPECT_Y).recipeContext(this);
        }

        if (!recipe.doesPassGate(Minecraft.getInstance().player)) {
            widgets.addDrawable(22, 14, 16, 16, (GuiGraphics draw, int mouseX, int mouseY, float delta) ->
                    draw.renderItem(Items.BARRIER.getDefaultInstance(), 0, 0));
        }

        widgets.addTooltip(this::tooltip, 22, 14, 18, 16);
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
