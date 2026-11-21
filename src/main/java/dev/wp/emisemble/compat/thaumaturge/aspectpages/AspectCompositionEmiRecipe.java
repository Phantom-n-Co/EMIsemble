package dev.wp.emisemble.compat.thaumaturge.aspectpages;

import com.leclowndu93150.thaumaturge.api.aspect.AspectComponents;
import com.leclowndu93150.thaumaturge.api.aspect.AspectKnowledgeAccess;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.wp.emisemble.compat.thaumaturge.AspectEmiStack;
import dev.wp.emisemble.porting.RecipeSlots;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.Nullable;

/** One non-primal aspect and the two it is made of: {@code left + right = result} on a bare 100x26 page. */
final class AspectCompositionEmiRecipe extends BasicEmiRecipe {
    static final int WIDTH = 100;
    static final int HEIGHT = 26;

    // JEI coordinates; RecipeSlots applies the -1 every slot needs.
    private static final int LEFT_X = 6;
    private static final int LEFT_Y = 5;
    private static final int RIGHT_X = 38;
    private static final int RESULT_X = 78;

    private static final int TEXT_COLOR = 0xFF000000 | 0x555555;

    private final Holder<IAspect> left;
    private final Holder<IAspect> right;
    private final Holder<IAspect> result;

    AspectCompositionEmiRecipe(EmiRecipeCategory category, ResourceLocation id, Holder<IAspect> left,
                               Holder<IAspect> right, Holder<IAspect> result) {
        super(category, id, WIDTH, HEIGHT);
        this.left = left;
        this.right = right;
        this.result = result;
        this.inputs.add(new AspectEmiStack(left, 1));
        this.inputs.add(new AspectEmiStack(right, 1));
        this.outputs.add(new AspectEmiStack(result, 1));
    }

    /** The host shows a composition once its aspect is deducible, not only once it is known. */
    boolean revealed() {
        return AspectKnowledgeAccess.of(result).isCompositionRevealed();
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        // Symbols and names first: JEI draws them in the category, before the chips.
        widgets.addDrawable(0, 0, WIDTH, HEIGHT, this::drawSymbols);

        RecipeSlots.add(widgets, new AspectEmiStack(left, 1), LEFT_X, LEFT_Y);
        RecipeSlots.add(widgets, new AspectEmiStack(right, 1), RIGHT_X, LEFT_Y);
        RecipeSlots.add(widgets, new AspectEmiStack(result, 1), RESULT_X, LEFT_Y);
    }

    private void drawSymbols(GuiGraphics draw, int mouseX, int mouseY, float delta) {
        Font font = Minecraft.getInstance().font;
        draw.drawString(font, "+", 27, 10, TEXT_COLOR, false);
        draw.drawString(font, "=", 63, 10, TEXT_COLOR, false);

        // EMI has no half-scale text widget, so the host's 0.5 pose is replicated here verbatim.
        draw.pose().pushPose();
        draw.pose().scale(0.5F, 0.5F, 1.0F);
        drawName(draw, font, left, 14);
        drawName(draw, font, right, 46);
        drawName(draw, font, result, 86);
        draw.pose().popPose();
    }

    /** JEI centres a name on {@code centre} by drawing at twice the offset inside the half-scale pose. */
    private static void drawName(GuiGraphics draw, Font font, Holder<IAspect> aspect, int centre) {
        Component name = AspectComponents.name(aspect);
        int halfWidth = font.width(name) / 2;
        draw.drawString(font, name, (centre - halfWidth / 2) * 2, 45, TEXT_COLOR, false);
    }

    /** A lookup table, not a way to obtain the aspect: it must not be offered as a crafting path. */
    @Override
    public boolean supportsRecipeTree() {
        return false;
    }

    @Override
    public boolean hideCraftable() {
        return true;
    }

    @Nullable
    @Override
    public RecipeHolder<?> getBackingRecipe() {
        return null;
    }
}
