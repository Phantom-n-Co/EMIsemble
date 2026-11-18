package dev.wp.emisemble.compat.thaumaturge.workbench;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.content.recipe.workbench.ArcaneCraftingRecipe;
import com.leclowndu93150.thaumaturge.content.recipe.workbench.ArcaneShapedCraftingRecipe;
import com.leclowndu93150.thaumaturge.content.recipe.workbench.ArcaneShapelessCraftingRecipe;
import com.leclowndu93150.thaumaturge.content.taint.item.EssentiaCrystalFactory;
import com.leclowndu93150.thaumaturge.content.workbench.MenuArcaneWorkbench;
import com.leclowndu93150.thaumaturge.content.workbench.WorkbenchPayment;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.wp.emisemble.compat.thaumaturge.ThaumaturgeCompat;
import dev.wp.emisemble.porting.EmiRecipes;
import dev.wp.emisemble.porting.RecipeSlots;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** The arcane grid page: both recipe classes share it, differing only in how they fill the nine cells. */
public final class ArcaneWorkbenchEmiRecipe extends BasicEmiRecipe {
    private static final int WIDTH = 162;
    private static final int HEIGHT = 138;

    private static final int GRID_X = 41;
    private static final int GRID_Y = 47;
    private static final int GRID_STEP = 31;
    private static final int CRYSTAL_X = 140;
    private static final int CRYSTAL_Y = 5;
    private static final int CRYSTAL_STEP = 22;
    private static final int OUTPUT_X = 72;
    private static final int OUTPUT_Y = 6;

    private final ArcaneCraftingRecipe recipe;
    private final List<Optional<Ingredient>> grid;
    private final ItemStack[] crystals = new ItemStack[MenuArcaneWorkbench.PRIMAL_ORDER.size()];
    private final ItemStack output;
    private final int visCost;

    private ArcaneWorkbenchEmiRecipe(ArcaneCraftingRecipe recipe, RecipeHolder<ArcaneCraftingRecipe> holder,
                                    List<Optional<Ingredient>> grid, ItemStack output) {
        super(ArcaneWorkbenchCompat.CATEGORY, holder.id(), WIDTH, HEIGHT);
        this.recipe = recipe;
        this.grid = grid;
        this.output = output;
        this.visCost = recipe.visCost();

        for (Optional<Ingredient> cell : grid) {
            cell.ifPresent(ingredient -> inputs.add(EmiIngredient.of(ingredient)));
        }

        // One reserved row per primal, not packed: the same aspect sits on the same row in every recipe.
        List<AspectInstance> crystalCost = recipe.crystalCost().sortedByAmount();
        List<ResourceKey<IAspect>> primals = MenuArcaneWorkbench.PRIMAL_ORDER;
        for (int i = 0; i < primals.size(); i++) {
            for (AspectInstance crystal : crystalCost) {
                if (crystal.aspect().is(primals.get(i))) {
                    crystals[i] = EssentiaCrystalFactory.of(crystal.aspect(), crystal.amount());
                    inputs.add(EmiStack.of(crystals[i]));
                    break;
                }
            }
        }

        outputs.add(EmiStack.of(output));
    }

    static ArcaneWorkbenchEmiRecipe of(RecipeHolder<ArcaneCraftingRecipe> holder) {
        ArcaneCraftingRecipe recipe = holder.value();
        if (recipe instanceof ArcaneShapedCraftingRecipe shaped) {
            return new ArcaneWorkbenchEmiRecipe(recipe, holder, grid(recipe), shaped.result());
        }
        ArcaneShapelessCraftingRecipe shapeless = (ArcaneShapelessCraftingRecipe) recipe;
        return new ArcaneWorkbenchEmiRecipe(recipe, holder, grid(recipe), shapeless.result());
    }

    /** Row-major over the 3x3 grid; shared with the transfer handler so the fill plan matches the page. */
    static List<Optional<Ingredient>> grid(ArcaneCraftingRecipe recipe) {
        List<Optional<Ingredient>> cells = new ArrayList<>(9);
        if (recipe instanceof ArcaneShapedCraftingRecipe shaped) {
            List<Optional<Ingredient>> pattern = shaped.optionalIngredients();
            int width = shaped.getWidth();
            int height = shaped.getHeight();
            for (int y = 0; y < 3; y++) {
                for (int x = 0; x < 3; x++) {
                    cells.add(x < width && y < height ? pattern.get(x + y * width) : Optional.empty());
                }
            }
        } else {
            List<Ingredient> ingredients = ((ArcaneShapelessCraftingRecipe) recipe).ingredients();
            for (int i = 0; i < 9; i++) {
                cells.add(i < ingredients.size() ? Optional.of(ingredients.get(i)) : Optional.empty());
            }
        }
        return cells;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        ThaumaturgeCompat.art(widgets, 30, 36, 102, 102, 225, 31);
        ThaumaturgeCompat.art(widgets, 65, 0, 32, 32, 40, 6);
        ThaumaturgeCompat.art(widgets, 12, 4, 23, 23, 135, 152);

        // All nine cells exist even when empty, as in JEI.
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                Optional<Ingredient> cell = grid.get(x + y * 3);
                int slotX = GRID_X + x * GRID_STEP;
                int slotY = GRID_Y + y * GRID_STEP;
                if (cell.isPresent()) {
                    widgets.addSlot(EmiIngredient.of(cell.get()), slotX, slotY).drawBack(false);
                } else {
                    widgets.addSlot(slotX, slotY).drawBack(false);
                }
            }
        }

        widgets.addSlot(EmiStack.of(output), OUTPUT_X, OUTPUT_Y).drawBack(false).recipeContext(this);

        for (int i = 0; i < crystals.length; i++) {
            if (crystals[i] != null) {
                widgets.addSlot(EmiStack.of(crystals[i]), CRYSTAL_X, CRYSTAL_Y + i * CRYSTAL_STEP).drawBack(false);
            }
        }

        String cost = Integer.toString(visCost);
        int width = Minecraft.getInstance().font.width(cost);
        widgets.addText(Component.literal(cost), 50 - width / 2, 12, 0xFF555555, false);

        if (!recipe.doesPassGate(Minecraft.getInstance().player)) {
            widgets.addDrawable(15, 8, 16, 16, (GuiGraphics draw, int mouseX, int mouseY, float delta) ->
                    draw.renderItem(Items.BARRIER.getDefaultInstance(), 0, 0));
        }

        widgets.addTooltip(this::tooltip, 10, 4, 50, 24);
    }

    private List<ClientTooltipComponent> tooltip(int mouseX, int mouseY) {
        List<Component> lines = new ArrayList<>();
        if (mouseX >= 34) {
            lines.add(Component.translatable("jei.thaumaturge.arcane_workbench.vis_cost"));
            lines.add(Component.translatable("jei.thaumaturge.arcane_workbench.vis_cost_wand", 2));
            lines.add(Component.translatable("jei.thaumaturge.arcane_workbench.vis_cost_aura",
                    WorkbenchPayment.crudeCost(recipe)));
        } else if (!recipe.doesPassGate(Minecraft.getInstance().player)) {
            recipe.researchGate().ifPresent(gate -> lines.addAll(ThaumaturgeCompat.missingResearch(gate)));
        }
        return EmiRecipes.tooltip(lines);
    }
}
