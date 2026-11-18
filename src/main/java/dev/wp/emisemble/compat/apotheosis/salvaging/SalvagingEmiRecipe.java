package dev.wp.emisemble.compat.apotheosis.salvaging;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.affix.salvaging.SalvagingRecipe;
import dev.wp.emisemble.porting.RecipeSlots;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.List;

/** One salvaging page: the input, up to six count-ranged outputs, and the host's {@code min-max} label per output. */
final class SalvagingEmiRecipe extends BasicEmiRecipe {
    // The host's opaque grey sheet, so a plain blit: blending it would paint the art twice.
    private static final ResourceLocation TEXTURE = Apotheosis.loc("textures/gui/salvage_jei.png");
    private static final int WIDTH = 98;
    private static final int HEIGHT = 74;

    private static final int INPUT_X = 5;
    private static final int INPUT_Y = 29;
    private static final int OUTPUT_X = 59;
    private static final int OUTPUT_Y = 11;
    private static final int OUTPUT_SPACING = 18;
    private static final int OUTPUT_COLUMNS = 2;

    // The host draws the label in the socket's lower half: row 0 lands on y = 23 with the socket at y = 11.
    private static final int LABEL_Y_OFFSET = 12;
    private static final float LABEL_SCALE = 0.5F;
    private static final int LABEL_COLOUR = 0xFFFFFF;

    private final RecipeHolder<SalvagingRecipe> holder;
    private final EmiIngredient input;
    private final List<SalvagingRecipe.OutputData> outputData;
    private final List<EmiStack> outputStacks;

    SalvagingEmiRecipe(EmiRecipeCategory category, RecipeHolder<SalvagingRecipe> holder) {
        super(category, holder.id(), WIDTH, HEIGHT);
        this.holder = holder;
        // EmiIngredient.of(Ingredient) walks getItems, so generated affix and gem stacks stay separate entries.
        this.input = EmiIngredient.of(holder.value().getInput());
        this.outputData = List.copyOf(holder.value().getOutputs());
        this.outputStacks = this.outputData.stream().map(data -> EmiStack.of(data.stack())).toList();
        this.inputs.add(this.input);
        this.outputs.addAll(this.outputStacks);
    }

    /** False when the ingredient resolved to nothing, which {@code SalvagingCompat} logs and skips. */
    boolean isDisplayable() {
        return !this.input.isEmpty();
    }

    @Override
    public RecipeHolder<?> getBackingRecipe() {
        return this.holder;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addTexture(TEXTURE, 0, 0, WIDTH, HEIGHT, 0, 0);
        RecipeSlots.add(widgets, this.input, INPUT_X, INPUT_Y);

        for (int i = 0; i < this.outputData.size(); i++) {
            RecipeSlots.add(widgets, this.outputStacks.get(i), outputX(i), outputY(i)).recipeContext(this);
        }

        widgets.addDrawable(0, 0, WIDTH, HEIGHT, this::drawCountLabels);
    }

    private static int outputX(int index) {
        return OUTPUT_X + OUTPUT_SPACING * (index % OUTPUT_COLUMNS);
    }

    private static int outputY(int index) {
        return OUTPUT_Y + OUTPUT_SPACING * (index / OUTPUT_COLUMNS);
    }

    /** The host's label pass, verbatim: {@code min-max}, right-aligned in the socket and drawn at half scale. */
    private void drawCountLabels(GuiGraphics draw, int mouseX, int mouseY, float delta) {
        Font font = Minecraft.getInstance().font;
        for (int i = 0; i < this.outputData.size(); i++) {
            SalvagingRecipe.OutputData data = this.outputData.get(i);
            String label = data.min() + "-" + data.max();
            float x = outputX(i) + (16.0F - font.width(label) * LABEL_SCALE);
            float y = outputY(i) + LABEL_Y_OFFSET;

            draw.pose().pushPose();
            // JEI lifts its overlay 200 into the item plane; same here, since the label overlaps the icon.
            draw.pose().translate(0.0F, 0.0F, 200.0F);
            draw.pose().scale(LABEL_SCALE, LABEL_SCALE, 1.0F);
            draw.drawString(font, label, (int) (x / LABEL_SCALE), (int) (y / LABEL_SCALE), LABEL_COLOUR);
            draw.pose().popPose();
        }
    }
}
