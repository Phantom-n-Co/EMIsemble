package dev.wp.emisemble.compat.hexerei.bottling;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.wp.emisemble.porting.AlphaTextures;
import dev.wp.emisemble.porting.OwnFrameRecipe;
import dev.wp.emisemble.porting.RecipeSlots;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.fluids.FluidStack;

/** One bottling page: an empty bottle and the fluid going in, the filled bottle coming out. */
public final class BottlingEmiRecipe extends BasicEmiRecipe implements OwnFrameRecipe {
    // EMI puts the card 4px outside the widgets, so the page is the art less 8 and the art fills it edge to edge.
    private static final int WIDTH = 118;
    private static final int HEIGHT = 51;

    private static final int INPUT_X = 10;
    private static final int INPUT_Y = 20;

    private static final int OUTPUT_X = 92;
    private static final int OUTPUT_Y = 20;

    private static final int FLUID_X = 58;
    private static final int FLUID_Y = 20;
    private static final int FLUID_SIZE = 16;
    private static final int FLUID_CAPACITY = 2000;

    private static final int TEXT_X = 7;
    private static final int TEXT_Y = 5;
    private static final float TEXT_LINE_HEIGHT = 9.0F / 2.0F;
    private static final int TEXT_MAX_WIDTH = 80;
    private static final int TEXT_COLOR = 0xFF404040;
    private static final int FULL_BRIGHT = 0xF000F0;

    private final ItemStack output;
    private final EmiIngredient input;
    private final EmiStack fluid;

    public BottlingEmiRecipe(EmiRecipeCategory category, ResourceLocation id, Ingredient input, FluidStack fluid,
                             ItemStack output) {
        super(category, id, WIDTH, HEIGHT);
        this.output = output;
        this.input = EmiIngredient.of(input);

        this.fluid = EmiStack.of(fluid.getFluid(), fluid.getComponentsPatch(), fluid.getAmount());

        inputs.add(this.input);
        if (!this.fluid.isEmpty()) {
            inputs.add(this.fluid);
        }
        outputs.add(EmiStack.of(output));
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        AlphaTextures.add(widgets, BottlingCategory.TEXTURE, 256, -4, -4, WIDTH + 8, HEIGHT + 8, 0, 0);

        RecipeSlots.add(widgets, input, INPUT_X, INPUT_Y);
        RecipeSlots.add(widgets, EmiStack.of(output), OUTPUT_X, OUTPUT_Y).recipeContext(this);

        if (!fluid.isEmpty() && fluid.getAmount() > 0) {
            widgets.addTank(fluid.copy(), FLUID_X - 1, FLUID_Y - 1, FLUID_SIZE + 2, FLUID_SIZE + 2, FLUID_CAPACITY)
                    .drawBack(false);
        }

        addResultName(widgets);
    }

    private void addResultName(WidgetHolder widgets) {
        Component name = output.getHoverName();
        widgets.addDrawable(-4, -4, WIDTH, HEIGHT, (GuiGraphics draw, int mouseX, int mouseY, float delta) -> {
            Minecraft minecraft = Minecraft.getInstance();
            float width = minecraft.font.width(name);
            float percent = width > TEXT_MAX_WIDTH ? width / TEXT_MAX_WIDTH : 1.0F;
            draw.pose().pushPose();
            draw.pose().scale(1.0F / percent, 1.0F / percent, 1.0F / percent);
            minecraft.font.drawInBatch(name, TEXT_X * percent, (TEXT_Y + TEXT_LINE_HEIGHT) * percent - 4.5F,
                    TEXT_COLOR, false, draw.pose().last().pose(), draw.bufferSource(),
                    Font.DisplayMode.NORMAL, 0, FULL_BRIGHT);
            draw.pose().popPose();
            draw.flush();
        });
    }
}
